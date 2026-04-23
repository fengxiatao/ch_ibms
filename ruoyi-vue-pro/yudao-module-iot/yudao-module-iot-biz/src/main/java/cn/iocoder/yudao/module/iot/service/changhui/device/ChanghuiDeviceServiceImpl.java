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
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceRuntimeMapper;
import cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.support.IbmsLegacyIotDeviceAdapterService;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import cn.hutool.json.JSONUtil;
import java.util.List;
import java.util.Map;
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
    private IbmsLegacyIotDeviceAdapterService legacyIotDeviceAdapterService;

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;

    @Resource
    private IbmsDeviceRuntimeMapper ibmsDeviceRuntimeMapper;

    @Resource
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerDevice(ChanghuiDeviceRegisterReqVO reqVO) {
        // 验证测站编码格式（德通协议规定：10字节 = 20个十六进制字符）
        String stationCode = reqVO.getStationCode();
        if (!isValidStationCode(stationCode)) {
            throw exception(STATION_CODE_INVALID);
        }
        
        // 检查测站编码（legacy serialNumber）是否已存在
        IbmsDeviceDO existIbmsDevice = ibmsDeviceMapper.selectBySn(stationCode);
        if (existIbmsDevice != null) {
            throw exception(STATION_CODE_EXISTS);
        }
        
        // 创建设备配置
        ChanghuiDeviceConfig config = buildConfig(reqVO);
        config.validate();
        
        // 单台账：仅创建 IBMS 设备 + 回填运行态配置
        IotDeviceSaveReqVO req = new IotDeviceSaveReqVO();
        req.setDeviceName(reqVO.getDeviceName());
        req.setProductId(CHANGHUI_PRODUCT_ID);
        req.setSerialNumber(stationCode);
        req.setConfig(JSONUtil.toJsonStr(config.toMap()));
        Long deviceId = legacyIotDeviceAdapterService.createDevice(req);
        legacyIotDeviceAdapterService.updateDeviceConfig(deviceId, config);

        log.info("[registerDevice] 设备注册成功(IBMS): sn(stationCode)={}, deviceName={}",
                stationCode, reqVO.getDeviceName());
        return deviceId;
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
        Long deviceId = reqVO.getId();
        IbmsDeviceDO exist = ibmsDeviceMapper.selectById(deviceId);
        if (exist == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeMapper.selectById(deviceId);
        ChanghuiDeviceConfig existConfig = runtime != null && runtime.getConfig() instanceof ChanghuiDeviceConfig
                ? (ChanghuiDeviceConfig) runtime.getConfig()
                : null;

        // 更新设备运行态配置（写 ibms_device_runtime）
        ChanghuiDeviceConfig config = buildConfigFromUpdate(reqVO, existConfig);
        config.validate();
        legacyIotDeviceAdapterService.updateDeviceConfig(deviceId, config);

        // stationCode 如有变更：更新 sn（台账唯一标识）+ extra legacyIotConfig
        if (reqVO.getStationCode() != null && !reqVO.getStationCode().trim().isEmpty()) {
            IotDeviceSaveReqVO updateReq = new IotDeviceSaveReqVO();
            updateReq.setId(deviceId);
            updateReq.setSerialNumber(reqVO.getStationCode());
            updateReq.setConfig(JSONUtil.toJsonStr(config.toMap()));
            legacyIotDeviceAdapterService.updateDevice(updateReq);
        }

        log.info("[updateDevice] 设备更新成功(IBMS): id={}", reqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long id) {
        // 直接删除 IBMS 台账（含运行态与通道引用清理）
        IbmsDeviceDO exist = ibmsDeviceMapper.selectById(id);
        if (exist == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        legacyIotDeviceAdapterService.deleteDevice(id);
        log.info("[deleteDevice] 设备删除成功(IBMS): id={}", id);
    }

    @Override
    public ChanghuiDeviceRespVO getDevice(Long id) {
        IbmsDeviceDO ibms = ibmsDeviceMapper.selectById(id);
        if (ibms == null) {
            return null;
        }
        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeMapper.selectById(id);
        IotDeviceDO device = buildLegacyChanghuiDeviceShell(ibms, runtime);
        return enrichDeviceRespVO(convertToRespVO(device));
    }

    @Override
    public ChanghuiDeviceRespVO getDeviceByStationCode(String stationCode) {
        IotDeviceDO device = getIotDeviceByStationCode(stationCode);
        return enrichDeviceRespVO(convertToRespVO(device));
    }

    @Override
    public PageResult<ChanghuiDeviceRespVO> getDevicePage(ChanghuiDevicePageReqVO reqVO) {
        // 单台账：通过 ibms_device + ibms_device_runtime 组装 legacy 壳对象
        List<IbmsDeviceDO> ibmsDevices = ibmsDeviceMapper.selectList(
                new LambdaQueryWrapperX<IbmsDeviceDO>()
                        .eq(IbmsDeviceDO::getIbmsProductId, CHANGHUI_PRODUCT_ID)
                        .likeIfPresent(IbmsDeviceDO::getName, reqVO.getDeviceName())
                        .eqIfPresent(IbmsDeviceDO::getDeviceType, reqVO.getDeviceType())
                        .orderByDesc(IbmsDeviceDO::getId));

        if (ibmsDevices == null || ibmsDevices.isEmpty()) {
            return PageResult.empty();
        }

        List<Long> ids = ibmsDevices.stream()
                .map(IbmsDeviceDO::getId)
                .filter(java.util.Objects::nonNull)
                .toList();

        Map<Long, IbmsDeviceRuntimeDO> runtimeMap = ibmsDeviceRuntimeMapper.selectList(
                        new LambdaQueryWrapperX<IbmsDeviceRuntimeDO>().in(IbmsDeviceRuntimeDO::getDeviceId, ids))
                .stream()
                .collect(Collectors.toMap(IbmsDeviceRuntimeDO::getDeviceId, r -> r, (a, b) -> a));

        List<ChanghuiDeviceRespVO> respList = new java.util.ArrayList<>();
        for (IbmsDeviceDO ibms : ibmsDevices) {
            if (ibms == null) {
                continue;
            }
            IbmsDeviceRuntimeDO runtime = runtimeMap.get(ibms.getId());
            IotDeviceDO device = buildLegacyChanghuiDeviceShell(ibms, runtime);
            if (device == null) {
                continue;
            }

            if (reqVO.getStatus() != null) {
                boolean online = IotDeviceStateEnum.isOnline(device.getState());
                if (reqVO.getStatus() == 1 && !online) {
                    continue;
                }
                if (reqVO.getStatus() == 0 && online) {
                    continue;
                }
            }

            // stationCode 在 config JSON 中：需要内存过滤
            if (!filterByStationCode(device, reqVO.getStationCode())) {
                continue;
            }

            respList.add(enrichDeviceRespVO(convertToRespVO(device)));
        }

        int total = respList.size();
        int pageNo = reqVO.getPageNo();
        int pageSize = reqVO.getPageSize();
        int fromIndex = Math.max(0, (pageNo - 1) * pageSize);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<ChanghuiDeviceRespVO> pageList = fromIndex >= total ? List.of() : respList.subList(fromIndex, toIndex);

        return new PageResult<>(pageList, (long) total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceStatus(String stationCode, Integer status) {
        IbmsDeviceDO device = ibmsDeviceMapper.selectBySn(stationCode);
        if (device == null) {
            return;
        }
        Integer newState = status == 1 ? IotDeviceStateEnum.ONLINE.getState() : IotDeviceStateEnum.OFFLINE.getState();
        ibmsDeviceRuntimeService.patchGatewayState(device.getId(), device.getTenantId(), newState, System.currentTimeMillis());
        log.info("[updateDeviceStatus] 设备状态更新(IBMS): stationCode={}, status={}", stationCode, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceHeartbeat(String stationCode) {
        IbmsDeviceDO device = ibmsDeviceMapper.selectBySn(stationCode);
        if (device == null) {
            return;
        }
        ibmsDeviceRuntimeService.patchGatewayState(
                device.getId(),
                device.getTenantId(),
                IotDeviceStateEnum.ONLINE.getState(),
                System.currentTimeMillis());
        log.debug("[updateDeviceHeartbeat] 设备心跳更新(IBMS): stationCode={}", stationCode);
    }

    @Override
    public List<ChanghuiDeviceRespVO> getOnlineDevices() {
        // 查询在线的长辉设备（单台账收口）
        List<IbmsDeviceDO> ibmsDevices = ibmsDeviceMapper.selectList(
                new LambdaQueryWrapperX<IbmsDeviceDO>()
                        .eq(IbmsDeviceDO::getIbmsProductId, CHANGHUI_PRODUCT_ID)
                        .orderByDesc(IbmsDeviceDO::getId));

        if (ibmsDevices == null || ibmsDevices.isEmpty()) {
            return java.util.List.of();
        }

        List<Long> ids = ibmsDevices.stream()
                .map(IbmsDeviceDO::getId)
                .filter(java.util.Objects::nonNull)
                .toList();

        Map<Long, IbmsDeviceRuntimeDO> runtimeMap = ibmsDeviceRuntimeMapper.selectList(
                        new LambdaQueryWrapperX<IbmsDeviceRuntimeDO>().in(IbmsDeviceRuntimeDO::getDeviceId, ids))
                .stream()
                .collect(Collectors.toMap(IbmsDeviceRuntimeDO::getDeviceId, r -> r, (a, b) -> a));

        List<ChanghuiDeviceRespVO> respList = new java.util.ArrayList<>();
        for (IbmsDeviceDO ibms : ibmsDevices) {
            IbmsDeviceRuntimeDO runtime = runtimeMap.get(ibms.getId());
            IotDeviceDO device = buildLegacyChanghuiDeviceShell(ibms, runtime);
            if (device != null && IotDeviceStateEnum.isOnline(device.getState())) {
                respList.add(enrichDeviceRespVO(convertToRespVO(device)));
            }
        }
        return respList;
    }

    @Override
    public ChanghuiDeviceDO getDeviceDO(Long id) {
        IbmsDeviceDO ibms = ibmsDeviceMapper.selectById(id);
        if (ibms == null) {
            return null;
        }
        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeMapper.selectById(id);
        IotDeviceDO device = buildLegacyChanghuiDeviceShell(ibms, runtime);
        return convertToChanghuiDeviceDO(device);
    }

    @Override
    public ChanghuiDeviceDO getDeviceDOByStationCode(String stationCode) {
        IotDeviceDO device = getIotDeviceByStationCode(stationCode);
        return convertToChanghuiDeviceDO(device);
    }

    /**
     * 把 {@code ibms_device + ibms_device_runtime} 转成 Changhui 旧逻辑仍消费的 legacy 壳对象。
     * <p>Changhui 特有配置直接来自 {@code runtime.config}（反序列化为 {@link ChanghuiDeviceConfig}）。</p>
     */
    private IotDeviceDO buildLegacyChanghuiDeviceShell(IbmsDeviceDO ibms, IbmsDeviceRuntimeDO runtime) {
        if (ibms == null) {
            return null;
        }

        IotDeviceDO device = new IotDeviceDO();
        device.setId(ibms.getId());
        device.setTenantId(ibms.getTenantId());
        device.setSubsystemCode(ibms.getSubsystemCode());
        device.setDeviceName(ibms.getName());
        device.setNickname(ibms.getNickname());
        device.setDeviceKey(ibms.getDeviceKey());
        device.setDeviceType(ibms.getDeviceType());

        device.setProductId(ibms.getIbmsProductId());
        device.setProductKey(ibms.getProductKey());

        if (runtime != null) {
            device.setState(runtime.getState());
            device.setOnlineTime(runtime.getOnlineTime());
            device.setOfflineTime(runtime.getOfflineTime());
            device.setActiveTime(runtime.getActiveTime());
            device.setFirmwareId(runtime.getFirmwareId());
            device.setGatewayId(runtime.getGatewayId());
            device.setConfig(runtime.getConfig());
        }

        return device;
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

        // 单台账：deviceKey = stationCode（德通协议约定）
        IbmsDeviceDO ibms = ibmsDeviceMapper.selectOne(
                new LambdaQueryWrapperX<IbmsDeviceDO>()
                        .eq(IbmsDeviceDO::getDeviceKey, stationCode));
        if (ibms == null) {
            return null;
        }
        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeMapper.selectById(ibms.getId());
        return buildLegacyChanghuiDeviceShell(ibms, runtime);
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
    private ChanghuiDeviceConfig buildConfigFromUpdate(ChanghuiDeviceUpdateReqVO reqVO, ChanghuiDeviceConfig existConfig) {
        return ChanghuiDeviceConfig.builder()
                .stationCode(reqVO.getStationCode() != null ? reqVO.getStationCode()
                        : (existConfig != null ? existConfig.getStationCode() : null))
                .teaKey(reqVO.getTeaKey() != null ? reqVO.getTeaKey()
                        : (existConfig != null ? existConfig.getTeaKey() : null))
                .password(reqVO.getPassword() != null ? reqVO.getPassword()
                        : (existConfig != null ? existConfig.getPassword() : null))
                .provinceCode(reqVO.getProvinceCode() != null ? reqVO.getProvinceCode()
                        : (existConfig != null ? existConfig.getProvinceCode() : null))
                .managementCode(reqVO.getManagementCode() != null ? reqVO.getManagementCode()
                        : (existConfig != null ? existConfig.getManagementCode() : null))
                .stationCodePart(reqVO.getStationCodePart() != null ? reqVO.getStationCodePart()
                        : (existConfig != null ? existConfig.getStationCodePart() : null))
                .pileFront(reqVO.getPileFront() != null ? reqVO.getPileFront()
                        : (existConfig != null ? existConfig.getPileFront() : null))
                .pileBack(reqVO.getPileBack() != null ? reqVO.getPileBack()
                        : (existConfig != null ? existConfig.getPileBack() : null))
                .manufacturer(reqVO.getManufacturer() != null ? reqVO.getManufacturer()
                        : (existConfig != null ? existConfig.getManufacturer() : null))
                .sequenceNo(reqVO.getSequenceNo() != null ? reqVO.getSequenceNo()
                        : (existConfig != null ? existConfig.getSequenceNo() : null))
                .changhuiDeviceType(reqVO.getDeviceType() != null ? reqVO.getDeviceType()
                        : (existConfig != null ? existConfig.getChanghuiDeviceType() : null))
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
