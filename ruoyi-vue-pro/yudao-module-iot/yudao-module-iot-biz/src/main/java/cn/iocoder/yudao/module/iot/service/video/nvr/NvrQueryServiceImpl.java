package cn.iocoder.yudao.module.iot.service.video.nvr;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsChannelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsChannelMapper;
import cn.iocoder.yudao.module.iot.enums.device.NvrDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.module.iot.service.ibms.device.support.IbmsDeviceLedgerRuntimeHelper;
import cn.iocoder.yudao.module.iot.service.video.IbmsDeviceVideoNetworkResolver;
import cn.iocoder.yudao.module.iot.service.video.nvr.dto.NvrScannedChannelRow;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;

/**
 * NVR 查询服务实现
 *
 * <p>提供 NVR 设备列表查询和通道刷新功能。</p>
 * <p>通道刷新通过 DeviceCommandPublisher 发送命令到 newgateway，替代原有的 HTTP 调用。</p>
 *
 * <p>Requirements: 11.1, 11.2, 11.3, 11.4, 11.5</p>
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class NvrQueryServiceImpl implements NvrQueryService {

    private static final long NVR_IBMS_PRODUCT_ID = 4L;

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;

    @Resource
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;

    @Resource
    private IbmsChannelMapper ibmsChannelMapper;

    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;

    @Override
    public List<IbmsDeviceDO> getNvrList() {
        List<IbmsDeviceDO> result = new ArrayList<>();
        Set<Long> addedIds = new HashSet<>();

        try {
            List<IbmsDeviceDO> byProduct = ibmsDeviceMapper.selectListByIbmsProductId(NVR_IBMS_PRODUCT_ID);
            if (!CollUtil.isEmpty(byProduct)) {
                for (IbmsDeviceDO device : byProduct) {
                    if (addedIds.add(device.getId())) {
                        result.add(device);
                    }
                }
            }

            List<IbmsDeviceDO> byRule = ibmsDeviceMapper.selectNvrLikeDevices();
            if (!CollUtil.isEmpty(byRule)) {
                for (IbmsDeviceDO device : byRule) {
                    if (addedIds.add(device.getId())) {
                        result.add(device);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[NVR] 查询NVR设备列表异常: {}", e.getMessage());
        }

        result.sort(Comparator
                .comparing((IbmsDeviceDO d) -> IbmsDeviceLedgerRuntimeHelper.parseGatewayRuntimeStateFromExtra(d.getExtra()),
                        Comparator.nullsLast(Integer::compareTo))
                .reversed()
                .thenComparing(IbmsDeviceDO::getId, Comparator.nullsLast(Long::compareTo)).reversed());

        log.info("[NVR] 查询到 {} 台NVR设备", result.size());
        return result;
    }

    @Override
    public List<NvrScannedChannelRow> getChannelsByNvrId(Long nvrId) {
        log.info("[NVR] 获取通道列表，直接从设备获取实时数据 nvrId={}", nvrId);
        return refreshChannelsByNvrId(nvrId);
    }

    @Override
    public List<NvrScannedChannelRow> refreshChannelsByNvrId(Long nvrId) {
        try {
            IbmsDeviceDO nvr = ibmsDeviceMapper.selectById(nvrId);
            if (nvr == null) {
                log.warn("[NVR] NVR 设备不存在: nvrId={}", nvrId);
                return new ArrayList<>();
            }

            IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeService.getByDeviceId(nvrId);
            IbmsDeviceVideoNetworkResolver.NetworkParams net = IbmsDeviceVideoNetworkResolver.resolve(nvr, runtime);

            String ip = StrUtil.blankToDefault(net.ip, null);
            Integer tcpPort = readTcpPort(runtime != null ? runtime.getConfig() : null);
            String username = StrUtil.blankToDefault(net.username, "admin");
            String password = StrUtil.blankToDefault(net.password, "admin123");

            Map<String, Object> params = new HashMap<>();
            params.put(NvrDeviceTypeConstants.PARAM_IP, ip);
            params.put("httpPort", net.httpPort);
            params.put("rtspPort", net.rtspPort);
            if (tcpPort != null) {
                params.put("tcpPort", tcpPort);
            }
            params.put(NvrDeviceTypeConstants.PARAM_USERNAME, username);
            params.put(NvrDeviceTypeConstants.PARAM_PASSWORD, password);

            String requestId = deviceCommandPublisher.publishCommand(
                    NvrDeviceTypeConstants.NVR,
                    nvrId,
                    NvrDeviceTypeConstants.COMMAND_SCAN_CHANNELS,
                    params
            );

            log.info("[NVR] 已发送扫描通道命令: nvrId={}, requestId={}, ip={}", nvrId, requestId, ip);

            LocalDateTime start = LocalDateTime.now();
            waitChannelSync(nvrId, start, 4000);
            return buildScannedChannelsFromDb(nvrId, ip);

        } catch (Exception e) {
            log.error("[NVR] 刷新通道异常 nvrId={}", nvrId, e);
            return new ArrayList<>();
        }
    }

    private static Integer readTcpPort(DeviceConfig config) {
        if (config == null) {
            return null;
        }
        try {
            Map<String, Object> configMap = config.toMap();
            Object tcpPortObj = configMap.get("tcpPort");
            if (tcpPortObj == null) {
                return null;
            }
            return tcpPortObj instanceof Number ? ((Number) tcpPortObj).intValue() : Integer.parseInt(tcpPortObj.toString());
        } catch (Exception ignore) {
            return null;
        }
    }

    private void waitChannelSync(Long nvrId, LocalDateTime start, long maxWaitMs) {
        long deadline = System.currentTimeMillis() + Math.max(0, maxWaitMs);
        while (System.currentTimeMillis() < deadline) {
            List<IbmsChannelDO> channels = ibmsChannelMapper.selectListByDeviceId(nvrId);
            boolean ok = false;
            for (IbmsChannelDO ch : channels) {
                String tc = ch.getTypeCode();
                if (StrUtil.isBlank(tc) || !tc.toUpperCase().startsWith("VT")) {
                    continue;
                }
                String extra = ch.getExtra();
                if (StrUtil.isBlank(extra)) {
                    continue;
                }
                try {
                    JSONObject ex = JSONUtil.parseObj(extra.trim());
                    String lastSyncTimeStr = ex.getStr("lastSyncTime");
                    if (StrUtil.isBlank(lastSyncTimeStr)) {
                        continue;
                    }
                    LocalDateTime lastSyncTime = LocalDateTime.parse(lastSyncTimeStr);
                    if (lastSyncTime.isAfter(start)) {
                        ok = true;
                        break;
                    }
                } catch (Exception ignore) {
                    // ignore parse errors
                }
            }
            if (ok) return;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private List<NvrScannedChannelRow> buildScannedChannelsFromDb(Long nvrId, String nvrIp) {
        List<IbmsChannelDO> channels = ibmsChannelMapper.selectListByDeviceId(nvrId);
        if (CollUtil.isEmpty(channels)) {
            return new ArrayList<>();
        }

        Long productId = 0L;
        IbmsDeviceDO nvr = ibmsDeviceMapper.selectById(nvrId);
        if (nvr != null && nvr.getIbmsProductId() != null) {
            productId = nvr.getIbmsProductId();
        }

        List<NvrScannedChannelRow> result = new ArrayList<>();
        for (IbmsChannelDO ch : channels) {
            // 兼容只处理 VT* 视频通道
            if (StrUtil.isBlank(ch.getTypeCode()) || !ch.getTypeCode().toUpperCase().startsWith("VT")) {
                continue;
            }

            JSONObject ex;
            try {
                ex = StrUtil.isBlank(ch.getExtra()) ? JSONUtil.createObj() :
                        JSONUtil.parseObj(ch.getExtra().trim());
            } catch (Exception ignore) {
                ex = JSONUtil.createObj();
            }

            String ip = StrUtil.isNotBlank(ex.getStr("targetIp")) ? ex.getStr("targetIp")
                    : (StrUtil.isNotBlank(nvrIp) ? nvrIp : null);

            Integer state;
            String status = ch.getStatus();
            if ("offline".equalsIgnoreCase(status)) {
                state = 0;
            } else if ("warning".equalsIgnoreCase(status)) {
                state = 2;
            } else {
                // online/armed/unknown -> online
                state = 1;
            }

            result.add(NvrScannedChannelRow.builder()
                    .syntheticId(nvrId * 1000L + (ch.getChannelNo() != null ? ch.getChannelNo() : 0))
                    .channelNo(ch.getChannelNo())
                    .channelName(ch.getName())
                    .state(state)
                    .productId(productId)
                    .ipAddress(ip)
                    .ptzSupport(ex.getBool("ptzSupport"))
                    .audioSupport(ex.getBool("audioSupport"))
                    .resolution(ex.getStr("resolution"))
                    .build());
        }
        return result;
    }

}
