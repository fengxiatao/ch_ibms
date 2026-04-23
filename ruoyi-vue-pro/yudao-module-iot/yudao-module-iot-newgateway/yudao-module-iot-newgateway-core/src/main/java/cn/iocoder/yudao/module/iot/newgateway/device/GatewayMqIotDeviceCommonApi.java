package cn.iocoder.yudao.module.iot.newgateway.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 网关侧设备查询：仅使用本地缓存（Biz 推送）+ MQ Request-Reply 补快照，不调用 Biz HTTP。
 */
@Slf4j
@Service("iotDeviceCommonApiImpl")
@RequiredArgsConstructor
public class GatewayMqIotDeviceCommonApi implements IotDeviceCommonApi {

    private final GatewayDeviceProfileCache cache;
    private final GatewayDeviceSnapshotClient snapshotClient;

    @Override
    public CommonResult<Boolean> authDevice(IotDeviceAuthReqDTO authReqDTO) {
        return success(Boolean.FALSE);
    }

    @Override
    public CommonResult<IotDeviceRespDTO> getDevice(IotDeviceGetReqDTO getReqDTO) {
        if (getReqDTO == null || getReqDTO.getId() == null) {
            log.debug("[GatewayMqIotDeviceCommonApi] getDevice 需要 id（MQ-only 基线）");
            return success(null);
        }
        Long id = getReqDTO.getId();
        IotDeviceRespDTO cached = cache.get(id);
        if (cached != null) {
            return success(cached);
        }
        try {
            IotDeviceRespDTO fetched = snapshotClient.fetchSnapshot(id, null, Duration.ofSeconds(30));
            if (fetched != null) {
                cache.upsert(fetched);
            }
            return success(fetched);
        } catch (Exception e) {
            log.warn("[GatewayMqIotDeviceCommonApi] MQ 快照失败 deviceId={} err={}", id, e.toString());
            return success(null);
        }
    }

    @Override
    public CommonResult<List<IotDeviceRespDTO>> getOnlineDevices() {
        // 缓存不含 Biz 在线态：启动初始化请依赖 profile 推送或租户列表
        return success(new ArrayList<>());
    }

    @Override
    public CommonResult<List<IotDeviceRespDTO>> getAccessDevices() {
        List<IotDeviceRespDTO> list = cache.listAll().stream()
                .filter(d -> {
                    String t = d.getDeviceType();
                    if (t == null) {
                        return false;
                    }
                    String u = t.toUpperCase();
                    return u.contains("ACCESS");
                })
                .collect(Collectors.toList());
        return success(list);
    }

    @Override
    public CommonResult<List<IotDeviceRespDTO>> getAllDevices() {
        return success(cache.listAll());
    }

    @Override
    public CommonResult<List<IotDeviceRespDTO>> getDevicesByTenantId(Long tenantId) {
        return success(cache.listByTenant(tenantId));
    }

    @Override
    public CommonResult<Integer> batchUpdateDeviceState(List<Long> deviceIds, Integer state, String reason) {
        log.debug("[GatewayMqIotDeviceCommonApi] 忽略 batchUpdateDeviceState（由 MQ 状态通道处理）");
        return success(0);
    }
}
