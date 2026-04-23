package cn.iocoder.yudao.module.iot.service.security;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.client.ZlmApiClient;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.ChannelRecordingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackClipReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackClipRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackQueryRecordingsBatchReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackQueryRecordingsReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackUrlReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackUrlRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.RecordingSegmentRespVO;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.device.NvrDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.manager.DeviceCommandResponseManager;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.module.iot.service.ibms.device.support.IbmsDeviceDahuaSdkHelper;
import cn.iocoder.yudao.module.iot.service.video.IbmsDeviceVideoNetworkResolver;
import cn.iocoder.yudao.module.iot.service.video.ZlmStreamService;
import org.apache.commons.lang3.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * 录像回放服务（实现 /security/playback/* 接口）
 *
 * <p>当前实现以“外网演示可用”为优先：</p>
 * <ul>
 *   <li>回放拉流：复用 {@link ZlmStreamService#getPlaybackUrl(Long, String, String, String)}</li>
 *   <li>录像片段查询：先提供占位片段（整段视为有录像），后续可对接 NVR 搜索命令/落库结果</li>
 *   <li>剪切：占位返回（前端可提示“已提交/不支持”）</li>
 * </ul>
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class VideoPlaybackServiceImpl implements VideoPlaybackService {

    private final IbmsChannelMapper ibmsChannelMapper;
    private final IbmsDeviceMapper ibmsDeviceMapper;
    private final IbmsDeviceRuntimeService ibmsDeviceRuntimeService;
    private final ZlmStreamService zlmStreamService;
    private final ZlmApiClient zlmApiClient;
    private final IotMessageBus messageBus;
    private final DeviceCommandResponseManager responseManager;

    private static final String APP_PLAYBACK = "playback";

    @Override
    public List<ChannelRecordingRespVO> queryRecordings(PlaybackQueryRecordingsReqVO reqVO) {
        IotDeviceChannelDO channel = loadChannel(reqVO.getCameraId());
        if (channel == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "通道不存在: channelId=" + reqVO.getCameraId());
        }

        // 通过 newgateway（大华 SDK）搜索录像文件列表，再转换为时间轴片段
        IbmsDeviceDO device = ibmsDeviceMapper.selectById(channel.getDeviceId());
        if (device == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "设备不存在: deviceId=" + channel.getDeviceId());
        }
        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeService.getByDeviceId(device.getId());
        IbmsDeviceVideoNetworkResolver.NetworkParams net = IbmsDeviceVideoNetworkResolver.resolve(device, runtime);
        String ip = net.ip;
        int port = IbmsDeviceDahuaSdkHelper.resolveDahuaSdkPort(runtime);
        String username = StringUtils.defaultIfBlank(net.username, "admin");
        String password = StringUtils.defaultIfBlank(net.password, "admin123");

        String requestId = UUID.randomUUID().toString();
        Map<String, Object> params = new HashMap<>();
        params.put(NvrDeviceTypeConstants.PARAM_IP, ip);
        params.put(NvrDeviceTypeConstants.PARAM_PORT, port);
        params.put(NvrDeviceTypeConstants.PARAM_USERNAME, username);
        params.put(NvrDeviceTypeConstants.PARAM_PASSWORD, password);
        params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channel.getChannelNo());
        params.put(NvrDeviceTypeConstants.PARAM_START_TIME, reqVO.getStartTime());
        params.put(NvrDeviceTypeConstants.PARAM_END_TIME, reqVO.getEndTime());
        params.put(NvrDeviceTypeConstants.PARAM_RECORD_TYPE, reqVO.getRecordType() != null ? reqVO.getRecordType() : 0);
        params.put("deviceType", NvrDeviceTypeConstants.NVR);
        params.put("commandType", NvrDeviceTypeConstants.COMMAND_SEARCH_RECORDS);

        CompletableFuture<IotDeviceMessage> future = responseManager.registerRequest(requestId);
        IotDeviceMessage message = IotDeviceMessage.requestOf(requestId, NvrDeviceTypeConstants.COMMAND_SEARCH_RECORDS, params);
        message.setDeviceId(device.getId());
        messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, message);
        log.info("[Playback] 已发送录像搜索命令: deviceId={}, channelId={}, channelNo={}, requestId={}",
                device.getId(), channel.getId(), channel.getChannelNo(), requestId);

        IotDeviceMessage response;
        try {
            response = responseManager.waitForResponse(requestId, future, 15);
        } catch (Exception e) {
            log.warn("[Playback] 等待录像搜索响应超时/失败: requestId={}, error={}", requestId, e.getMessage());
            ChannelRecordingRespVO vo = new ChannelRecordingRespVO();
            vo.setChannelId(channel.getId());
            vo.setChannelName(channel.getChannelName());
            // 超时/失败时不再“整段标绿”，直接返回空片段，让前端显示“未找到录像”
            vo.setSegments(List.of());
            return List.of(vo);
        }

        List<RecordingSegmentRespVO> segments = new ArrayList<>();
        Object data = response != null ? response.getData() : null;
        if (data instanceof Map) {
            Object filesObj = ((Map<?, ?>) data).get("files");
            if (filesObj instanceof List) {
                for (Object f : (List<?>) filesObj) {
                    if (!(f instanceof Map)) continue;
                    Map<?, ?> file = (Map<?, ?>) f;
                    String s = file.get("startTime") != null ? file.get("startTime").toString() : null;
                    String e = file.get("endTime") != null ? file.get("endTime").toString() : null;
                    if (StrUtil.isBlank(s) || StrUtil.isBlank(e)) continue;
                    RecordingSegmentRespVO seg = new RecordingSegmentRespVO();
                    seg.setStartTime(s);
                    seg.setEndTime(e);
                    seg.setHasRecording(true);
                    segments.add(seg);
                }
            }
        }

        if (segments.isEmpty()) {
            // 无 files 时不回退整段占位，避免把整个时间轴渲染为“有录像”
            segments = List.of();
        }

        ChannelRecordingRespVO vo = new ChannelRecordingRespVO();
        vo.setChannelId(channel.getId());
        vo.setChannelName(channel.getChannelName());
        vo.setSegments(segments);
        return List.of(vo);
    }

    @Override
    public List<ChannelRecordingRespVO> queryRecordingsBatch(PlaybackQueryRecordingsBatchReqVO reqVO) {
        if (reqVO.getCameraIds() == null || reqVO.getCameraIds().isEmpty()) {
            throw new ServiceException(BAD_REQUEST.getCode(), "cameraIds 不能为空");
        }

        // 1) 批量加载通道（当前 service 无批量方法，先逐个取；真正的瓶颈在 NVR 查询，不在 DB 读）
        List<Long> cameraIds = reqVO.getCameraIds();
        Map<Long, IotDeviceChannelDO> channelById = new LinkedHashMap<>();
        for (Long id : cameraIds) {
            IotDeviceChannelDO ch = loadChannel(id);
            if (ch != null) {
                channelById.put(id, ch);
            }
        }
        // 若有不存在的通道，仍返回占位空 segments，避免前端整体失败

        // 2) 按 deviceId 分组，单 device 发 1 条批量命令
        Map<Long, List<IotDeviceChannelDO>> channelsByDeviceId = channelById.values().stream()
                .collect(Collectors.groupingBy(IotDeviceChannelDO::getDeviceId));

        Map<Long, List<RecordingSegmentRespVO>> segmentsByChannelId = new HashMap<>();

        for (Map.Entry<Long, List<IotDeviceChannelDO>> entry : channelsByDeviceId.entrySet()) {
            Long deviceId = entry.getKey();
            List<IotDeviceChannelDO> channels = entry.getValue();

            IbmsDeviceDO device = ibmsDeviceMapper.selectById(deviceId);
            if (device == null) {
                // 设备不存在：该 device 下所有通道返回空 segments
                for (IotDeviceChannelDO ch : channels) {
                    segmentsByChannelId.put(ch.getId(), List.of());
                }
                continue;
            }

            IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeService.getByDeviceId(deviceId);
            IbmsDeviceVideoNetworkResolver.NetworkParams net = IbmsDeviceVideoNetworkResolver.resolve(device, runtime);
            String ip = net.ip;
            int port = IbmsDeviceDahuaSdkHelper.resolveDahuaSdkPort(runtime);
            String username = StringUtils.defaultIfBlank(net.username, "admin");
            String password = StringUtils.defaultIfBlank(net.password, "admin123");

            String requestId = UUID.randomUUID().toString();
            Map<String, Object> params = new HashMap<>();
            params.put(NvrDeviceTypeConstants.PARAM_IP, ip);
            params.put(NvrDeviceTypeConstants.PARAM_PORT, port);
            params.put(NvrDeviceTypeConstants.PARAM_USERNAME, username);
            params.put(NvrDeviceTypeConstants.PARAM_PASSWORD, password);
            params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NOS,
                    channels.stream().map(IotDeviceChannelDO::getChannelNo).collect(Collectors.toList()));
            params.put(NvrDeviceTypeConstants.PARAM_START_TIME, reqVO.getStartTime());
            params.put(NvrDeviceTypeConstants.PARAM_END_TIME, reqVO.getEndTime());
            params.put(NvrDeviceTypeConstants.PARAM_RECORD_TYPE, reqVO.getRecordType() != null ? reqVO.getRecordType() : 0);
            params.put("deviceType", NvrDeviceTypeConstants.NVR);
            params.put("commandType", NvrDeviceTypeConstants.COMMAND_SEARCH_RECORDS_BATCH);

            CompletableFuture<IotDeviceMessage> future = responseManager.registerRequest(requestId);
            IotDeviceMessage message = IotDeviceMessage.requestOf(requestId, NvrDeviceTypeConstants.COMMAND_SEARCH_RECORDS_BATCH, params);
            message.setDeviceId(deviceId);
            messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, message);

            IotDeviceMessage response;
            try {
                response = responseManager.waitForResponse(requestId, future, 15);
            } catch (Exception e) {
                log.warn("[Playback] 批量录像搜索响应超时/失败: requestId={}, deviceId={}, err={}",
                        requestId, deviceId, e.getMessage());
                for (IotDeviceChannelDO ch : channels) {
                    segmentsByChannelId.put(ch.getId(), List.of());
                }
                continue;
            }

            // 3) 解析 filesByChannelNo: { channelNo -> [ {startTime,endTime,...}, ... ] }
            Map<Integer, List<RecordingSegmentRespVO>> segsByChannelNo = new HashMap<>();
            Object data = response != null ? response.getData() : null;
            if (data instanceof Map) {
                Object mapObj = ((Map<?, ?>) data).get("filesByChannelNo");
                if (mapObj instanceof Map) {
                    for (Map.Entry<?, ?> me : ((Map<?, ?>) mapObj).entrySet()) {
                        Integer channelNo = null;
                        try {
                            channelNo = Integer.parseInt(String.valueOf(me.getKey()));
                        } catch (Exception ignored) {}
                        if (channelNo == null) continue;

                        List<RecordingSegmentRespVO> segs = new ArrayList<>();
                        Object filesObj = me.getValue();
                        if (filesObj instanceof List) {
                            for (Object f : (List<?>) filesObj) {
                                if (!(f instanceof Map)) continue;
                                Map<?, ?> file = (Map<?, ?>) f;
                                String s = file.get("startTime") != null ? file.get("startTime").toString() : null;
                                String e = file.get("endTime") != null ? file.get("endTime").toString() : null;
                                if (StrUtil.isBlank(s) || StrUtil.isBlank(e)) continue;
                                RecordingSegmentRespVO seg = new RecordingSegmentRespVO();
                                seg.setStartTime(s);
                                seg.setEndTime(e);
                                seg.setHasRecording(true);
                                segs.add(seg);
                            }
                        }
                        segsByChannelNo.put(channelNo, segs);
                    }
                }
            }

            // 4) 按 channelId 回填
            for (IotDeviceChannelDO ch : channels) {
                List<RecordingSegmentRespVO> segs = segsByChannelNo.getOrDefault(ch.getChannelNo(), List.of());
                segmentsByChannelId.put(ch.getId(), segs);
            }
        }

        // 5) 按入参 cameraIds 顺序输出
        List<ChannelRecordingRespVO> resp = new ArrayList<>(cameraIds.size());
        for (Long id : cameraIds) {
            IotDeviceChannelDO ch = channelById.get(id);
            ChannelRecordingRespVO vo = new ChannelRecordingRespVO();
            vo.setChannelId(id);
            vo.setChannelName(ch != null ? ch.getChannelName() : ("通道" + id));
            vo.setSegments(segmentsByChannelId.getOrDefault(id, List.of()));
            resp.add(vo);
        }
        return resp;
    }

    @Override
    public PlaybackUrlRespVO getPlaybackUrl(PlaybackUrlReqVO reqVO) {
        IotDeviceChannelDO channel = loadChannel(reqVO.getCameraId());
        if (channel == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "通道不存在: channelId=" + reqVO.getCameraId());
        }

        // playId 这里用前端窗口索引更好；当前前端未传，先用 null（会按时间 hash 生成 streamKey）
        var playUrls = zlmStreamService.getPlaybackUrl(reqVO.getCameraId(), reqVO.getStartTime(), reqVO.getEndTime(), null);

        return PlaybackUrlRespVO.builder()
                .cameraId(channel.getId())
                .cameraName(channel.getChannelName())
                .rtspUrl(null)
                .flvUrl(playUrls.getFlvUrl())
                .webrtcUrl(playUrls.getWebrtcUrl())
                .streamId(playUrls.getStreamKey())
                .app(APP_PLAYBACK)
                .stream(playUrls.getStreamKey())
                .build();
    }

    @Override
    public void closePlaybackStream(String streamId) {
        if (StrUtil.isBlank(streamId)) {
            return;
        }
        boolean closed = zlmApiClient.closeStream(APP_PLAYBACK, streamId);
        log.info("[Playback] closePlaybackStream streamId={}, closed={}", streamId, closed);
        // 代理删除使用内部 cache key 管理，当前不强依赖删除代理（close_streams 会停止拉流与转发）
    }

    @Override
    public PlaybackClipRespVO clipRecording(PlaybackClipReqVO reqVO) {
        IotDeviceChannelDO channel = loadChannel(reqVO.getCameraId());
        if (channel == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "通道不存在: channelId=" + reqVO.getCameraId());
        }

        // 占位：当前 iot-biz 未实现下载剪切文件；先返回空，让前端提示“已提交/不支持”
        return PlaybackClipRespVO.builder()
                .cameraId(channel.getId())
                .cameraName(channel.getChannelName())
                .startTime(reqVO.getStartTime())
                .endTime(reqVO.getEndTime())
                .fileUrl(null)
                .filePath(null)
                .build();
    }

    private IotDeviceChannelDO loadChannel(Long channelId) {
        IbmsChannelDO channel = ibmsChannelMapper.selectById(channelId);
        if (channel == null) {
            return null;
        }
        return IotDeviceChannelDO.builder()
                .id(channel.getId())
                .deviceId(channel.getDeviceId())
                .channelNo(channel.getChannelNo())
                .channelName(channel.getName())
                .build();
    }
}

