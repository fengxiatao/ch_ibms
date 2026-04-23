package cn.iocoder.yudao.module.iot.service.ibms.device;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.api.device.support.IbmsDeviceGatewayDtoMapper;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceRuntimeMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsProductMapper;
import cn.iocoder.yudao.module.iot.service.ibms.product.IbmsProductExtraHelper;
import cn.iocoder.yudao.module.iot.service.ibms.product.IbmsProductService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IBMS 台账 + 产品 extra 组装网关 DTO。
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class IbmsDeviceGatewaySupportServiceImpl implements IbmsDeviceGatewaySupportService {

    private final IbmsDeviceMapper ibmsDeviceMapper;
    private final IbmsDeviceRuntimeMapper ibmsDeviceRuntimeMapper;
    private final IbmsProductMapper ibmsProductMapper;
    private final IbmsProductService ibmsProductService;
    private final IbmsDeviceRuntimeService ibmsDeviceRuntimeService;

    @Override
    @TenantIgnore
    public IotDeviceRespDTO getGatewayDevice(IotDeviceGetReqDTO req) {
        if (req == null) {
            return null;
        }
        IbmsDeviceDO d = null;
        if (req.getId() != null) {
            d = ibmsDeviceMapper.selectById(req.getId());
        } else if (StrUtil.isNotBlank(req.getDeviceKey())) {
            d = ibmsDeviceMapper.selectByGatewayDeviceKey(req.getDeviceKey());
        } else if (StrUtil.isNotBlank(req.getProductKey()) && StrUtil.isNotBlank(req.getDeviceName())) {
            d = ibmsDeviceMapper.selectByProductKeyAndName(req.getProductKey(), req.getDeviceName());
        }
        if (d == null) {
            log.debug("[getGatewayDevice] 未找到 IBMS 设备: {}", req);
            return null;
        }
        return toDtoWithProduct(d);
    }

    @Override
    @TenantIgnore
    public boolean authDevice(IotDeviceAuthReqDTO authReqDTO) {
        IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(authReqDTO.getUsername());
        if (deviceInfo == null) {
            log.error("[authDevice][认证失败，username({}) 格式不正确]", authReqDTO.getUsername());
            return false;
        }
        IbmsDeviceDO device = ibmsDeviceMapper.selectByProductKeyAndName(
                deviceInfo.getProductKey(), deviceInfo.getDeviceName());
        if (device == null) {
            log.warn("[authDevice][设备({}/{}) 不存在]", deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            return false;
        }
        String secret = readDeviceSecret(device);
        if (StrUtil.isBlank(secret)) {
            log.warn("[authDevice][设备({}/{}) 未配置 deviceSecret]", deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            return false;
        }
        IotDeviceAuthUtils.AuthInfo authInfo = IotDeviceAuthUtils.getAuthInfo(
                deviceInfo.getProductKey(), deviceInfo.getDeviceName(), secret);
        if (ObjUtil.notEqual(authInfo.getPassword(), authReqDTO.getPassword())) {
            log.error("[authDevice][设备({}/{}) 密码不正确]", deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            return false;
        }
        return true;
    }

    @Override
    @TenantIgnore
    public List<IotDeviceRespDTO> listOnlineDevices() {
        // 在线集合：extra.gatewayRuntimeState（网关观测） ∪ ibms_device_runtime.state（台账运行态，含自 iot 迁入行）
        Map<Long, IbmsDeviceDO> byId = new LinkedHashMap<>();
        for (IbmsDeviceDO d : ibmsDeviceMapper.selectListByGatewayRuntimeState(IotDeviceStateEnum.ONLINE.getState())) {
            if (d != null && d.getId() != null) {
                byId.put(d.getId(), d);
            }
        }
        for (Long deviceId : ibmsDeviceRuntimeMapper.selectOnlineDeviceIds()) {
            if (deviceId == null || byId.containsKey(deviceId)) {
                continue;
            }
            IbmsDeviceDO d = ibmsDeviceMapper.selectById(deviceId);
            if (d != null) {
                byId.put(deviceId, d);
            }
        }
        return byId.values().stream().map(this::toDtoWithProductForGatewayOnline).collect(Collectors.toList());
    }

    @Override
    @TenantIgnore
    public List<IotDeviceRespDTO> listAccessDevices() {
        List<IbmsDeviceDO> list = ibmsDeviceMapper.selectListAccessLikeDevices();
        return list.stream().map(this::toDtoWithProduct).collect(Collectors.toList());
    }

    @Override
    @TenantIgnore
    public List<IotDeviceRespDTO> listAllDevices() {
        List<IbmsDeviceDO> list = ibmsDeviceMapper.selectList();
        return list.stream().map(this::toDtoWithProduct).collect(Collectors.toList());
    }

    @Override
    @TenantIgnore
    public List<IotDeviceRespDTO> listDevicesForTenant(Long tenantId) {
        if (tenantId == null) {
            return new ArrayList<>();
        }
        List<IotDeviceRespDTO> out = new ArrayList<>();
        TenantUtils.execute(tenantId, () -> {
            List<IbmsDeviceDO> list = ibmsDeviceMapper.selectList();
            for (IbmsDeviceDO d : list) {
                out.add(toDtoWithProduct(d));
            }
        });
        return out;
    }

    @Override
    @TenantIgnore
    public int batchUpdateGatewayRuntimeState(List<Long> deviceIds, Integer state, String reason) {
        if (deviceIds == null || deviceIds.isEmpty() || state == null) {
            return 0;
        }
        long ts = System.currentTimeMillis();
        int n = 0;
        for (Long id : deviceIds) {
            if (id == null) {
                continue;
            }
            IbmsDeviceDO d = ibmsDeviceMapper.selectById(id);
            if (d == null) {
                continue;
            }
            JSONObject cfg = parseExtraObject(d.getExtra());
            cfg.set("gatewayRuntimeState", state);
            cfg.set("gatewayRuntimeAt", ts);
            if (StrUtil.isNotBlank(reason)) {
                cfg.set("gatewayRuntimeReason", reason.trim());
            }
            ibmsDeviceMapper.update(null, new LambdaUpdateWrapper<IbmsDeviceDO>()
                    .eq(IbmsDeviceDO::getId, id)
                    .set(IbmsDeviceDO::getExtra, cfg.toString()));
            n++;
        }
        return n;
    }

    @Override
    @TenantIgnore
    public void updateGatewayDeviceStateWithTimestamp(Long deviceId, Integer newState, Long timestampMillis) {
        if (deviceId == null) {
            return;
        }
        IbmsDeviceDO ibms = ibmsDeviceMapper.selectById(deviceId);
        if (ibms == null) {
            log.debug("[updateGatewayDeviceStateWithTimestamp] 无 IBMS 台账，跳过: deviceId={}", deviceId);
            return;
        }
        patchIbmsGatewayRuntimeState(ibms.getId(), ibms.getExtra(), newState, timestampMillis);
        ibmsDeviceRuntimeService.patchGatewayState(deviceId, ibms.getTenantId(), newState, timestampMillis);
        log.debug("[updateGatewayDeviceStateWithTimestamp] deviceId={}, newState={}", deviceId, newState);
    }

    /**
     * 网关运行态写入 ibms_device.extra（与业务接入 JSON 合并），键：gatewayRuntimeState / gatewayRuntimeAt
     */
    private void patchIbmsGatewayRuntimeState(Long deviceId, String extraJson, Integer newState, Long timestamp) {
        JSONObject cfg;
        try {
            cfg = StrUtil.isBlank(extraJson) ? new JSONObject() : JSONUtil.parseObj(extraJson.trim());
        } catch (Exception e) {
            cfg = new JSONObject();
        }
        cfg.set("gatewayRuntimeState", newState);
        cfg.set("gatewayRuntimeAt", timestamp != null ? timestamp : System.currentTimeMillis());
        ibmsDeviceMapper.update(null, new LambdaUpdateWrapper<IbmsDeviceDO>()
                .eq(IbmsDeviceDO::getId, deviceId)
                .set(IbmsDeviceDO::getExtra, cfg.toString()));
    }

    /**
     * 网关「全量在线」列表：在台账 DTO 上合并 {@code ibms_device_runtime.config}（与自 iot 迁入设备对齐）。
     */
    private IotDeviceRespDTO toDtoWithProductForGatewayOnline(IbmsDeviceDO d) {
        IotDeviceRespDTO dto = toDtoWithProduct(d);
        mergeRuntimeConfigIntoGatewayDto(d.getId(), dto);
        return dto;
    }

    private void mergeRuntimeConfigIntoGatewayDto(Long deviceId, IotDeviceRespDTO dto) {
        if (deviceId == null || dto == null) {
            return;
        }
        IbmsDeviceRuntimeDO r = ibmsDeviceRuntimeService.getByDeviceId(deviceId);
        if (r == null || r.getConfig() == null) {
            return;
        }
        String fromRuntime = DeviceConfigHelper.toJson(r.getConfig());
        if (StrUtil.isBlank(fromRuntime) || "{}".equals(fromRuntime.trim())) {
            return;
        }
        JSONObject base = StrUtil.isBlank(dto.getConfig()) ? new JSONObject() : JSONUtil.parseObj(dto.getConfig());
        JSONObject overlay = JSONUtil.parseObj(fromRuntime);
        for (String k : overlay.keySet()) {
            base.set(k, overlay.get(k));
        }
        dto.setConfig(base.toString());
    }

    private IotDeviceRespDTO toDtoWithProduct(IbmsDeviceDO d) {
        IotDeviceRespDTO dto = IbmsDeviceGatewayDtoMapper.toGatewayDto(d);
        IbmsProductRespVO template = ibmsProductService.getProductTemplateForDevice(
                d.getGroupCode(), d.getSystemCode(), d.getDeviceTypeCode(), d.getProductModel());
        if (template != null && template.getId() != null) {
            dto.setProductId(template.getId());
            IbmsProductDO p = ibmsProductMapper.selectById(template.getId());
            String codec = IbmsProductExtraHelper.getCodecTypeOrNull(p);
            if (StrUtil.isNotBlank(codec)) {
                dto.setCodecType(codec);
            }
        }
        return dto;
    }

    private static String readDeviceSecret(IbmsDeviceDO d) {
        JSONObject cfg = parseExtraObject(d.getExtra());
        String s = cfg.getStr("deviceSecret");
        return StrUtil.trimToNull(s);
    }

    private static JSONObject parseExtraObject(String extra) {
        if (StrUtil.isBlank(extra)) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(extra.trim());
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
