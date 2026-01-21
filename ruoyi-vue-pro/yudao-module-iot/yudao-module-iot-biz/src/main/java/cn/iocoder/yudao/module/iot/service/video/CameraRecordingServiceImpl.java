package cn.iocoder.yudao.module.iot.service.video;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraRecordingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraRecordingRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraRecordingDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.mysql.camera.IotCameraRecordingMapper;
import cn.iocoder.yudao.module.iot.enums.device.NvrDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import org.springframework.web.multipart.MultipartFile;

/**
 * 摄像头录像记录 Service实现类
 * 
 * <p>提供录像查询、上传、开始/停止录像等功能。</p>
 * <p>录像查询通过 DeviceCommandPublisher 发送命令到 newgateway，替代原有的 HTTP 调用。</p>
 * 
 * <p>Requirements: 11.1, 11.3, 11.4, 11.5</p>
 *
 * @author 长辉信息
 */
@Slf4j
@Service
public class CameraRecordingServiceImpl implements CameraRecordingService {

    @Resource
    private IotCameraRecordingMapper cameraRecordingMapper;

    @Resource
    private IotDeviceService deviceService;
    
    @Resource
    private IotDeviceChannelService channelService;

    /**
     * 设备命令发布器（统一命令接口）
     * <p>Requirements: 11.1</p>
     */
    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;

    /**
     * 录像文件存储目录（关联基础设施模块）
     * 默认：/recordings/{deviceId}/{yyyyMMdd}/
     */
    @Value("${iot.recording.directory:/recordings}")
    private String recordingDirectory;


    /**
     * 录像文件URL前缀（ZLMediaKit HTTP服务地址）
     * 例如：http://192.168.1.246:8080/recordings
     */
    @Value("${iot.recording.url-prefix:http://192.168.1.246:8080/recordings}")
    private String recordingUrlPrefix;


