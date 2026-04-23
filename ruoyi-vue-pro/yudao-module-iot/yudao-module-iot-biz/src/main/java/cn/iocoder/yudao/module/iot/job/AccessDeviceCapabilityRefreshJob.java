package cn.iocoder.yudao.module.iot.job;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.enums.device.CapabilityRefreshDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.enums.device.NvrDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.service.access.IotAccessDeviceCapabilityService;
import cn.iocoder.yudao.module.iot.service.video.nvr.NvrQueryService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 门禁 / NVR 等设备能力刷新定时任务
 *
 * <p>周期性调用网关 QUERY_DEVICE_CAPABILITY（门禁）或触发 NVR 通道扫描，将能力快照写回台账
 * {@code ibms_device.extra}（{@code accessCapabilities} / {@code nvrCapabilities}），供业务侧精准下发。</p>
 *
 * <p>迭代说明：由遍历 {@code iot_device} 改为以 {@code ibms_device} + {@code extra.gatewayRuntimeState}
 * 在线为准，与 IBMS 主数据收敛一致。</p>
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "iot.access.capability-refresh", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AccessDeviceCapabilityRefreshJob {

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;

    @Resource
    private IotAccessDeviceCapabilityService capabilityService;

    @Resource
    private NvrQueryService nvrQueryService;

    @Value("${iot.access.capability-refresh.ttl-minutes:1440}")
    private long ttlMinutes;

    @Value("${iot.access.capability-refresh.max-per-run:50}")
    private int maxPerRun;

    @Scheduled(fixedDelayString = "${iot.access.capability-refresh.interval:600000}", initialDelay = 15000)
    public void refreshOnlineAccessCapabilities() {
        TenantUtils.execute(1L, () -> {
            try {
                int refreshed = 0;
                LocalDateTime now = LocalDateTime.now();
                Duration ttl = Duration.ofMinutes(ttlMinutes);

                List<IbmsDeviceDO> onlineList = ibmsDeviceMapper
                        .selectListByGatewayRuntimeState(IotDeviceStateEnum.ONLINE.getState());
                if (onlineList == null) {
                    return;
                }
                for (IbmsDeviceDO ibms : onlineList) {
                    if (refreshed >= maxPerRun) {
                        break;
                    }
                    if (ibms == null || ibms.getId() == null) {
                        continue;
                    }

                    String pluginDeviceType = resolvePluginDeviceTypeFromIbms(ibms);
                    if (!CapabilityRefreshDeviceTypeConstants.isCapabilityRefreshEnabled(pluginDeviceType)) {
                        continue;
                    }

                    LocalDateTime capTime = extractCapabilityTimeFromIbms(ibms);
                    if (capTime != null && Duration.between(capTime, now).compareTo(ttl) < 0) {
                        continue;
                    }

                    try {
                        Long tenantId = ibms.getTenantId() != null ? ibms.getTenantId() : 1L;
                        if (NvrDeviceTypeConstants.NVR.equalsIgnoreCase(pluginDeviceType)) {
                            TenantUtils.execute(tenantId, () -> refreshNvrCapabilityIbms(ibms));
                        } else {
                            TenantUtils.execute(tenantId, () -> capabilityService.refreshCapability(ibms.getId()));
                        }
                        refreshed++;
                    } catch (Exception e) {
                        log.warn("[AccessCapabilityRefreshJob] 刷新能力失败: deviceId={}, error={}",
                                ibms.getId(), e.getMessage());
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

    private LocalDateTime extractCapabilityTimeFromIbms(IbmsDeviceDO device) {
        if (device == null || StrUtil.isBlank(device.getExtra())) {
            return null;
        }
        try {
            JSONObject o = JSONUtil.parseObj(device.getExtra().trim());
            Object ac = o.get("accessCapabilities");
            if (ac instanceof JSONObject) {
                Object u = ((JSONObject) ac).get("updatedAt");
                if (u != null) {
                    return LocalDateTime.parse(String.valueOf(u));
                }
            }
            Object nv = o.get("nvrCapabilities");
            if (nv instanceof JSONObject) {
                Object u = ((JSONObject) nv).get("updatedAt");
                if (u != null) {
                    return LocalDateTime.parse(String.valueOf(u));
                }
            }
        } catch (Exception ignore) {
            return null;
        }
        return null;
    }

    private String resolvePluginDeviceTypeFromIbms(IbmsDeviceDO d) {
        if (d == null) {
            return null;
        }
        if (StrUtil.isNotBlank(d.getExtra())) {
            try {
                JSONObject o = JSONUtil.parseObj(d.getExtra().trim());
                Object caps = o.get("accessCapabilities");
                if (caps instanceof JSONObject) {
                    Object dt = ((JSONObject) caps).get("deviceType");
                    if (dt != null) {
                        return String.valueOf(dt);
                    }
                }
                String dt2 = o.getStr("deviceType");
                if (StrUtil.isNotBlank(dt2)) {
                    return dt2;
                }
            } catch (Exception ignored) {
                // fall through
            }
        }
        if ("NVR".equals(d.getDeviceTypeCode()) || Objects.equals(d.getIbmsProductId(), 4L)) {
            return NvrDeviceTypeConstants.NVR;
        }
        return AccessDeviceTypeConstants.getAccessDeviceType(d);
    }

    private void refreshNvrCapabilityIbms(IbmsDeviceDO nvr) {
        if (nvr == null || nvr.getId() == null) {
            return;
        }
        var channels = nvrQueryService.refreshChannelsByNvrId(nvr.getId());

        JSONObject ex = JSONUtil.parseObj(StrUtil.blankToDefault(nvr.getExtra(), "{}"));
        Map<String, Object> snap = new HashMap<>();
        snap.put("deviceType", NvrDeviceTypeConstants.NVR);
        snap.put("channelCount", channels != null ? channels.size() : 0);
        snap.put("updatedAt", LocalDateTime.now().toString());
        ex.set("nvrCapabilities", new JSONObject(snap));

        ibmsDeviceMapper.update(null, new LambdaUpdateWrapper<IbmsDeviceDO>()
                .eq(IbmsDeviceDO::getId, nvr.getId())
                .set(IbmsDeviceDO::getExtra, ex.toString()));
    }
}
