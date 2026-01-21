package cn.iocoder.yudao.module.iot.job;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.device.CapabilityRefreshDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.enums.device.NvrDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.service.access.IotAccessDeviceCapabilityService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.video.nvr.NvrQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 门禁设备能力刷新定时任务
 *
 * <p>周期性调用网关 QUERY_DEVICE_CAPABILITY，并将能力快照写回 iot_device.config，
 * 供业务侧按能力做精准下发/撤销，避免“默认全支持”的一窝熟行为。</p>
 *
 * <p>目前按租户 1 执行（与现有 NVR/视频任务一致），后续如需多租户可遍历租户列表。</p>
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "iot.access.capability-refresh", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AccessDeviceCapabilityRefreshJob {

    @Resource
    private IotDeviceMapper deviceMapper;

    @Resource
    private IotAccessDeviceCapabilityService capabilityService;

    @Resource
    private NvrQueryService nvrQueryService;

    @Resource
    private IotDeviceService iotDeviceService;

    /**
     * 刷新间隔（毫秒），默认 10 分钟
     */
    @Value("${iot.access.capability-refresh.interval:600000}")
    private long intervalMs;

    /**
     * 能力 TTL（分钟），默认 24 小时
     */
    @Value("${iot.access.capability-refresh.ttl-minutes:1440}")
    private long ttlMinutes;

    /**
     * 每次任务最大刷新数量（避免大批量阻塞）
     */
    @Value("${iot.access.capability-refresh.max-per-run:50}")
    private int maxPerRun;

    @Scheduled(fixedDelayString = "${iot.access.capability-refresh.interval:600000}", initialDelay = 15000)
    public void refreshOnlineAccessCapabilities() {
        // TODO: 当前使用默认租户ID=1，未来如需支持多租户，需要遍历所有租户
        TenantUtils.execute(1L, () -> {
            try {
                int refreshed = 0;
                LocalDateTime now = LocalDateTime.now();
                Duration ttl = Duration.ofMinutes(ttlMinutes);

                // 仅刷新“在线”的门禁设备，避免定时任务主动把离线设备拉起登录
                for (IotDeviceDO device : deviceMapper.selectList()) {
                    if (refreshed >= maxPerRun) {
                        break;
                    }
                    if (device == null || device.getId() == null) {
                        continue;
                    }
                    if (!IotDeviceStateEnum.ONLINE.getState().equals(device.getState())) {
                        continue;
                    }

                    // 根据插件 deviceType 标识是否需要刷新能力（门禁一/二代、NVR；排除报警主机、长辉 TCP）
                    String pluginDeviceType = resolvePluginDeviceType(device);
                    if (!CapabilityRefreshDeviceTypeConstants.isCapabilityRefreshEnabled(pluginDeviceType)) {
                        continue;
                    }

                    LocalDateTime capTime = extractCapabilityTime(device);
                    if (capTime != null && Duration.between(capTime, now).compareTo(ttl) < 0) {
                        continue; // 未过期
                    }

                    try {
                        Long tenantId = device.getTenantId() != null ? device.getTenantId() : 1L;
                        // 分设备类型执行不同刷新策略
                        if (NvrDeviceTypeConstants.NVR.equalsIgnoreCase(pluginDeviceType)) {
                            TenantUtils.execute(tenantId, () -> refreshNvrCapability(device));
                        } else {
                            TenantUtils.execute(tenantId, () -> capabilityService.refreshCapability(device.getId()));
                        }
                        refreshed++;
                    } catch (Exception e) {
                        log.warn("[AccessCapabilityRefreshJob] 刷新能力失败: deviceId={}, error={}",
                                device.getId(), e.getMessage());
                    }
                }

                if (refreshed > 0) {
                    log.info("[AccessCapabilityRefreshJob] 本次刷新完成: refreshed={}, ttlMinutes={}, maxPerRun={}",
                            refreshed, ttlMinutes, maxPerRun);
                }
            } catch (Exception e) {
                log.error("[AccessCapabilityRefreshJob] 任务执行异常: {}", e.getMessage(), e);
            }
        });
    }

    private LocalDateTime extractCapabilityTime(IotDeviceDO device) {
        if (device == null || device.getConfig() == null) {
            return null;
        }
        if (device.getConfig() instanceof AccessDeviceConfig) {
            return ((AccessDeviceConfig) device.getConfig()).getCapabilityTime();
        }
        if (device.getConfig() instanceof GenericDeviceConfig) {
            Object capsObj = ((GenericDeviceConfig) device.getConfig()).get("accessCapabilities");
            if (capsObj instanceof Map<?, ?>) {
                Object updatedAt = ((Map<?, ?>) capsObj).get("updatedAt");
                if (updatedAt != null) {
                    try {
                        return LocalDateTime.parse(String.valueOf(updatedAt));
                    } catch (Exception ignore) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 解析 newgateway 插件 deviceType（用于能力刷新策略选择）
     *
     * <p>优先取 config.accessCapabilities.deviceType（refreshCapability 会写回），其次取 GenericDeviceConfig.deviceType。</p>
     */
    private String resolvePluginDeviceType(IotDeviceDO device) {
        if (device == null || device.getConfig() == null) {
            return null;
        }
        // 1) accessCapabilities.deviceType（Access/Generic 都可能写）
        Object capsObj = null;
        if (device.getConfig() instanceof AccessDeviceConfig) {
            capsObj = ((AccessDeviceConfig) device.getConfig()).getAccessCapabilities();
        } else if (device.getConfig() instanceof GenericDeviceConfig) {
            capsObj = ((GenericDeviceConfig) device.getConfig()).get("accessCapabilities");
        }
        if (capsObj instanceof Map<?, ?>) {
            Object dt = ((Map<?, ?>) capsObj).get("deviceType");
            if (dt != null) {
                return String.valueOf(dt);
            }
        }
        // 2) GenericDeviceConfig.deviceType（NVR/ACCESS_GENx 通常在这里）
        if (device.getConfig() instanceof GenericDeviceConfig) {
            return ((GenericDeviceConfig) device.getConfig()).getDeviceType();
        }
        return null;
    }

    private void refreshNvrCapability(IotDeviceDO nvr) {
        if (nvr == null || nvr.getId() == null) {
            return;
        }
        // 复用现有 NVR 通道刷新逻辑（会触发 newgateway 扫描通道并同步到 DB）
        List<IotDeviceDO> channels = nvrQueryService.refreshChannelsByNvrId(nvr.getId());

        // 写回一份“能力/概览快照”到 iot_device.config，供业务侧快速判断（例如通道数、更新时间）
        if (nvr.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) nvr.getConfig();
            Map<String, Object> snap = new HashMap<>();
            snap.put("deviceType", NvrDeviceTypeConstants.NVR);
            snap.put("channelCount", channels != null ? channels.size() : 0);
            snap.put("updatedAt", LocalDateTime.now().toString());
            cfg.set("nvrCapabilities", snap);
            iotDeviceService.updateDeviceConfig(nvr.getId(), cfg);
        }
    }
}