    @Override
    public PageResult<CameraRecordingRespVO> getRecordingPage(CameraRecordingPageReqVO pageReqVO) {
        log.info("[录像查询] 开始查询录像: cameraId={}, cameraIds={}, startTime={}, endTime={}", 
                pageReqVO.getCameraId(), pageReqVO.getCameraIds(), pageReqVO.getStartTime(), pageReqVO.getEndTime());
        
        // 参数校验：优先使用 cameraIds，其次使用 cameraId
        List<Long> channelIds = new ArrayList<>();
        if (pageReqVO.getCameraIds() != null && !pageReqVO.getCameraIds().isEmpty()) {
            channelIds.addAll(pageReqVO.getCameraIds());
        } else if (pageReqVO.getCameraId() != null) {
            channelIds.add(pageReqVO.getCameraId());
        }
        
        if (channelIds.isEmpty()) {
            log.warn("[录像查询] 通道ID为空");
            return new PageResult<>(new ArrayList<>(), 0L);
        }
        
        if (pageReqVO.getStartTime() == null || pageReqVO.getEndTime() == null) {
            log.warn("[录像查询] 时间范围为空");
            return new PageResult<>(new ArrayList<>(), 0L);
        }
        
        try {
            for (Long channelId : channelIds) {
                // 1. 查询通道信息
                IotDeviceChannelDO channel = channelService.getChannel(channelId);
                if (channel == null) {
                    log.warn("[录像查询] 通道不存在: channelId={}", channelId);
                    continue;
                }
            
                // 2. 查询设备信息
                IotDeviceDO device = deviceService.getDevice(channel.getDeviceId());
                if (device == null) {
                    log.warn("[录像查询] 设备不存在: deviceId={}", channel.getDeviceId());
                    continue;
                }
                
                // 3. 获取设备连接信息（从 config 中获取）
                String ip = DeviceConfigHelper.getIpAddress(device);
                Integer port = DeviceConfigHelper.getPort(device) != null ? DeviceConfigHelper.getPort(device) : 37777;  // 大华默认端口
                String username = "admin";  // 默认用户名
                String password = "admin123";  // 默认密码
                
                // TODO: 从设备或通道配置中获取实际的端口、用户名、密码
                
                // 4. 转换时间格式：LocalDateTime -> String (yyyy-MM-dd HH:mm:ss)
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String startTimeStr = pageReqVO.getStartTime() != null ? 
                        pageReqVO.getStartTime().format(formatter) : null;
                String endTimeStr = pageReqVO.getEndTime() != null ? 
                        pageReqVO.getEndTime().format(formatter) : null;
                
                // 5. 通过消息总线发送录像搜索命令
                // Requirements: 11.1, 11.3
                // 注意：录像搜索结果将通过 WebSocket 异步返回给前端
                try {
                    Map<String, Object> params = new HashMap<>();
                    params.put(NvrDeviceTypeConstants.PARAM_IP, ip);
                    params.put(NvrDeviceTypeConstants.PARAM_PORT, port);
                    params.put(NvrDeviceTypeConstants.PARAM_USERNAME, username);
                    params.put(NvrDeviceTypeConstants.PARAM_PASSWORD, password);
                    params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channel.getChannelNo());
                    params.put(NvrDeviceTypeConstants.PARAM_START_TIME, startTimeStr);
                    params.put(NvrDeviceTypeConstants.PARAM_END_TIME, endTimeStr);
                    params.put(NvrDeviceTypeConstants.PARAM_RECORD_TYPE, 
                            pageReqVO.getRecordingType() != null ? pageReqVO.getRecordingType() : 0);

                    String requestId = deviceCommandPublisher.publishCommand(
                            NvrDeviceTypeConstants.NVR,
                            device.getId(),
                            NvrDeviceTypeConstants.COMMAND_SEARCH_RECORDS,
                            params
                    );

                    log.info("[录像查询] 已发送录像搜索命令: channelId={}, deviceId={}, requestId={}", 
                            channelId, device.getId(), requestId);
                    
                    // 录像搜索结果将通过 DeviceServiceResultConsumer 处理
                    // 并通过 DeviceMessagePushService 推送到前端 WebSocket
                    // 前端需要监听 WebSocket 消息获取搜索结果
                    
                } catch (Exception e) {
                    log.warn("[录像查询] 通过消息总线发送搜索命令失败: {}", e.getMessage());
                }
            }
            
            // 6. 返回空结果，实际录像数据将通过 WebSocket 异步推送
            // 前端需要：
            // 1. 调用此接口发起搜索请求
            // 2. 监听 WebSocket 消息获取搜索结果
            log.info("[录像查询] ✅ 已发送搜索命令: 总通道数={}, 结果将通过 WebSocket 推送", channelIds.size());
            
            return new PageResult<>(new ArrayList<>(), 0L);
            
        } catch (Exception e) {
            log.error("[录像查询] 查询失败", e);
            return new PageResult<>(new ArrayList<>(), 0L);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadRecording(Long deviceId, Integer recordingType, MultipartFile file) throws Exception {
        // 1. 验证设备
        IotDeviceDO device = deviceService.getDevice(deviceId);
        if (device == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "设备不存在");
        }

        // 2. 直接落盘保存文件（避免强依赖 infra 模块的 FileApi）
        String filePath = generateRecordingPath(deviceId);
        Path target = Paths.get(filePath);
        Files.createDirectories(target.getParent());
        Files.write(target, file.getBytes());

        // 3. 生成访问 URL（基于 recordingUrlPrefix）
        String url;
        String normalizedDir = recordingDirectory != null ? recordingDirectory.replace("\\", "/") : "";
        String normalizedPath = filePath.replace("\\", "/");
        String relative = normalizedDir.isEmpty() ? normalizedPath : normalizedPath.replaceFirst("^" + java.util.regex.Pattern.quote(normalizedDir), "");
        if (!relative.startsWith("/")) {
            relative = "/" + relative;
        }
        url = recordingUrlPrefix.replaceAll("/+$", "") + relative;

        // 4. 解析路径
        String pathOnly = null;
        try {
            URL u = new URL(url);
            pathOnly = u.getPath();
        } catch (Exception ignore) {
            pathOnly = url;
        }

        // 5. 创建录像记录（状态：已完成）
        IotCameraRecordingDO recording = new IotCameraRecordingDO();
        recording.setCameraId(deviceId);
        recording.setRecordingType(recordingType != null ? recordingType : 1);
        recording.setStatus(1); // 1=已完成
        LocalDateTime now = LocalDateTime.now();
        recording.setStartTime(now);
        recording.setEndTime(now);
        recording.setDuration(0);
        recording.setFilePath(pathOnly);
        recording.setFileUrl(url);
        recording.setFileSize(file.getSize());

        cameraRecordingMapper.insert(recording);

        log.info("[录像管理] 上传录像并创建记录: id={}, deviceId={}, url={}", recording.getId(), deviceId, url);
        return recording.getId();
    }

