package cn.iocoder.yudao.module.iot.service.device.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceImportExcelVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceImportRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceGroupDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceRuntimeMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsProductMapper;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceGroupService;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 遗留 Excel/legacy VO 导入适配器：把 legacy 创建/更新收口为仅写 {@code ibms_device}/{@code ibms_device_runtime}。
 *
 * <p>用于 G2：移除 {@code IotDeviceServiceImpl} 依赖。</p>
 */
@Service
@Slf4j
@Validated
public class IbmsLegacyIotDeviceAdapterService {

    @Resource
    private IotProductService productService;

    @Resource
    private IotDeviceGroupService deviceGroupService;

    @Resource
    private IbmsDeviceService ibmsDeviceService;

    @Resource
    private IbmsProductMapper ibmsProductMapper;

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;

    @Resource
    private IbmsDeviceRuntimeMapper ibmsDeviceRuntimeMapper;

    @Resource
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;

    @Transactional(rollbackFor = Exception.class)
    public Long createDevice(IotDeviceSaveReqVO createReqVO) {
        // 1. 校验产品是否存在
        IotProductDO product = productService.getProduct(createReqVO.getProductId());
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }

        // 2. 统一校验（包含楼层唯一性和 DXF 绑定校验）
        validateCreateDeviceParam(product.getProductKey(), createReqVO.getDeviceName(),
                createReqVO.getGatewayId(), createReqVO.getFloorId(),
                createReqVO.getDxfEntityId(), product);

        // 3. 校验分组存在
        deviceGroupService.validateDeviceGroupExists(createReqVO.getGroupIds());

        // 4. 校验设备序列号全局唯一
        validateSerialNumberUnique(createReqVO.getSerialNumber(), null);

