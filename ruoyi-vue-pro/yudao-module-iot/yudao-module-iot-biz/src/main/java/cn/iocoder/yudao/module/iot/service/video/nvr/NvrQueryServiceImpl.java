package cn.iocoder.yudao.module.iot.service.video.nvr;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.channel.IotDeviceChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.device.NvrDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
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

    @Resource
    private IotDeviceMapper deviceMapper;

    private static final long NVR_PRODUCT_ID = 4L; // 按现场约定：productId=4 为 NVR

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotDeviceChannelMapper channelMapper;

    /**
     * 设备命令发布器（统一命令接口）
     * <p>Requirements: 11.1</p>
     */
    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;

    @Override
    public List<IotDeviceDO> getNvrList() {
        // 仅按产品ID=4 识别 NVR；租户与逻辑删除由全局拦截器处理
        List<IotDeviceDO> result = new ArrayList<>();
        try {
            List<IotDeviceDO> listByProduct = deviceMapper.selectListByProductId(NVR_PRODUCT_ID);
            if (!CollUtil.isEmpty(listByProduct)) {
                result.addAll(listByProduct);
            }
        } catch (Exception ignored) {}
        // 在线优先、按ID倒序
        result.sort(Comparator.comparing(IotDeviceDO::getState, Comparator.nullsLast(Integer::compareTo)).reversed()
                .thenComparing(IotDeviceDO::getId, Comparator.nullsLast(Long::compareTo)).reversed());
        return result;
    }

    @Override
    public List<IotDeviceDO> getChannelsByNvrId(Long nvrId) {
        // NVR通道不再存储在数据库中，直接调用刷新方法获取实时数据
        log.info("[NVR] 获取通道列表，直接从设备获取实时数据 nvrId={}", nvrId);
        return refreshChannelsByNvrId(nvrId);
    }

    @Override
    public List<IotDeviceDO> refreshChannelsByNvrId(Long nvrId) {
        try {
            IotDeviceDO nvr = deviceService.getDevice(nvrId);
            if (nvr == null) {
                log.warn("[NVR] NVR 设备不存在: nvrId={}", nvrId);
                return new ArrayList<>();
            }

            // 使用 DeviceConfigHelper 从 config 中获取 IP 地址
            String ip = DeviceConfigHelper.getIpAddress(nvr);
            Integer httpPort = null;
            Integer rtspPort = null;
            Integer tcpPort = null;  // SDK TCP 登录端口
            String username = null;
            String password = null;
            if (nvr.getConfig() != null) {
                try {
                    Map<String, Object> configMap = nvr.getConfig().toMap();
                    // 安全地获取端口值（可能是 Integer 或 Number）
                    Object httpPortObj = configMap.get("httpPort");
                    Object rtspPortObj = configMap.get("rtspPort");
                    Object tcpPortObj = configMap.get("tcpPort");
                    httpPort = httpPortObj != null ? ((Number) httpPortObj).intValue() : null;
                    rtspPort = rtspPortObj != null ? ((Number) rtspPortObj).intValue() : null;
                    tcpPort = tcpPortObj != null ? ((Number) tcpPortObj).intValue() : null;
                    username = configMap.get("username") != null ? configMap.get("username").toString() : null;
                    password = configMap.get("password") != null ? configMap.get("password").toString() : null;
                    // 兼容旧数据：如果 DeviceConfigHelper 没有获取到 IP，尝试从 config.ip 获取
                    if (StrUtil.isBlank(ip)) {
                        Object ipObj = configMap.get("ip");
                        ip = ipObj != null ? ipObj.toString() : null;
                    }
                } catch (Exception ignore) {}
            }
            // 兜底凭证
            if (StrUtil.isBlank(username)) username = "admin";
            if (StrUtil.isBlank(password)) password = "admin123";

            // 构建命令参数
            Map<String, Object> params = new HashMap<>();
            params.put(NvrDeviceTypeConstants.PARAM_IP, ip);
            if (httpPort != null) params.put("httpPort", httpPort);
            if (rtspPort != null) params.put("rtspPort", rtspPort);
            if (tcpPort != null) params.put("tcpPort", tcpPort);
            params.put(NvrDeviceTypeConstants.PARAM_USERNAME, username);
            params.put(NvrDeviceTypeConstants.PARAM_PASSWORD, password);

            // 通过统一命令接口发送扫描通道命令
            // Requirements: 11.1, 11.3
            String requestId = deviceCommandPublisher.publishCommand(
                    NvrDeviceTypeConstants.NVR,
                    nvrId,
                    NvrDeviceTypeConstants.COMMAND_SCAN_CHANNELS,
                    params
            );

            log.info("[NVR] 已发送扫描通道命令: nvrId={}, requestId={}, ip={}", nvrId, requestId, ip);

            // 通过 DEVICE_SERVICE_RESULT 的消费者落库后，从数据库读取并返回（用于兼容同步场景）
            LocalDateTime start = LocalDateTime.now();
            waitChannelSync(nvrId, start, 4000);
            return buildChannelDevicesFromDb(nvrId, ip);
            
        } catch (Exception e) {
            log.error("[NVR] 刷新通道异常 nvrId={}", nvrId, e);
            return new ArrayList<>();
        }
    }

    private void waitChannelSync(Long nvrId, LocalDateTime start, long maxWaitMs) {
        long deadline = System.currentTimeMillis() + Math.max(0, maxWaitMs);
        while (System.currentTimeMillis() < deadline) {
            List<IotDeviceChannelDO> channels = channelMapper.selectListByDeviceId(nvrId);
            boolean ok = channels.stream().anyMatch(ch -> ch.getLastSyncTime() != null && ch.getLastSyncTime().isAfter(start));
            if (ok) {
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private List<IotDeviceDO> buildChannelDevicesFromDb(Long nvrId, String nvrIp) {
        List<IotDeviceChannelDO> channels = channelMapper.selectListByDeviceId(nvrId);
        if (CollUtil.isEmpty(channels)) {
            return new ArrayList<>();
        }

        List<IotDeviceDO> result = new ArrayList<>();
        for (IotDeviceChannelDO ch : channels) {
            IotDeviceDO d = new IotDeviceDO();
            d.setId(nvrId * 1000 + (ch.getChannelNo() != null ? ch.getChannelNo() : 0));
            d.setDeviceName(ch.getChannelName());
            d.setState(ch.getOnlineStatus());
            d.setGatewayId(nvrId);
            d.setProductId(ch.getProductId());

            GenericDeviceConfig cfg = new GenericDeviceConfig();
            cfg.set("channel", ch.getChannelNo());
            cfg.set("channelName", ch.getChannelName());
            cfg.set("online", ch.getOnlineStatus() != null && ch.getOnlineStatus() == 1);
            if (StrUtil.isNotBlank(ch.getTargetIp())) {
                cfg.set("ipAddress", ch.getTargetIp());
            } else if (StrUtil.isNotBlank(nvrIp)) {
                cfg.set("ipAddress", nvrIp);
            }
            cfg.set("ptzSupport", ch.getPtzSupport());
            cfg.set("audioSupport", ch.getAudioSupport());
            cfg.set("resolution", ch.getResolution());
            d.setConfig(cfg);

            result.add(d);
        }
        return result;
    }

}