    @Override
    public IotCameraRecordingDO getRecording(Long id) {
        return cameraRecordingMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecording(Long id) {
        IotCameraRecordingDO recording = cameraRecordingMapper.selectById(id);
        if (recording == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "录像记录不存在");
        }
        
        // TODO: 同时删除物理文件（通过基础设施模块的FileService）
        // 或者调用ZLMediaKit删除录像文件
        
        cameraRecordingMapper.deleteById(id);
        log.info("[录像管理] 删除录像记录成功: id={}, cameraId={}", id, recording.getCameraId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startRecording(Long deviceId, Integer duration, String policy) {
        // 1. 验证设备
        IotDeviceDO device = deviceService.getDevice(deviceId);
        if (device == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "设备不存在");
        }
        
        if (device.getState() != 1) {
            log.warn("[录像管理] 设备状态非在线(state={}), 仍尝试开始录像 deviceId={}", device.getState(), deviceId);
        }
        
        // 2. 创建录像记录（状态：录制中）
        IotCameraRecordingDO recording = new IotCameraRecordingDO();
        recording.setCameraId(deviceId);
        recording.setRecordingType(1);
        recording.setStatus(0);
        recording.setStartTime(LocalDateTime.now());
        recording.setDuration(0);
        recording.setFileSize(0L);
        
        // 生成录像文件路径（ZLMediaKit会使用此路径保存）
        String filePath = generateRecordingPath(deviceId);
        recording.setFilePath(filePath);
        recording.setFileUrl(recordingUrlPrefix + filePath); // 生成完整URL
        
        cameraRecordingMapper.insert(recording);
        
        log.info("[录像管理] 创建录像记录成功: id={}, deviceId={}, duration={}", 
                recording.getId(), deviceId, duration);
        
        // 3. 通过物模型发送录像控制指令到Gateway
        sendRecordingCommand(deviceId, "start", recording.getId(), duration, filePath, policy);
        
        return recording.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopRecording(Long recordingId) {
        // 1. 查询录像记录
        IotCameraRecordingDO recording = cameraRecordingMapper.selectById(recordingId);
        if (recording == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "录像记录不存在");
        }
        
        if (recording.getStatus() != 0) {
            throw new ServiceException(BAD_REQUEST.getCode(), "录像已停止，无需重复操作");
        }
        
        // 2. 通过物模型发送停止录像指令到Gateway
        sendRecordingCommand(recording.getCameraId(), "stop", recordingId, null, null, null);
        
        log.info("[录像管理] 发送停止录像指令: recordingId={}, deviceId={}", 
                recordingId, recording.getCameraId());

        // 3. 标记为已停止，结束时间为当前
        recording.setEndTime(LocalDateTime.now());
        recording.setStatus(2);
        cameraRecordingMapper.updateById(recording);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRecording(Long deviceId, Integer recordingType, LocalDateTime startTime, String filePath) {
        // 由ZLMediaKit回调触发，创建录像记录
        IotCameraRecordingDO recording = new IotCameraRecordingDO();
        recording.setCameraId(deviceId);
        // 注意：iot_camera_recording表没有device_name字段，设备名称通过关联查询获取
        recording.setRecordingType(recordingType);
        recording.setStatus(0); // 0=录制中
        recording.setStartTime(startTime);
        recording.setFilePath(filePath);
        recording.setFileUrl(recordingUrlPrefix + filePath);
        recording.setDuration(0);
        recording.setFileSize(0L);
        
        cameraRecordingMapper.insert(recording);
        
        log.info("[录像管理] [ZLMediaKit回调] 创建录像记录: id={}, deviceId={}, type={}", 
                recording.getId(), deviceId, recordingType);
        
        return recording.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeRecording(Long recordingId, LocalDateTime endTime, Long fileSize, Integer duration) {
        // 由ZLMediaKit回调触发，完成录像记录
        IotCameraRecordingDO recording = cameraRecordingMapper.selectById(recordingId);
        if (recording == null) {
            log.warn("[录像管理] [ZLMediaKit回调] 录像记录不存在: recordingId={}", recordingId);
            return;
        }
        
        recording.setEndTime(endTime);
        recording.setFileSize(fileSize);
        recording.setDuration(duration);
        // 若仍处于录制中(0)，则标记为已完成(1)；若已被手动停止(2)，保留停止状态
        if (recording.getStatus() == 0) {
            recording.setStatus(1);
        }
        
        cameraRecordingMapper.updateById(recording);
        
        log.info("[录像管理] [ZLMediaKit回调] 完成录像记录: id={}, duration={}s, size={}bytes", 
                recordingId, duration, fileSize);
    }

    /**
     * 生成录像文件路径
     * 格式：/recordings/{deviceId}/{yyyyMMdd}/recording_{timestamp}.mp4
     */
    private String generateRecordingPath(Long deviceId) {
        LocalDateTime now = LocalDateTime.now();
        String dateDir = String.format("%04d%02d%02d", 
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String fileName = String.format("recording_%d_%d.mp4", 
                System.currentTimeMillis(), deviceId);
        
        return String.format("%s/%d/%s/%s", recordingDirectory, deviceId, dateDir, fileName);
    }

    /**
     * 发送录像控制指令（通过物模型）
     * 
     * 说明：旧的 HTTP 直连 gateway 已移除，统一通过 DEVICE_SERVICE_INVOKE 发送给 newgateway。
     */
    private void sendRecordingCommand(Long deviceId, String command, Long recordingId,
                                      Integer duration, String filePath, String policy) {
        try {
            String prefer = "nvr";
            if (StrUtil.isNotBlank(policy)) {
                prefer = policy.toLowerCase();
            }

            String commandType;
            if ("start".equalsIgnoreCase(command)) {
                commandType = NvrDeviceTypeConstants.COMMAND_START_RECORDING;
            } else if ("stop".equalsIgnoreCase(command)) {
                commandType = NvrDeviceTypeConstants.COMMAND_STOP_RECORDING;
            } else {
                log.warn("[录像管理] 不支持的录像控制命令: deviceId={}, command={}", deviceId, command);
                return;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("recordingId", recordingId);
            params.put("prefer", prefer);
            if (duration != null) params.put("duration", duration);
            if (filePath != null) params.put("filePath", filePath);

            String requestId = deviceCommandPublisher.publishCommand(
                    NvrDeviceTypeConstants.NVR,
                    deviceId,
                    commandType,
                    params
            );

            log.info("[录像管理] 已发送录像控制命令到 newgateway: deviceId={}, commandType={}, recordingId={}, requestId={}",
                    deviceId, commandType, recordingId, requestId);
        } catch (Exception e) {
            log.error("[录像管理] 发送录像控制命令失败: deviceId={}, command={}", deviceId, command, e);
            throw new ServiceException(BAD_REQUEST.getCode(), "发送录像控制命令失败: " + e.getMessage());
        }
    }

}