        // 5. 单台账：仅写入 ibms_device / ibms_device_runtime
        IbmsDeviceSaveReqVO ibmsReq = buildIbmsDeviceSaveForLegacyCreate(product, createReqVO);
        Long id = ibmsDeviceService.createDevice(ibmsReq);
        applyLegacyFieldsAfterIbmsCreate(id, createReqVO, product);
        return id;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDevice(IotDeviceSaveReqVO updateReqVO) {
        // legacy update 仅允许补丁 ibms 台账与运行态字段
        updateReqVO.setDeviceName(null).setProductId(null);
        Long id = updateReqVO.getId();

        IbmsDeviceDO ibmsOnly = ibmsDeviceMapper.selectById(id);
        if (ibmsOnly == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 校验父设备是否为合法网关
        if (IotProductDeviceTypeEnum.isGatewaySub(ibmsOnly.getDeviceType())
                && updateReqVO.getGatewayId() != null) {
            validateGatewayDeviceExists(updateReqVO.getGatewayId());
        }

        // 校验分组存在
        deviceGroupService.validateDeviceGroupExists(updateReqVO.getGroupIds());

        // 校验设备序列号全局唯一
        validateSerialNumberUnique(updateReqVO.getSerialNumber(), id);

        patchIbmsDeviceFromLegacyUpdate(updateReqVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long id) {
        ibmsDeviceService.deleteDevice(id);
    }

    public void updateDeviceConfig(Long deviceId, DeviceConfig config) {
        ibmsDeviceRuntimeService.saveRuntimeConfig(deviceId, config);
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDeviceImportRespVO importDevice(List<IotDeviceImportExcelVO> importDevices, boolean updateSupport) {
        if (CollUtil.isEmpty(importDevices)) {
            throw exception(DEVICE_IMPORT_LIST_IS_EMPTY);
        }

        IotDeviceImportRespVO respVO = IotDeviceImportRespVO.builder()
                .createDeviceNames(new ArrayList<>())
                .updateDeviceNames(new ArrayList<>())
                .failureDeviceNames(new LinkedHashMap<>())
                .build();

        importDevices.forEach(importDevice -> {
            try {
                // 参数校验
                try {
                    ValidationUtils.validate(importDevice);
                } catch (ConstraintViolationException ex) {
                    respVO.getFailureDeviceNames().put(importDevice.getDeviceName(), ex.getMessage());
                    return;
                }

                // 校验产品是否存在
                IotProductDO product = productService.validateProductExists(importDevice.getProductKey());

                // 校验父设备是否存在
                Long gatewayId = null;
                if (StrUtil.isNotEmpty(importDevice.getParentDeviceName())) {
                    IbmsDeviceDO gatewayDevice = ibmsDeviceMapper.selectByDeviceName(importDevice.getParentDeviceName());
                    if (gatewayDevice == null) {
                        throw exception(DEVICE_GATEWAY_NOT_EXISTS);
                    }
                    if (!IotProductDeviceTypeEnum.isGateway(gatewayDevice.getDeviceType())) {
                        throw exception(DEVICE_NOT_GATEWAY);
                    }
                    gatewayId = gatewayDevice.getId();
                }

                // 校验设备分组是否存在
                Set<Long> groupIds = new HashSet<>();
                if (StrUtil.isNotEmpty(importDevice.getGroupNames())) {
                    String[] groupNames = importDevice.getGroupNames().split(",");
                    for (String groupName : groupNames) {
                        IotDeviceGroupDO group = deviceGroupService.getDeviceGroupByName(groupName);
                        if (group == null) {
                            throw exception(DEVICE_GROUP_NOT_EXISTS);
                        }
                        groupIds.add(group.getId());
                    }
                }

                // 设备是否存在（单台账：仅 ibms_device）
                IbmsDeviceDO existDevice = ibmsDeviceMapper.selectByProductKeyAndDeviceName(
                        product.getProductKey(), importDevice.getDeviceName());
                if (existDevice == null) {
                    createDevice(new IotDeviceSaveReqVO()
                            .setDeviceName(importDevice.getDeviceName())
                            .setProductId(product.getId()).setGatewayId(gatewayId).setGroupIds(groupIds)
                            .setLocationType(importDevice.getLocationType()));
                    respVO.getCreateDeviceNames().add(importDevice.getDeviceName());
                    return;
                }

                // 已存在：按 updateSupport 决定是否允许更新
                if (updateSupport) {
                    throw exception(DEVICE_KEY_EXISTS);
                }

                patchIbmsDeviceFromLegacyImport(existDevice.getId(), gatewayId, groupIds, importDevice.getLocationType());
                respVO.getUpdateDeviceNames().add(importDevice.getDeviceName());
            } catch (ServiceException ex) {
                respVO.getFailureDeviceNames().put(importDevice.getDeviceName(), ex.getMessage());
            }
        });
        return respVO;
    }

    private void validateCreateDeviceParam(String productKey, String deviceName,
                                             Long gatewayId, Long floorId,
                                             String dxfEntityId, IotProductDO product) {
        // DXF 实体导入幂等校验
        if (floorId != null && StrUtil.isNotBlank(dxfEntityId)) {
            IbmsDeviceDO existDevice = ibmsDeviceMapper.selectByFloorIdAndDxfEntityId(floorId, dxfEntityId);
            if (existDevice != null) {
                throw exception(DEVICE_ALREADY_IMPORTED_FROM_DXF);
            }
        }

        // 楼层内设备名称唯一性 / 设备名-产品唯一性
        if (floorId != null) {
            TenantUtils.executeIgnore(() -> {
                IbmsDeviceDO existDevice = ibmsDeviceMapper.selectByFloorIdAndDeviceName(floorId, deviceName);
                if (existDevice != null) {
                    throw exception(DEVICE_NAME_EXISTS_IN_FLOOR);
                }
            });
        } else {
            TenantUtils.executeIgnore(() -> {
                if (ibmsDeviceMapper.selectByProductKeyAndDeviceName(productKey, deviceName) != null) {
                    throw exception(DEVICE_NAME_EXISTS);
                }
            });
        }

        // 校验父设备是否为合法网关
        if (IotProductDeviceTypeEnum.isGatewaySub(product.getDeviceType())
                && gatewayId != null) {
            validateGatewayDeviceExists(gatewayId);
        }
    }

    private void validateSerialNumberUnique(String serialNumber, Long excludeId) {
        if (StrUtil.isBlank(serialNumber)) {
            return;
        }
        IbmsDeviceDO existDevice = ibmsDeviceMapper.selectBySn(serialNumber);
        if (existDevice != null && ObjUtil.notEqual(existDevice.getId(), excludeId)) {
            throw exception(DEVICE_SERIAL_NUMBER_EXISTS);
        }
    }

    private IbmsDeviceSaveReqVO buildIbmsDeviceSaveForLegacyCreate(IotProductDO iotProduct, IotDeviceSaveReqVO vo) {
        IbmsProductDO p = ibmsProductMapper.selectById(iotProduct.getId());
        if (p == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        String brand = StrUtil.blankToDefault(p.getManufacturer(), "UNK");
        String model = StrUtil.blankToDefault(p.getModelNumber(), p.getModelCode());
        if (StrUtil.isBlank(model)) {
            model = "LEGACY";
        }
        String prefix = StrUtil.blankToDefault(p.getSystemCode(), "VI").trim() + "-"
                + StrUtil.blankToDefault(p.getModelCode(), "UNK").trim() + "-"
                + StrUtil.blankToDefault(p.getDeviceTypeCode(), "CAM").trim() + "-"
                + brand.trim() + "-";
        int nextSeq = ibmsDeviceMapper.selectMaxNumericSuffixByDeviceCodePrefix(prefix) + 1;

        IbmsDeviceSaveReqVO req = new IbmsDeviceSaveReqVO();
        req.setName(vo.getDeviceName());
        req.setGroupCode(StrUtil.blankToDefault(p.getGroupCode(), "SA"));
        req.setSystemCode(StrUtil.blankToDefault(p.getSystemCode(), "VI"));
        req.setDeviceTypeCode(StrUtil.blankToDefault(p.getDeviceTypeCode(), "CAM"));
        req.setBrand(brand);
        req.setAccessType("IP");
        req.setProductModel(model);
        req.setSeq(nextSeq);
        req.setProtocol(p.getProtocol());
        req.setExtra(buildLegacyExtraJson(vo));
        return req;
    }

    private String buildLegacyExtraJson(IotDeviceSaveReqVO vo) {
        if (vo == null) {
            return null;
        }
        cn.hutool.json.JSONObject obj = JSONUtil.createObj();
        if (StrUtil.isNotBlank(vo.getConfig())) {
            try {
                obj.set("legacyIotConfig", JSONUtil.parse(vo.getConfig()));
            } catch (Exception e) {
                obj.set("legacyIotConfigRaw", vo.getConfig());
            }
        }
        if (StrUtil.isNotBlank(vo.getAccount())) {
            obj.set("account", vo.getAccount());
        }
        return obj.isEmpty() ? null : obj.toString();
    }

    private void applyLegacyFieldsAfterIbmsCreate(Long deviceId, IotDeviceSaveReqVO vo, IotProductDO product) {
        if (deviceId == null || vo == null || product == null) {
            return;
        }
        IbmsDeviceDO d = ibmsDeviceMapper.selectById(deviceId);
        if (d == null) {
            return;
        }

        if (StrUtil.isNotBlank(vo.getNickname())) {
            d.setNickname(vo.getNickname());
        }
        if (StrUtil.isNotBlank(vo.getPicUrl())) {
            d.setPicUrl(vo.getPicUrl());
        }
        if (vo.getGroupIds() != null) {
            d.setGroupIds(vo.getGroupIds());
        }
        if (StrUtil.isNotBlank(vo.getDxfEntityId())) {
            d.setDxfEntityId(vo.getDxfEntityId());
        }
        d.setDeviceType(product.getDeviceType());
        if (StrUtil.isNotBlank(vo.getMenuIds())) {
            d.setMenuIds(vo.getMenuIds());
        }
        if (vo.getPrimaryMenuId() != null) {
            d.setPrimaryMenuId(vo.getPrimaryMenuId());
        }
        if (vo.getMenuOverride() != null) {
            d.setMenuOverride(vo.getMenuOverride());
        }

        d.setProductKey(product.getProductKey());
        d.setDeviceKey(generateDeviceKey(product.getProductKey(), vo.getSerialNumber()));
        d.setDeviceSecret(generateDeviceSecret());
        if (StrUtil.isNotBlank(vo.getSerialNumber())) {
            d.setSn(vo.getSerialNumber());
        }
        ibmsDeviceMapper.updateById(d);

        IbmsDeviceRuntimeDO rt = ibmsDeviceRuntimeMapper.selectById(deviceId);
        if (rt == null) {
            return;
        }
        if (vo.getGatewayId() != null) {
            rt.setGatewayId(vo.getGatewayId());
        }
        if (vo.getLocationType() != null) {
            rt.setLocationType(vo.getLocationType());
        }
        if (vo.getLatitude() != null) {
            rt.setLatitude(vo.getLatitude());
        }
        if (vo.getLongitude() != null) {
            rt.setLongitude(vo.getLongitude());
        }
        if (vo.getCampusId() != null) {
            rt.setCampusId(vo.getCampusId());
        }
        if (vo.getBuildingId() != null) {
            rt.setBuildingId(vo.getBuildingId());
        }
        if (vo.getFloorId() != null) {
            rt.setFloorId(vo.getFloorId());
        }
        if (vo.getRoomId() != null) {
            rt.setRoomId(vo.getRoomId());
        }
        if (vo.getLocalX() != null) {
            rt.setLocalX(vo.getLocalX());
        }
        if (vo.getLocalY() != null) {
            rt.setLocalY(vo.getLocalY());
        }
        if (vo.getLocalZ() != null) {
            rt.setLocalZ(vo.getLocalZ());
        }
        if (StrUtil.isNotBlank(vo.getInstallLocation())) {
            rt.setInstallLocation(vo.getInstallLocation());
        }
        if (StrUtil.isNotBlank(vo.getInstallHeightType())) {
            rt.setInstallHeightType(vo.getInstallHeightType());
        }
        rt.setState(IotDeviceStateEnum.INACTIVE.getState());
        ibmsDeviceRuntimeMapper.updateById(rt);
    }

    private void patchIbmsDeviceFromLegacyImport(Long deviceId, Long gatewayId, Set<Long> groupIds, Integer locationType) {
        if (deviceId == null) {
            return;
        }
        IbmsDeviceDO d = ibmsDeviceMapper.selectById(deviceId);
        if (d != null && groupIds != null) {
            d.setGroupIds(groupIds);
            ibmsDeviceMapper.updateById(d);
        }
        IbmsDeviceRuntimeDO rt = ibmsDeviceRuntimeMapper.selectById(deviceId);
        if (rt != null) {
            if (gatewayId != null) {
                rt.setGatewayId(gatewayId);
            }
            if (locationType != null) {
                rt.setLocationType(locationType);
            }
            ibmsDeviceRuntimeMapper.updateById(rt);
        }
    }

    private void patchIbmsDeviceFromLegacyUpdate(IotDeviceSaveReqVO vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        Long id = vo.getId();
        IbmsDeviceDO d = ibmsDeviceMapper.selectById(id);
        if (d == null) {
            return;
        }
        if (vo.getNickname() != null) {
            d.setNickname(vo.getNickname());
        }
        if (vo.getPicUrl() != null) {
            d.setPicUrl(vo.getPicUrl());
        }
        if (vo.getGroupIds() != null) {
            d.setGroupIds(vo.getGroupIds());
        }
        if (StrUtil.isNotBlank(vo.getSerialNumber())) {
            d.setSn(vo.getSerialNumber());
        }
        if (StrUtil.isNotBlank(vo.getDxfEntityId())) {
            d.setDxfEntityId(vo.getDxfEntityId());
        }
        if (vo.getMenuIds() != null) {
            d.setMenuIds(vo.getMenuIds());
        }
        if (vo.getPrimaryMenuId() != null) {
            d.setPrimaryMenuId(vo.getPrimaryMenuId());
        }
        if (vo.getMenuOverride() != null) {
            d.setMenuOverride(vo.getMenuOverride());
        }
        if (StrUtil.isNotBlank(vo.getConfig())) {
            try {
                cn.hutool.json.JSONObject root = StrUtil.isBlank(d.getExtra())
                        ? JSONUtil.createObj()
                        : JSONUtil.parseObj(d.getExtra());
                root.set("legacyIotConfig", JSONUtil.parse(vo.getConfig()));
                d.setExtra(root.toString());
            } catch (Exception e) {
                cn.hutool.json.JSONObject root = StrUtil.isBlank(d.getExtra())
                        ? JSONUtil.createObj()
                        : JSONUtil.parseObj(d.getExtra());
                root.set("legacyIotConfigRaw", vo.getConfig());
                d.setExtra(root.toString());
            }
        }
        ibmsDeviceMapper.updateById(d);

        IbmsDeviceRuntimeDO rt = ibmsDeviceRuntimeMapper.selectById(id);
        if (rt == null) {
            return;
        }
        if (vo.getGatewayId() != null) {
            rt.setGatewayId(vo.getGatewayId());
        }
        if (vo.getLocationType() != null) {
            rt.setLocationType(vo.getLocationType());
        }
        if (vo.getLatitude() != null) {
            rt.setLatitude(vo.getLatitude());
        }
        if (vo.getLongitude() != null) {
            rt.setLongitude(vo.getLongitude());
        }
        if (vo.getCampusId() != null) {
            rt.setCampusId(vo.getCampusId());
        }
        if (vo.getBuildingId() != null) {
            rt.setBuildingId(vo.getBuildingId());
        }
        if (vo.getFloorId() != null) {
            rt.setFloorId(vo.getFloorId());
        }
        if (vo.getRoomId() != null) {
            rt.setRoomId(vo.getRoomId());
        }
        if (vo.getLocalX() != null) {
            rt.setLocalX(vo.getLocalX());
        }
        if (vo.getLocalY() != null) {
            rt.setLocalY(vo.getLocalY());
        }
        if (vo.getLocalZ() != null) {
            rt.setLocalZ(vo.getLocalZ());
        }
        if (vo.getInstallLocation() != null) {
            rt.setInstallLocation(vo.getInstallLocation());
        }
        if (vo.getInstallHeightType() != null) {
            rt.setInstallHeightType(vo.getInstallHeightType());
        }
        ibmsDeviceRuntimeMapper.updateById(rt);
    }

    private void validateGatewayDeviceExists(Long id) {
        IbmsDeviceDO device = ibmsDeviceMapper.selectById(id);
        if (device == null) {
            throw exception(DEVICE_GATEWAY_NOT_EXISTS);
        }
        if (!IotProductDeviceTypeEnum.isGateway(device.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY);
        }
    }

    private String generateDeviceKey(String productKey, String serialNumber) {
        if (StrUtil.isNotEmpty(serialNumber)) {
            return productKey + "_" + serialNumber;
        }
        return productKey + "_" + IdUtil.fastSimpleUUID();
    }

    private String generateDeviceSecret() {
        return IdUtil.fastSimpleUUID();
    }
}

