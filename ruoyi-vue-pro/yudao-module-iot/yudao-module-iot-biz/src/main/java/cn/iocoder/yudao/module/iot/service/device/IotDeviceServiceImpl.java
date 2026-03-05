package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.*;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceGroupDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.websocket.IotWebSocketHandler;
import cn.iocoder.yudao.module.iot.websocket.message.DeviceStatusMessage;
import cn.iocoder.yudao.module.iot.websocket.message.IotMessage;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 设备 Service 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class IotDeviceServiceImpl implements IotDeviceService {

    @Resource(name = "iotWebSocketHandler")
    private IotWebSocketHandler iotWebSocketHandler;

    @Resource
    private IotDeviceMapper deviceMapper;

    @Resource
    @Lazy  // 延迟加载，解决循环依赖
    private IotProductService productService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotDeviceGroupService deviceGroupService;

    @Override
    public Long createDevice(IotDeviceSaveReqVO createReqVO) {
        // 1.1 校验产品是否存在
        IotProductDO product = productService.getProduct(createReqVO.getProductId());
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        // 1.2 统一校验（包含楼层唯一性和DXF绑定校验）
        validateCreateDeviceParam(product.getProductKey(), createReqVO.getDeviceName(),
                createReqVO.getGatewayId(), createReqVO.getFloorId(), 
                createReqVO.getDxfEntityId(), product);
        // 1.3 校验分组存在
        deviceGroupService.validateDeviceGroupExists(createReqVO.getGroupIds());
        // 1.4 校验设备序列号全局唯一
        validateSerialNumberUnique(createReqVO.getSerialNumber(), null);

        // 2. 插入到数据库
        IotDeviceDO device = BeanUtils.toBean(createReqVO, IotDeviceDO.class);
        initDevice(device, product);
        deviceMapper.insert(device);
        return device.getId();
    }

    private void validateCreateDeviceParam(String productKey, String deviceName,
                                           Long gatewayId, Long floorId, 
                                           String dxfEntityId, IotProductDO product) {
        // 🎯 优先检查：如果有DXF实体ID，检查是否已导入
        if (floorId != null && StrUtil.isNotBlank(dxfEntityId)) {
            IotDeviceDO existDevice = deviceMapper.selectByFloorIdAndDxfEntityId(floorId, dxfEntityId);
            if (existDevice != null) {
                throw exception(DEVICE_ALREADY_IMPORTED_FROM_DXF);
            }
        }
        
        // 🎯 校验设备名称在同一楼层下是否唯一（如果有楼层信息）
        if (floorId != null) {
            TenantUtils.executeIgnore(() -> {
                IotDeviceDO existDevice = deviceMapper.selectByFloorIdAndDeviceName(floorId, deviceName);
                if (existDevice != null) {
                    throw exception(DEVICE_NAME_EXISTS_IN_FLOOR);
                }
            });
        } else {
            // 如果没有楼层信息，则在同一产品下唯一（保持原有逻辑）
            TenantUtils.executeIgnore(() -> {
                if (deviceMapper.selectByProductKeyAndDeviceName(productKey, deviceName) != null) {
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

    /**
     * 校验设备序列号全局唯一性
     *
     * @param serialNumber 设备序列号
     * @param excludeId 排除的设备编号（用于更新时排除自身）
     */
    private void validateSerialNumberUnique(String serialNumber, Long excludeId) {
        if (StrUtil.isBlank(serialNumber)) {
            return;
        }
        IotDeviceDO existDevice = deviceMapper.selectBySerialNumber(serialNumber);
        if (existDevice != null && ObjUtil.notEqual(existDevice.getId(), excludeId)) {
            throw exception(DEVICE_SERIAL_NUMBER_EXISTS);
        }
    }

    private void initDevice(IotDeviceDO device, IotProductDO product) {
        device.setProductId(product.getId()).setProductKey(product.getProductKey())
                .setDeviceType(product.getDeviceType());
        // 🔑 生成设备Key（必填字段）
        device.setDeviceKey(generateDeviceKey(product.getProductKey(), device.getSerialNumber()));
        // 生成密钥
        device.setDeviceSecret(generateDeviceSecret());
        // 设置设备状态为未激活
        device.setState(IotDeviceStateEnum.INACTIVE.getState());
    }
    
    /**
     * 生成设备Key
     * 
     * @param productKey 产品Key
     * @param serialNumber 设备序列号（可选）
     * @return 设备Key
     */
    private String generateDeviceKey(String productKey, String serialNumber) {
        if (StrUtil.isNotEmpty(serialNumber)) {
            // 使用序列号：产品Key + 序列号
            return productKey + "_" + serialNumber;
        } else {
            // 无序列号：产品Key + UUID
            return productKey + "_" + IdUtil.fastSimpleUUID();
        }
    }

    @Override
    public void updateDevice(IotDeviceSaveReqVO updateReqVO) {
        updateReqVO.setDeviceName(null).setProductId(null); // 不允许更新
        // 1.1 校验存在
        IotDeviceDO device = validateDeviceExists(updateReqVO.getId());
        // 1.2 校验父设备是否为合法网关
        if (IotProductDeviceTypeEnum.isGatewaySub(device.getDeviceType())
                && updateReqVO.getGatewayId() != null) {
            validateGatewayDeviceExists(updateReqVO.getGatewayId());
        }
        // 1.3 校验分组存在
        deviceGroupService.validateDeviceGroupExists(updateReqVO.getGroupIds());
        // 1.4 校验设备序列号全局唯一
        validateSerialNumberUnique(updateReqVO.getSerialNumber(), updateReqVO.getId());

        // 2. 更新到数据库
        IotDeviceDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceDO.class);
        deviceMapper.updateById(updateObj);

        // 3. 清空对应缓存
        deleteDeviceCache(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceGroup(IotDeviceUpdateGroupReqVO updateReqVO) {
        // 1.1 校验设备存在
        List<IotDeviceDO> devices = deviceMapper.selectByIds(updateReqVO.getIds());
        if (CollUtil.isEmpty(devices)) {
            return;
        }
        // 1.2 校验分组存在
        deviceGroupService.validateDeviceGroupExists(updateReqVO.getGroupIds());

        // 3. 更新设备分组
        deviceMapper.updateBatch(convertList(devices, device -> new IotDeviceDO()
                .setId(device.getId()).setGroupIds(updateReqVO.getGroupIds())));

        // 4. 清空对应缓存
        deleteDeviceCache(devices);
    }

    @Override
    public void deleteDevice(Long id) {
        // 1.1 校验存在
        IotDeviceDO device = validateDeviceExists(id);
        // 1.2 如果是网关设备，检查是否有子设备
        if (device.getGatewayId() != null && deviceMapper.selectCountByGatewayId(id) > 0) {
            throw exception(DEVICE_HAS_CHILDREN);
        }

        // 2. 删除设备
        deviceMapper.deleteById(id);

        // 3. 清空对应缓存
        deleteDeviceCache(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeviceList(Collection<Long> ids) {
        // 1.1 校验存在
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<IotDeviceDO> devices = deviceMapper.selectByIds(ids);
        if (CollUtil.isEmpty(devices)) {
            return;
        }
        // 1.2 校验网关设备是否存在
        for (IotDeviceDO device : devices) {
            if (device.getGatewayId() != null && deviceMapper.selectCountByGatewayId(device.getId()) > 0) {
                throw exception(DEVICE_HAS_CHILDREN);
            }
        }

        // 2. 删除设备
        deviceMapper.deleteByIds(ids);

        // 3. 清空对应缓存
        deleteDeviceCache(devices);
    }

    @Override
    public IotDeviceDO validateDeviceExists(Long id) {
        IotDeviceDO device = deviceMapper.selectById(id);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    @Override
    public IotDeviceDO validateDeviceExistsFromCache(Long id) {
        IotDeviceDO device = getSelf().getDeviceFromCache(id);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    /**
     * 校验网关设备是否存在
     *
     * @param id 设备 ID
     */
    private void validateGatewayDeviceExists(Long id) {
        IotDeviceDO device = deviceMapper.selectById(id);
        if (device == null) {
            throw exception(DEVICE_GATEWAY_NOT_EXISTS);
        }
        if (!IotProductDeviceTypeEnum.isGateway(device.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY);
        }
    }

    @Override
    public IotDeviceDO getDevice(Long id) {
        return deviceMapper.selectById(id);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.DEVICE, key = "#id", unless = "#result == null")
    @TenantIgnore // 忽略租户信息
    public IotDeviceDO getDeviceFromCache(Long id) {
        return deviceMapper.selectById(id);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.DEVICE, key = "#productKey + '_' + #deviceName", unless = "#result == null")
    @TenantIgnore // 忽略租户信息，跨租户 productKey + deviceName 是唯一的
    public IotDeviceDO getDeviceFromCache(String productKey, String deviceName) {
        return deviceMapper.selectByProductKeyAndDeviceName(productKey, deviceName);
    }

    @Override
    public IotDeviceDO getDeviceBySerialNumber(String serialNumber) {
        return deviceMapper.selectOne(IotDeviceDO::getSerialNumber, serialNumber);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.DEVICE, key = "'key_' + #deviceKey", unless = "#result == null")
    @TenantIgnore // 忽略租户信息
    public IotDeviceDO getDeviceFromCacheByDeviceKey(String deviceKey) {
        return deviceMapper.selectByDeviceKey(deviceKey);
    }

    @Override
    public PageResult<IotDeviceDO> getDevicePage(IotDevicePageReqVO pageReqVO) {
        return deviceMapper.selectPage(pageReqVO);
    }

    @Override
    public void fillDeviceRealTimeStatus(List<IotDeviceRespVO> devices) {
        // 设备状态已通过 DeviceStateChangeConsumer 从 RocketMQ 同步到数据库
        // 无需额外查询，直接使用数据库中的状态
        if (devices == null || devices.isEmpty()) {
            return;
        }
        
        log.debug("[fillDeviceRealTimeStatus] 设备状态已从数据库加载，总数: {}", devices.size());
        // 状态字段已在 DAO 查询时填充，无需额外操作
    }

    @Override
    public List<IotDeviceDO> getDeviceListByCondition(@Nullable Integer deviceType, @Nullable Long productId) {
        return deviceMapper.selectListByCondition(deviceType, productId);
    }

    @Override
    public List<IotDeviceDO> getDeviceListByState(Integer state) {
        return deviceMapper.selectListByState(state);
    }

    @Override
    public List<IotDeviceDO> getDeviceListByProductId(Long productId) {
        return deviceMapper.selectListByProductId(productId);
    }

    @Override
    public void updateDeviceState(IotDeviceDO device, Integer state) {
        // 1. 更新状态和时间
        IotDeviceDO updateObj = new IotDeviceDO().setId(device.getId()).setState(state);
        if (device.getOnlineTime() == null
                && Objects.equals(state, IotDeviceStateEnum.ONLINE.getState())) {
            updateObj.setActiveTime(LocalDateTime.now());
        }
        if (Objects.equals(state, IotDeviceStateEnum.ONLINE.getState())) {
            updateObj.setOnlineTime(LocalDateTime.now());
        } else if (Objects.equals(state, IotDeviceStateEnum.OFFLINE.getState())) {
            updateObj.setOfflineTime(LocalDateTime.now());
        }
        deviceMapper.updateById(updateObj);

        // 2. 清空对应缓存
        deleteDeviceCache(device);
        
        // 3. 推送 WebSocket 消息（设备状态变化）
        pushDeviceStatusChange(device.getId(), device.getDeviceName(), state);
    }
    
    /**
     * 推送设备状态变化消息（通过 WebSocket）
     */
    private void pushDeviceStatusChange(Long deviceId, String deviceName, Integer state) {
        try {
            // 构建状态消息
            DeviceStatusMessage statusMsg = DeviceStatusMessage.builder()
                    .deviceId(deviceId)
                    .deviceName(deviceName)
                    .status(Objects.equals(state, IotDeviceStateEnum.ONLINE.getState()) ? "online" : "offline")
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            // 构建 IoT 消息
            IotMessage message = IotMessage.deviceStatus(statusMsg);
            
            // 广播给所有在线用户
            if (iotWebSocketHandler != null) {
                iotWebSocketHandler.broadcast(message);
                log.info("[设备状态] WebSocket推送成功: deviceId={}, status={}", deviceId, statusMsg.getStatus());
            }
        } catch (Exception e) {
            log.error("[设备状态] WebSocket推送失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
        }
    }

    @Override
    public void updateDeviceState(Long id, Integer state) {
        // 校验存在
        IotDeviceDO device = validateDeviceExists(id);
        // 执行更新
        updateDeviceState(device, state);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceConfig(Long deviceId, DeviceConfig config) {
        if (deviceId == null || config == null) {
            return;
        }
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            return;
        }

        IotDeviceDO updateObj = new IotDeviceDO();
        updateObj.setId(deviceId);
        updateObj.setConfig(config);
        deviceMapper.updateById(updateObj);

        // 清理缓存，避免业务侧继续读取旧 config
        deleteDeviceCache(device);
    }

    @Override
    public Long getDeviceCountByProductId(Long productId) {
        return deviceMapper.selectCountByProductId(productId);
    }

    @Override
    public Long getDeviceCountByGroupId(Long groupId) {
        return deviceMapper.selectCountByGroupId(groupId);
    }

    /**
     * 生成 deviceSecret
     *
     * @return 生成的 deviceSecret
     */
    private String generateDeviceSecret() {
        return IdUtil.fastSimpleUUID();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public IotDeviceImportRespVO importDevice(List<IotDeviceImportExcelVO> importDevices, boolean updateSupport) {
        // 1. 参数校验
        if (CollUtil.isEmpty(importDevices)) {
            throw exception(DEVICE_IMPORT_LIST_IS_EMPTY);
        }

        // 2. 遍历，逐个创建 or 更新
        IotDeviceImportRespVO respVO = IotDeviceImportRespVO.builder().createDeviceNames(new ArrayList<>())
                .updateDeviceNames(new ArrayList<>()).failureDeviceNames(new LinkedHashMap<>()).build();
        importDevices.forEach(importDevice -> {
            try {
                // 2.1.1 校验字段是否符合要求
                try {
                    ValidationUtils.validate(importDevice);
                } catch (ConstraintViolationException ex) {
                    respVO.getFailureDeviceNames().put(importDevice.getDeviceName(), ex.getMessage());
                    return;
                }
                // 2.1.2 校验产品是否存在
                IotProductDO product = productService.validateProductExists(importDevice.getProductKey());
                // 2.1.3 校验父设备是否存在
                Long gatewayId = null;
                if (StrUtil.isNotEmpty(importDevice.getParentDeviceName())) {
                    IotDeviceDO gatewayDevice = deviceMapper.selectByDeviceName(importDevice.getParentDeviceName());
                    if (gatewayDevice == null) {
                        throw exception(DEVICE_GATEWAY_NOT_EXISTS);
                    }
                    if (!IotProductDeviceTypeEnum.isGateway(gatewayDevice.getDeviceType())) {
                        throw exception(DEVICE_NOT_GATEWAY);
                    }
                    gatewayId = gatewayDevice.getId();
                }
                // 2.1.4 校验设备分组是否存在
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

                // 2.2.1 判断如果不存在，在进行插入
                IotDeviceDO existDevice = deviceMapper.selectByDeviceName(importDevice.getDeviceName());
                if (existDevice == null) {
                    createDevice(new IotDeviceSaveReqVO()
                            .setDeviceName(importDevice.getDeviceName())
                            .setProductId(product.getId()).setGatewayId(gatewayId).setGroupIds(groupIds)
                            .setLocationType(importDevice.getLocationType()));
                    respVO.getCreateDeviceNames().add(importDevice.getDeviceName());
                    return;
                }
                // 2.2.2 如果存在，判断是否允许更新
                if (updateSupport) {
                    throw exception(DEVICE_KEY_EXISTS);
                }
                updateDevice(new IotDeviceSaveReqVO().setId(existDevice.getId())
                        .setGatewayId(gatewayId).setGroupIds(groupIds).setLocationType(importDevice.getLocationType()));
                respVO.getUpdateDeviceNames().add(importDevice.getDeviceName());
            } catch (ServiceException ex) {
                respVO.getFailureDeviceNames().put(importDevice.getDeviceName(), ex.getMessage());
            }
        });
        return respVO;
    }

    @Override
    public IotDeviceAuthInfoRespVO getDeviceAuthInfo(Long id) {
        IotDeviceDO device = validateDeviceExists(id);
        // 使用 IotDeviceAuthUtils 生成认证信息
        IotDeviceAuthUtils.AuthInfo authInfo = IotDeviceAuthUtils.getAuthInfo(
                device.getProductKey(), device.getDeviceName(), device.getDeviceSecret());
        return BeanUtils.toBean(authInfo, IotDeviceAuthInfoRespVO.class);
    }

    private void deleteDeviceCache(IotDeviceDO device) {
        // 保证 Spring AOP 触发
        getSelf().deleteDeviceCache0(device);
    }

    private void deleteDeviceCache(List<IotDeviceDO> devices) {
        devices.forEach(this::deleteDeviceCache);
    }

    @SuppressWarnings("unused")
    @Caching(evict = {
        @CacheEvict(value = RedisKeyConstants.DEVICE, key = "#device.id"),
        @CacheEvict(value = RedisKeyConstants.DEVICE, key = "#device.productKey + '_' + #device.deviceName")
    })
    public void deleteDeviceCache0(IotDeviceDO device) {
    }

    @Override
    public Long getDeviceCount(LocalDateTime createTime) {
        return deviceMapper.selectCountByCreateTime(createTime);
    }

    @Override
    public Map<Long, Integer> getDeviceCountMapByProductId() {
        return deviceMapper.selectDeviceCountMapByProductId();
    }

    @Override
    public Map<Integer, Long> getDeviceCountMapByState() {
        return deviceMapper.selectDeviceCountGroupByState();
    }

    @Override
    public List<IotDeviceDO> getDeviceListByProductKeyAndNames(String productKey, List<String> deviceNames) {
        if (StrUtil.isBlank(productKey) || CollUtil.isEmpty(deviceNames)) {
            return Collections.emptyList();
        }
        return deviceMapper.selectByProductKeyAndDeviceNames(productKey, deviceNames);
    }

    @Override
    public boolean authDevice(IotDeviceAuthReqDTO authReqDTO) {
        // 1. 校验设备是否存在
        IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(authReqDTO.getUsername());
        if (deviceInfo == null) {
            log.error("[authDevice][认证失败，username({}) 格式不正确]", authReqDTO.getUsername());
            return false;
        }
        String deviceName = deviceInfo.getDeviceName();
        String productKey = deviceInfo.getProductKey();
        IotDeviceDO device = getSelf().getDeviceFromCache(productKey, deviceName);
        if (device == null) {
            log.warn("[authDevice][设备({}/{}) 不存在]", productKey, deviceName);
            return false;
        }

        // 2. 校验密码
        IotDeviceAuthUtils.AuthInfo authInfo = IotDeviceAuthUtils.getAuthInfo(productKey, deviceName, device.getDeviceSecret());
        if (ObjUtil.notEqual(authInfo.getPassword(), authReqDTO.getPassword())) {
            log.error("[authDevice][设备({}/{}) 密码不正确]", productKey, deviceName);
            return false;
        }
        return true;
    }

    @Override
    public List<IotDeviceDO> validateDeviceListExists(Collection<Long> ids) {
        List<IotDeviceDO> devices = getDeviceList(ids);
        if (devices.size() != ids.size()) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return devices;
    }

    @Override
    public List<IotDeviceDO> getDeviceList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return deviceMapper.selectByIds(ids);
    }

    @Override
    public void updateDeviceFirmware(Long deviceId, Long firmwareId) {
        // 1. 校验设备是否存在
        IotDeviceDO device = validateDeviceExists(deviceId);
        
        // 2. 更新设备固件版本
        IotDeviceDO updateObj = new IotDeviceDO().setId(deviceId).setFirmwareId(firmwareId);
        deviceMapper.updateById(updateObj);
        
        // 3. 清空对应缓存
        deleteDeviceCache(device);
    }

    @Override
    @TenantIgnore // 忽略租户隔离：因为 RocketMQ 消费者调用时，未传递租户上下文
    public boolean isDeviceExistsByIp(String ip) {
        // 通过 config 字段中的 IP 判断
        // 优先使用 DeviceConfigHelper，兼容旧数据的 ip 字段
        List<IotDeviceDO> devices = deviceMapper.selectList();
        return devices.stream()
            .anyMatch(device -> {
                // 1. 优先使用 DeviceConfigHelper 获取 ipAddress
                String deviceIp = DeviceConfigHelper.getIpAddress(device);
                if (deviceIp != null && ip.equals(deviceIp)) {
                    return true;
                }
                
                // 2. 兼容旧数据：检查 config.ip 字段
                if (device.getConfig() == null) {
                    return false;
                }
                try {
                    Map<String, Object> configMap = device.getConfig().toMap();
                    Object configIp = configMap.get("ip");
                    return configIp != null && ip.equals(configIp.toString());
                } catch (Exception e) {
                    return false;
                }
            });
    }

    private IotDeviceServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    @Override
    public List<IotDeviceDO> getDevicesWithJobConfig() {
        return deviceMapper.selectDevicesWithJobConfig();
    }

    @Override
    public void updateDeviceJobConfig(Long id, String jobConfig) {
        IotDeviceDO updateObj = new IotDeviceDO();
        updateObj.setId(id);
        updateObj.setJobConfig(jobConfig);
        deviceMapper.updateById(updateObj);
    }

    @Override
    public IotDeviceMenuConfigVO getDeviceMenuConfig(Long deviceId) {
        // 1. 查询设备信息
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        
        // 2. 构建菜单配置
        IotDeviceMenuConfigVO config = new IotDeviceMenuConfigVO();
        
        // 3. 判断是否使用设备自己的配置
        if (Boolean.TRUE.equals(device.getMenuOverride()) && StrUtil.isNotBlank(device.getMenuIds())) {
            // 使用设备配置
            config.setMenuIds(device.getMenuIds());
            config.setPrimaryMenuId(device.getPrimaryMenuId());
            config.setSource("device");
        } else {
            // 继承产品配置
            IotProductDO product = productService.getProduct(device.getProductId());
            if (product != null) {
                config.setMenuIds(product.getMenuIds());
                config.setPrimaryMenuId(product.getPrimaryMenuId());
            }
            config.setSource("product");
        }
        
        return config;
    }
    
    @Override
    public Map<Long, IotDeviceMenuConfigVO> getDeviceMenuConfigBatch(List<Long> deviceIds) {
        if (CollUtil.isEmpty(deviceIds)) {
            return Collections.emptyMap();
        }
        
        // 1. 批量查询设备
        List<IotDeviceDO> devices = deviceMapper.selectBatchIds(deviceIds);
        if (CollUtil.isEmpty(devices)) {
            return Collections.emptyMap();
        }
        
        // 2. 收集需要查询的产品ID
        Set<Long> productIds = devices.stream()
            .map(IotDeviceDO::getProductId)
            .filter(Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
        
        // 3. 批量查询产品
        Map<Long, IotProductDO> productMap = productService.getProductMap(productIds);
        
        // 4. 构建菜单配置映射
        Map<Long, IotDeviceMenuConfigVO> configMap = new HashMap<>();
        for (IotDeviceDO device : devices) {
            IotDeviceMenuConfigVO config = new IotDeviceMenuConfigVO();
            
            // 判断是否使用设备自己的配置
            if (Boolean.TRUE.equals(device.getMenuOverride()) && StrUtil.isNotBlank(device.getMenuIds())) {
                // 使用设备配置
                config.setMenuIds(device.getMenuIds());
                config.setPrimaryMenuId(device.getPrimaryMenuId());
                config.setSource("device");
            } else {
                // 继承产品配置
                IotProductDO product = productMap.get(device.getProductId());
                if (product != null) {
                    config.setMenuIds(product.getMenuIds());
                    config.setPrimaryMenuId(product.getPrimaryMenuId());
                }
                config.setSource("product");
            }
            
            configMap.put(device.getId(), config);
        }
        
        return configMap;
    }

    @Override
    @TenantIgnore  // 忽略租户隔离，获取所有租户的在线设备
    public List<IotDeviceDO> getOnlineDeviceList() {
        // 查询所有在线状态的设备
        return deviceMapper.selectList("state", IotDeviceStateEnum.ONLINE.getState());
    }

    @Override
    @TenantIgnore  // 忽略租户隔离，获取所有租户的设备
    public List<IotDeviceDO> getDeviceList() {
        // 查询所有设备（不限状态和类型）
        return deviceMapper.selectList();
    }

    @Override
    public List<IotDeviceDO> getDeviceListWithTenantFilter() {
        // 查询当前租户的所有设备（不限状态和类型）
        // 注意：此方法不使用 @TenantIgnore，会根据租户上下文自动过滤
        // 配合 TenantUtils.execute(tenantId, ...) 使用
        return deviceMapper.selectList();
    }

    @Override
    @TenantIgnore  // 忽略租户隔离，获取所有租户的门禁设备
    public List<IotDeviceDO> getDeviceListBySubsystemCode(String subsystemCode) {
        // 查询指定子系统的所有设备（不限状态）
        return deviceMapper.selectList("subsystem_code", subsystemCode);
    }

    @Override
    @TenantIgnore // 忽略租户隔离：因为 RocketMQ 消费者调用时，已在租户上下文中执行
    public void updateDeviceStateWithTimestamp(Long deviceId, Integer newState, Long timestamp) {
        // 1. 校验设备是否存在
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            log.warn("[updateDeviceStateWithTimestamp] 设备不存在: deviceId={}", deviceId);
            return;
        }
        
        // 2. 构建更新对象
        IotDeviceDO updateObj = new IotDeviceDO().setId(deviceId).setState(newState);
        
        // 3. 根据状态类型更新对应的时间字段
        // Requirements: 1.1, 1.2, 1.3
        LocalDateTime changeTime = timestamp != null 
                ? LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault())
                : LocalDateTime.now();
        
        if (Objects.equals(newState, IotDeviceStateEnum.ONLINE.getState())) {
            // 设备上线：更新 onlineTime
            updateObj.setOnlineTime(changeTime);
            // 首次上线同时更新 activeTime
            if (device.getActiveTime() == null) {
                updateObj.setActiveTime(changeTime);
            }
        } else if (Objects.equals(newState, IotDeviceStateEnum.OFFLINE.getState())) {
            // 设备离线：更新 offlineTime
            updateObj.setOfflineTime(changeTime);
        }
        
        // 4. 更新数据库
        deviceMapper.updateById(updateObj);
        
        // 5. 清空对应缓存
        deleteDeviceCache(device);
        
        log.info("[updateDeviceStateWithTimestamp] 设备状态更新成功: deviceId={}, newState={}, timestamp={}", 
                deviceId, newState, changeTime);
    }
    
    @Override
    @TenantIgnore // 忽略租户隔离：因为 RocketMQ 消费者调用时，已在租户上下文中执行
    public void updateDeviceVideoCapability(Long deviceId, Boolean supportVideo, Integer videoChannelCount, 
                                             Integer httpPort, Integer rtspPort) {
        // 1. 校验设备是否存在
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            log.warn("[updateDeviceVideoCapability] 设备不存在: deviceId={}", deviceId);
            return;
        }
        
        // 2. 获取现有配置
        cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig existingConfig = device.getConfig();
        
        // 3. 如果是门禁设备配置，更新视频能力字段
        if (existingConfig instanceof cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig) {
            cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig accessConfig = 
                    (cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig) existingConfig;
            
            // 更新视频能力字段
            accessConfig.setSupportVideo(supportVideo);
            if (httpPort != null) {
                accessConfig.setHttpPort(httpPort);
            }
            if (rtspPort != null) {
                accessConfig.setRtspPort(rtspPort);
            }
            
            // 4. 更新数据库
            IotDeviceDO updateObj = new IotDeviceDO();
            updateObj.setId(deviceId);
            updateObj.setConfig(accessConfig);
            deviceMapper.updateById(updateObj);
            
            // 5. 清空对应缓存
            deleteDeviceCache(device);
            
            log.info("[updateDeviceVideoCapability] 设备视频能力更新成功: deviceId={}, supportVideo={}, videoChannelCount={}, httpPort={}, rtspPort={}", 
                    deviceId, supportVideo, videoChannelCount, httpPort, rtspPort);
        } else {
            log.warn("[updateDeviceVideoCapability] 设备配置类型不是门禁设备配置，跳过更新: deviceId={}, configType={}", 
                    deviceId, existingConfig != null ? existingConfig.getClass().getSimpleName() : "null");
        }
    }
    
    @Override
    public List<IotDeviceDO> getNvrDevices() {
        return deviceMapper.selectNvrDevices();
    }
}
