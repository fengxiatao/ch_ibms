package cn.iocoder.yudao.module.iot.service.changhui.device;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceRegisterReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceUpdateReqVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.ChanghuiDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiDeviceTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 长辉设备 Service 实现类
 * <p>
 * 使用统一的 iot_device 表存储设备，长辉设备特有属性存储在 config 字段中。
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class ChanghuiDeviceServiceImpl implements ChanghuiDeviceService {

    /** 测站编码已存在错误码 */
    private static final ErrorCode STATION_CODE_EXISTS = new ErrorCode(1_001_002_001, "测站编码已存在");
    
    /** 设备不存在错误码 */
    private static final ErrorCode DEVICE_NOT_EXISTS = new ErrorCode(1_001_002_002, "设备不存在");
    
    /** 测站编码格式错误 */
    private static final ErrorCode STATION_CODE_INVALID = new ErrorCode(1_001_002_003, "测站编码格式错误，必须是20位十六进制字符串");

    /** 长辉设备产品ID（需要根据实际情况配置） */
    private static final Long CHANGHUI_PRODUCT_ID = 100L;

    @Resource
    private IotDeviceMapper iotDeviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerDevice(ChanghuiDeviceRegisterReqVO reqVO) {
        // 验证测站编码格式（德通协议规定：10字节 = 20个十六进制字符）
        String stationCode = reqVO.getStationCode();
        if (!isValidStationCode(stationCode)) {
            throw exception(STATION_CODE_INVALID);
        }
        
        // 检查测站编码是否已存在
        IotDeviceDO existDevice = getIotDeviceByStationCode(stationCode);
        if (existDevice != null) {
            throw exception(STATION_CODE_EXISTS);
        }
        
        // 创建设备配置
        ChanghuiDeviceConfig config = buildConfig(reqVO);
        config.validate();
        
        // 创建 IotDeviceDO
        // 重要：德通协议规定 deviceKey = stationCode（测站编码）
        // Gateway 通过 deviceKey 查询设备，确保设备能被正确识别
        IotDeviceDO device = IotDeviceDO.builder()
                .deviceName(reqVO.getDeviceName())
                .deviceKey(stationCode) // 强制 deviceKey = stationCode
                .productId(CHANGHUI_PRODUCT_ID)
                .deviceType(reqVO.getDeviceType())
                .state(IotDeviceStateEnum.INACTIVE.getState()) // 默认未激活
                .config(config)
                .build();
        
        iotDeviceMapper.insert(device);
        log.info("[registerDevice] 设备注册成功: deviceKey(stationCode)={}, deviceName={}", 
                stationCode, reqVO.getDeviceName());
        return device.getId();
    }
    
    /**
     * 验证测站编码格式
     * <p>
     * 德通协议规定：测站编码为10字节（20个十六进制字符）
     *
     * @param stationCode 测站编码
     * @return 是否有效
     */
    private boolean isValidStationCode(String stationCode) {
        if (stationCode == null || stationCode.length() != 20) {
            return false;
        }
        // 检查是否全部是十六进制字符
        return stationCode.matches("^[0-9A-Fa-f]{20}$");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDevice(ChanghuiDeviceUpdateReqVO reqVO) {
        // 检查设备是否存在
        IotDeviceDO existDevice = iotDeviceMapper.selectById(reqVO.getId());
        if (existDevice == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        
        // 更新设备配置
        ChanghuiDeviceConfig config = buildConfigFromUpdate(reqVO, existDevice);
        config.validate();
        
        // 更新设备
        IotDeviceDO updateDevice = new IotDeviceDO();
        updateDevice.setId(reqVO.getId());
        updateDevice.setDeviceName(reqVO.getDeviceName());
        if (reqVO.getDeviceType() != null) {
            updateDevice.setDeviceType(reqVO.getDeviceType());
        }
        // stationCode 作为 deviceKey 的唯一标识，保持同步（如果前端允许修改 stationCode）
        if (config != null && config.getStationCode() != null) {
            updateDevice.setDeviceKey(config.getStationCode());
        }
        updateDevice.setConfig(config);
        
        iotDeviceMapper.updateById(updateDevice);
        log.info("[updateDevice] 设备更新成功: id={}", reqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long id) {
        // 检查设备是否存在
        IotDeviceDO existDevice = iotDeviceMapper.selectById(id);
        if (existDevice == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        iotDeviceMapper.deleteById(id);
        log.info("[deleteDevice] 设备删除成功: id={}", id);
    }

    @Override
    public ChanghuiDeviceRespVO getDevice(Long id) {
        IotDeviceDO device = iotDeviceMapper.selectById(id);
        return enrichDeviceRespVO(convertToRespVO(device));
    }

    @Override
    public ChanghuiDeviceRespVO getDeviceByStationCode(String stationCode) {
        IotDeviceDO device = getIotDeviceByStationCode(stationCode);
        return enrichDeviceRespVO(convertToRespVO(device));
    }

    @Override
    public PageResult<ChanghuiDeviceRespVO> getDevicePage(ChanghuiDevicePageReqVO reqVO) {
        // 查询长辉设备（通过产品ID或设备类型过滤）
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(reqVO.getPageNo());
        pageParam.setPageSize(reqVO.getPageSize());
        
        LambdaQueryWrapperX<IotDeviceDO> wrapper = new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getProductId, CHANGHUI_PRODUCT_ID)
                .likeIfPresent(IotDeviceDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IotDeviceDO::getDeviceType, reqVO.getDeviceType())
                .eqIfPresent(IotDeviceDO::getState, reqVO.getStatus())
                .orderByDesc(IotDeviceDO::getId);
        
        // 如果有测站编码过滤，需要在内存中过滤（因为stationCode在config JSON中）
        PageResult<IotDeviceDO> pageResult = iotDeviceMapper.selectPage(pageParam, wrapper);
        
        // 转换为响应VO
        List<ChanghuiDeviceRespVO> respList = pageResult.getList().stream()
                .filter(device -> filterByStationCode(device, reqVO.getStationCode()))
                .map(this::convertToRespVO)
                .map(this::enrichDeviceRespVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceStatus(String stationCode, Integer status) {
        IotDeviceDO device = getIotDeviceByStationCode(stationCode);
        if (device != null) {
            IotDeviceDO updateDevice = new IotDeviceDO();
            updateDevice.setId(device.getId());
            updateDevice.setState(status == 1 ? IotDeviceStateEnum.ONLINE.getState() : IotDeviceStateEnum.OFFLINE.getState());
            iotDeviceMapper.updateById(updateDevice);
            log.info("[updateDeviceStatus] 设备状态更新: stationCode={}, status={}", stationCode, status);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceHeartbeat(String stationCode) {
        IotDeviceDO device = getIotDeviceByStationCode(stationCode);
        if (device != null) {
            IotDeviceDO updateDevice = new IotDeviceDO();
            updateDevice.setId(device.getId());
            updateDevice.setOnlineTime(LocalDateTime.now());
            updateDevice.setState(IotDeviceStateEnum.ONLINE.getState());
            iotDeviceMapper.updateById(updateDevice);
            log.debug("[updateDeviceHeartbeat] 设备心跳更新: stationCode={}", stationCode);
        }
    }

    @Override
    public List<ChanghuiDeviceRespVO> getOnlineDevices() {
        // 查询在线的长辉设备
        List<IotDeviceDO> devices = iotDeviceMapper.selectList(
                new LambdaQueryWrapperX<IotDeviceDO>()
                        .eq(IotDeviceDO::getProductId, CHANGHUI_PRODUCT_ID)
                        .eq(IotDeviceDO::getState, IotDeviceStateEnum.ONLINE.getState()));
        
        return devices.stream()
                .map(this::convertToRespVO)
                .map(this::enrichDeviceRespVO)
                .collect(Collectors.toList());
    }

    @Override
    public ChanghuiDeviceDO getDeviceDO(Long id) {
        IotDeviceDO device = iotDeviceMapper.selectById(id);
        return convertToChanghuiDeviceDO(device);
    }

    @Override
    public ChanghuiDeviceDO getDeviceDOByStationCode(String stationCode) {
        IotDeviceDO device = getIotDeviceByStationCode(stationCode);
        return convertToChanghuiDeviceDO(device);
    }

    // ==================== 私有方法 ====================

    /**
     * 根据测站编码查询设备
     * <p>
     * 德通协议规定：deviceKey = stationCode（测站编码）
     * 因此可以直接通过 deviceKey 查询，无需遍历所有设备
     */
    private IotDeviceDO getIotDeviceByStationCode(String stationCode) {
        if (stationCode == null || stationCode.trim().isEmpty()) {
            return null;
        }
        
        // 直接通过 deviceKey 查询（deviceKey = stationCode）
        return iotDeviceMapper.selectOne(
                new LambdaQueryWrapperX<IotDeviceDO>()
                        .eq(IotDeviceDO::getProductId, CHANGHUI_PRODUCT_ID)
                        .eq(IotDeviceDO::getDeviceKey, stationCode));
    }

    /**
     * 根据测站编码过滤设备
     */
    private boolean filterByStationCode(IotDeviceDO device, String stationCode) {
        if (stationCode == null || stationCode.trim().isEmpty()) {
            return true; // 不过滤
        }
        ChanghuiDeviceConfig config = getChanghuiConfig(device);
        return config != null && config.getStationCode() != null 
                && config.getStationCode().contains(stationCode);
    }

    /**
     * 获取设备的长辉配置
     */
    private ChanghuiDeviceConfig getChanghuiConfig(IotDeviceDO device) {
        if (device == null || device.getConfig() == null) {
            return null;
        }
        if (device.getConfig() instanceof ChanghuiDeviceConfig) {
            return (ChanghuiDeviceConfig) device.getConfig();
        }
        return null;
    }

    /**
     * 从注册请求构建配置
     */
    private ChanghuiDeviceConfig buildConfig(ChanghuiDeviceRegisterReqVO reqVO) {
        return ChanghuiDeviceConfig.builder()
                .stationCode(reqVO.getStationCode())
                .teaKey(reqVO.getTeaKey())
                .password(reqVO.getPassword())
                .provinceCode(reqVO.getProvinceCode())
                .managementCode(reqVO.getManagementCode())
                .stationCodePart(reqVO.getStationCodePart())
                .pileFront(reqVO.getPileFront())
                .pileBack(reqVO.getPileBack())
                .manufacturer(reqVO.getManufacturer())
                .sequenceNo(reqVO.getSequenceNo())
                .changhuiDeviceType(reqVO.getDeviceType())
                .build();
    }

    /**
     * 从更新请求构建配置（保留原有值）
     */
    private ChanghuiDeviceConfig buildConfigFromUpdate(ChanghuiDeviceUpdateReqVO reqVO, IotDeviceDO existDevice) {
        ChanghuiDeviceConfig existConfig = getChanghuiConfig(existDevice);
        
        return ChanghuiDeviceConfig.builder()
                .stationCode(reqVO.getStationCode() != null ? reqVO.getStationCode() : 
                        (existConfig != null ? existConfig.getStationCode() : null))
                .teaKey(reqVO.getTeaKey() != null ? reqVO.getTeaKey() : 
                        (existConfig != null ? existConfig.getTeaKey() : null))
                .password(reqVO.getPassword() != null ? reqVO.getPassword() : 
                        (existConfig != null ? existConfig.getPassword() : null))
                .provinceCode(reqVO.getProvinceCode() != null ? reqVO.getProvinceCode() : 
                        (existConfig != null ? existConfig.getProvinceCode() : null))
                .managementCode(reqVO.getManagementCode() != null ? reqVO.getManagementCode() : 
                        (existConfig != null ? existConfig.getManagementCode() : null))
                .stationCodePart(reqVO.getStationCodePart() != null ? reqVO.getStationCodePart() : 
                        (existConfig != null ? existConfig.getStationCodePart() : null))
                .pileFront(reqVO.getPileFront() != null ? reqVO.getPileFront() : 
                        (existConfig != null ? existConfig.getPileFront() : null))
                .pileBack(reqVO.getPileBack() != null ? reqVO.getPileBack() : 
                        (existConfig != null ? existConfig.getPileBack() : null))
                .manufacturer(reqVO.getManufacturer() != null ? reqVO.getManufacturer() : 
                        (existConfig != null ? existConfig.getManufacturer() : null))
                .sequenceNo(reqVO.getSequenceNo() != null ? reqVO.getSequenceNo() : 
                        (existConfig != null ? existConfig.getSequenceNo() : null))
                .changhuiDeviceType(reqVO.getDeviceType() != null ? reqVO.getDeviceType() : 
                        (existConfig != null ? existConfig.getChanghuiDeviceType() : null))
                .build();
    }

    /**
     * 将 IotDeviceDO 转换为 ChanghuiDeviceRespVO
     */
    private ChanghuiDeviceRespVO convertToRespVO(IotDeviceDO device) {
        if (device == null) {
            return null;
        }
        
        ChanghuiDeviceRespVO respVO = new ChanghuiDeviceRespVO();
        respVO.setId(device.getId());
        respVO.setDeviceName(device.getDeviceName());
        respVO.setDeviceType(device.getDeviceType());
        // 只有 ONLINE(1) 状态才视为在线
        respVO.setStatus(IotDeviceStateEnum.isOnline(device.getState()) ? 1 : 0);
        respVO.setLastHeartbeat(device.getOnlineTime());
        respVO.setCreateTime(device.getCreateTime());
        respVO.setUpdateTime(device.getUpdateTime());
        
        // 从config中提取长辉特有字段
        ChanghuiDeviceConfig config = getChanghuiConfig(device);
        if (config != null) {
            respVO.setStationCode(config.getStationCode());
            respVO.setProvinceCode(config.getProvinceCode());
            respVO.setManagementCode(config.getManagementCode());
            respVO.setStationCodePart(config.getStationCodePart());
            respVO.setPileFront(config.getPileFront());
            respVO.setPileBack(config.getPileBack());
            respVO.setManufacturer(config.getManufacturer());
            respVO.setSequenceNo(config.getSequenceNo());
        }
        
        return respVO;
    }

    /**
     * 将 IotDeviceDO 转换为 ChanghuiDeviceDO（兼容旧代码）
     */
    private ChanghuiDeviceDO convertToChanghuiDeviceDO(IotDeviceDO device) {
        if (device == null) {
            return null;
        }
        
        ChanghuiDeviceDO changhuiDevice = new ChanghuiDeviceDO();
        changhuiDevice.setId(device.getId());
        changhuiDevice.setDeviceName(device.getDeviceName());
        // 只有 ONLINE(1) 状态才视为在线
        changhuiDevice.setStatus(IotDeviceStateEnum.isOnline(device.getState()) ? 1 : 0);
        changhuiDevice.setLastHeartbeat(device.getOnlineTime());
        
        // 从config中提取长辉特有字段
        ChanghuiDeviceConfig config = getChanghuiConfig(device);
        if (config != null) {
            changhuiDevice.setStationCode(config.getStationCode());
            changhuiDevice.setTeaKey(config.getTeaKey());
            changhuiDevice.setPassword(config.getPassword());
            changhuiDevice.setProvinceCode(config.getProvinceCode());
            changhuiDevice.setManagementCode(config.getManagementCode());
            changhuiDevice.setStationCodePart(config.getStationCodePart());
            changhuiDevice.setPileFront(config.getPileFront());
            changhuiDevice.setPileBack(config.getPileBack());
            changhuiDevice.setManufacturer(config.getManufacturer());
            changhuiDevice.setSequenceNo(config.getSequenceNo());
            // 使用config中的长辉设备类型（而非IotDeviceDO的产品类型）
            changhuiDevice.setDeviceType(config.getChanghuiDeviceType());
        } else {
            // 如果没有config，回退使用IotDeviceDO的deviceType
            changhuiDevice.setDeviceType(device.getDeviceType());
        }
        
        return changhuiDevice;
    }

    /**
     * 填充设备响应VO的额外字段
     */
    private ChanghuiDeviceRespVO enrichDeviceRespVO(ChanghuiDeviceRespVO respVO) {
        if (respVO == null) {
            return null;
        }
        // 填充设备类型名称
        if (respVO.getDeviceType() != null) {
            ChanghuiDeviceTypeEnum deviceTypeEnum = ChanghuiDeviceTypeEnum.getByCode(respVO.getDeviceType());
            if (deviceTypeEnum != null) {
                respVO.setDeviceTypeName(deviceTypeEnum.getDescription());
            }
        }
        // 填充状态名称
        if (respVO.getStatus() != null) {
            respVO.setStatusName(respVO.getStatus() == 1 ? "在线" : "离线");
        }
        return respVO;
    }

}
