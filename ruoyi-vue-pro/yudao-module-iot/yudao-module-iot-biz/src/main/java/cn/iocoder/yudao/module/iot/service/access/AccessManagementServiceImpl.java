package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.AccessControllerDetailVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.AccessControllerTreeVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.AuthRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.PersonDeviceAuthVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDeviceAuthDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonDeviceAuthMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.channel.IotDeviceChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.DoorControlReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.DoorControlRespVO;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlDeviceCommand;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlDeviceResponse;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import cn.iocoder.yudao.module.iot.service.access.dto.DispatchResult;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.ACCESS_DEVICE_NOT_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.ACCESS_AUTH_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.ACCESS_AUTH_RECORD_STATUS_NOT_FAILED;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.ACCESS_AUTH_RETRY_FAILED;

/**
 * 门禁管理 Service 实现类
 * 
 * 提供门禁控制器+门通道的组合查询功能
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class AccessManagementServiceImpl implements AccessManagementService {

    /** 门禁子系统代码 */
    private static final String ACCESS_SUBSYSTEM_CODE = "access";
    
    /** 门禁通道类型 */
    private static final String ACCESS_CHANNEL_TYPE = "ACCESS";

    @Resource
    private IotDeviceMapper deviceMapper;

    @Resource
    private IotDeviceChannelMapper channelMapper;

    @Resource
    private IotProductMapper productMapper;

    @Resource
    private AccessControlMessageBusClient messageBusClient;

    /**
     * 统一命令发布器
     * Requirements: 9.1 - 门禁设备命令使用统一接口
     */
    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;

    @Resource
    private IotAccessPersonDeviceAuthMapper personDeviceAuthMapper;

    @Resource
    private IotAccessPersonMapper personMapper;

    /**
     * 操作日志服务，用于记录门控操作日志
     * Requirements: 4.1, 4.2, 4.3
     */
    @Resource
    private IotAccessOperationLogService operationLogService;

    @Override
    public List<AccessControllerTreeVO> getControllerTree() {
        // 1. 查询所有门禁控制器
        List<IotDeviceDO> devices = deviceMapper.selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getSubsystemCode, ACCESS_SUBSYSTEM_CODE)
                .orderByDesc(IotDeviceDO::getId));
        
        // 2. 转换为树形结构
        return buildControllerTree(devices);
    }

    @Override
    public List<AccessControllerTreeVO> getOnlineControllerTree() {
        // 1. 查询在线的门禁控制器
        List<IotDeviceDO> devices = deviceMapper.selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getSubsystemCode, ACCESS_SUBSYSTEM_CODE)
                .eq(IotDeviceDO::getState, IotDeviceStateEnum.ONLINE.getState())
                .orderByDesc(IotDeviceDO::getId));
        
        // 2. 转换为树形结构
        return buildControllerTree(devices);
    }

    @Override
    public AccessControllerDetailVO getControllerDetail(Long deviceId) {
        // 1. 获取设备信息
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }
        if (!ACCESS_SUBSYSTEM_CODE.equals(device.getSubsystemCode())) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }
        
        // 2. 获取产品信息
        String productName = null;
        if (device.getProductId() != null) {
            IotProductDO product = productMapper.selectById(device.getProductId());
            if (product != null) {
                productName = product.getName();
            }
        }
        
        // 3. 获取通道列表
        List<IotDeviceChannelDO> channels = channelMapper.selectList(new LambdaQueryWrapperX<IotDeviceChannelDO>()
                .eq(IotDeviceChannelDO::getDeviceId, deviceId)
                .orderByAsc(IotDeviceChannelDO::getChannelNo));
        
        // 4. 构建详情VO
        return buildControllerDetail(device, productName, channels);
    }

    @Override
    public void refreshControllerStatus(Long deviceId) {
        // 验证设备存在
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }
        if (!ACCESS_SUBSYSTEM_CODE.equals(device.getSubsystemCode())) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }

        // 通过统一命令总线触发一次“在线检测/状态刷新”
        // 前端状态刷新依赖 DEVICE_STATE_CHANGED + WebSocket 推送
        String configDeviceType = null;
        Boolean supportVideo = null;
        if (device.getConfig() instanceof AccessDeviceConfig) {
            supportVideo = ((AccessDeviceConfig) device.getConfig()).getSupportVideo();
        } else if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
            Object dt = cfg.get("deviceType");
            Object sv = cfg.get("supportVideo");
            configDeviceType = dt != null ? dt.toString() : null;
            supportVideo = (sv instanceof Boolean) ? (Boolean) sv : null;
        }
        String deviceType = AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo);

        Map<String, Object> params = new HashMap<>();
        params.put("ipAddress", DeviceConfigHelper.getIpAddress(device));
        params.put("port", DeviceConfigHelper.getPort(device) != null ? DeviceConfigHelper.getPort(device) : 37777);
        // username/password 对 ACTIVE 设备在线检测通常需要；若配置里没有，newgateway 会按默认处理或失败
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig cfg = (AccessDeviceConfig) device.getConfig();
            params.put("username", cfg.getUsername());
            params.put("password", cfg.getPassword());
        } else if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
            params.put("username", cfg.get("username"));
            params.put("password", cfg.get("password"));
        }
        params.put("tenantId", device.getTenantId());

        String requestId = deviceCommandPublisher.publishCommand(deviceType, deviceId, "CHECK_DEVICE_ONLINE", params);
        log.info("[refreshControllerStatus] 已触发门禁在线检测: deviceId={}, deviceType={}, requestId={}",
                deviceId, deviceType, requestId);
    }

    /**
     * 构建控制器树形结构
     */
    private List<AccessControllerTreeVO> buildControllerTree(List<IotDeviceDO> devices) {
        List<AccessControllerTreeVO> result = new ArrayList<>();
        
        for (IotDeviceDO device : devices) {
            AccessControllerTreeVO treeVO = new AccessControllerTreeVO();
            treeVO.setDeviceId(device.getId());
            treeVO.setDeviceName(device.getDeviceName());
            treeVO.setDeviceCode(device.getDeviceKey());
            treeVO.setIpAddress(DeviceConfigHelper.getIpAddress(device));

            // 设备类型（ACCESS_GEN1/ACCESS_GEN2）：优先从 config.deviceType 读取，兜底用 supportVideo 推断
            String configDeviceType = null;
            Boolean supportVideo = null;
            if (device.getConfig() instanceof GenericDeviceConfig) {
                GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
                Object dt = cfg.get("deviceType");
                Object sv = cfg.get("supportVideo");
                configDeviceType = dt != null ? dt.toString() : null;
                supportVideo = (sv instanceof Boolean) ? (Boolean) sv : null;
            } else if (device.getConfig() instanceof AccessDeviceConfig) {
                supportVideo = ((AccessDeviceConfig) device.getConfig()).getSupportVideo();
            }
            treeVO.setDeviceType(AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo));
            
            // 解析端口
            Integer port = 37777;
            if (device.getConfig() != null && device.getConfig() instanceof AccessDeviceConfig) {
                AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
                if (config.getPort() != null) {
                    port = config.getPort();
                }
            }
            treeVO.setPort(port);
            
            // 设置状态
            boolean online = IotDeviceStateEnum.ONLINE.getState().equals(device.getState());
            treeVO.setOnline(online);
            treeVO.setState(device.getState());
            treeVO.setStateDesc(getStateDesc(device.getState()));
            treeVO.setLastOnlineTime(device.getOnlineTime());
            
            // 查询门通道
            List<IotDeviceChannelDO> channels = channelMapper.selectList(new LambdaQueryWrapperX<IotDeviceChannelDO>()
                    .eq(IotDeviceChannelDO::getDeviceId, device.getId())
                    .orderByAsc(IotDeviceChannelDO::getChannelNo));
            
            treeVO.setChannelCount(channels.size());
            treeVO.setChannels(buildDoorChannels(channels, online));
            
            // 设置视频支持状态
            if (device.getConfig() != null && device.getConfig() instanceof AccessDeviceConfig) {
                AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
                treeVO.setSupportVideo(Boolean.TRUE.equals(config.getSupportVideo()));
            } else {
                treeVO.setSupportVideo(false);
            }
            
            result.add(treeVO);
        }
        
        return result;
    }

    /**
     * 构建门通道列表
     */
    private List<AccessControllerTreeVO.DoorChannelVO> buildDoorChannels(List<IotDeviceChannelDO> channels, boolean deviceOnline) {
        List<AccessControllerTreeVO.DoorChannelVO> result = new ArrayList<>();
        
        for (IotDeviceChannelDO channel : channels) {
            AccessControllerTreeVO.DoorChannelVO channelVO = new AccessControllerTreeVO.DoorChannelVO();
            channelVO.setChannelId(channel.getId());
            channelVO.setChannelNo(channel.getChannelNo());
            channelVO.setChannelName(channel.getChannelName());
            
            // 解析门状态
            Integer doorStatus = parseDoorStatus(channel);
            channelVO.setDoorStatus(doorStatus);
            channelVO.setDoorStatusDesc(getDoorStatusDesc(doorStatus));
            
            // 解析锁状态
            Integer lockStatus = parseLockStatus(channel);
            channelVO.setLockStatus(lockStatus);
            channelVO.setLockStatusDesc(getLockStatusDesc(lockStatus));
            
            // 解析常开常闭模式
            Integer alwaysMode = parseAlwaysMode(channel);
            channelVO.setAlwaysMode(alwaysMode);
            channelVO.setAlwaysModeDesc(getAlwaysModeDesc(alwaysMode));
            
            // 设备在线时通道可操作
            channelVO.setOperable(deviceOnline);
            
            result.add(channelVO);
        }
        
        return result;
    }

    /**
     * 构建控制器详情
     */
    private AccessControllerDetailVO buildControllerDetail(IotDeviceDO device, String productName, 
                                                           List<IotDeviceChannelDO> channels) {
        AccessControllerDetailVO detailVO = new AccessControllerDetailVO();
        
        // 基本信息
        detailVO.setDeviceId(device.getId());
        detailVO.setDeviceName(device.getDeviceName());
        detailVO.setDeviceCode(device.getDeviceKey());
        detailVO.setProductId(device.getProductId());
        detailVO.setProductName(productName);
        detailVO.setIpAddress(DeviceConfigHelper.getIpAddress(device));

        // 设备类型（ACCESS_GEN1/ACCESS_GEN2）：优先从 config.deviceType 读取，兜底用 supportVideo 推断
        String configDeviceType = null;
        Boolean supportVideo = null;
        if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
            Object dt = cfg.get("deviceType");
            Object sv = cfg.get("supportVideo");
            configDeviceType = dt != null ? dt.toString() : null;
            supportVideo = (sv instanceof Boolean) ? (Boolean) sv : null;
        } else if (device.getConfig() instanceof AccessDeviceConfig) {
            supportVideo = ((AccessDeviceConfig) device.getConfig()).getSupportVideo();
        }
        detailVO.setDeviceType(AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo));
        
        // 解析连接配置
        Integer port = 37777;
        String username = "admin";
        if (device.getConfig() != null && device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            if (config.getPort() != null) port = config.getPort();
            if (config.getUsername() != null) username = config.getUsername();
        }
        detailVO.setPort(port);
        detailVO.setUsername(username);
        
        // 状态信息
        boolean online = IotDeviceStateEnum.ONLINE.getState().equals(device.getState());
        detailVO.setOnline(online);
        detailVO.setState(device.getState());
        detailVO.setStateDesc(getStateDesc(device.getState()));
        detailVO.setLastOnlineTime(device.getOnlineTime());
        detailVO.setLastOfflineTime(device.getOfflineTime());
        detailVO.setActiveTime(device.getActiveTime());
        
        // 计算在线时长
        if (online && device.getOnlineTime() != null) {
            detailVO.setOnlineDuration(Duration.between(device.getOnlineTime(), LocalDateTime.now()).getSeconds());
        }
        
        // 设备能力集（默认值）
        detailVO.setCapabilities(buildDefaultCapabilities());
        
        // 设置视频支持状态（只有二代门禁如人脸一体机才支持视频，一代门禁控制器不支持）
        if (device.getConfig() != null && device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            detailVO.setSupportVideo(Boolean.TRUE.equals(config.getSupportVideo()));
        } else {
            detailVO.setSupportVideo(false);
        }
        
        // 通道信息
        detailVO.setChannelCount(channels.size());
        detailVO.setChannels(buildDoorChannelDetails(channels, online));
        
        // 其他信息
        detailVO.setCreateTime(device.getCreateTime());
        
        return detailVO;
    }

    /**
     * 构建门通道详情列表
     */
    private List<AccessControllerDetailVO.DoorChannelDetailVO> buildDoorChannelDetails(
            List<IotDeviceChannelDO> channels, boolean deviceOnline) {
        List<AccessControllerDetailVO.DoorChannelDetailVO> result = new ArrayList<>();
        
        for (IotDeviceChannelDO channel : channels) {
            AccessControllerDetailVO.DoorChannelDetailVO channelVO = new AccessControllerDetailVO.DoorChannelDetailVO();
            channelVO.setChannelId(channel.getId());
            channelVO.setChannelNo(channel.getChannelNo());
            channelVO.setChannelName(channel.getChannelName());
            
            // 解析门状态
            Integer doorStatus = parseDoorStatus(channel);
            channelVO.setDoorStatus(doorStatus);
            channelVO.setDoorStatusDesc(getDoorStatusDesc(doorStatus));
            
            // 解析锁状态
            Integer lockStatus = parseLockStatus(channel);
            channelVO.setLockStatus(lockStatus);
            channelVO.setLockStatusDesc(getLockStatusDesc(lockStatus));
            
            // 解析常开常闭模式
            Integer alwaysMode = parseAlwaysMode(channel);
            channelVO.setAlwaysMode(alwaysMode);
            channelVO.setAlwaysModeDesc(getAlwaysModeDesc(alwaysMode));
            
            // 解析开门时长和报警时长
            Map<String, Object> config = channel.getConfig();
            if (config != null) {
                channelVO.setOpenDuration(getIntFromConfig(config, "openDuration", 5));
                channelVO.setAlarmDuration(getIntFromConfig(config, "alarmDuration", 30));
            } else {
                channelVO.setOpenDuration(5);
                channelVO.setAlarmDuration(30);
            }
            
            // 设备在线时通道可操作
            channelVO.setOperable(deviceOnline);
            
            // 位置信息
            channelVO.setLocation(channel.getLocation());
            
            result.add(channelVO);
        }
        
        return result;
    }

    /**
     * 构建默认设备能力集
     */
    private AccessControllerDetailVO.DeviceCapabilities buildDefaultCapabilities() {
        AccessControllerDetailVO.DeviceCapabilities capabilities = new AccessControllerDetailVO.DeviceCapabilities();
        capabilities.setSupportPassword(true);
        capabilities.setSupportCard(true);
        capabilities.setSupportFingerprint(true);
        capabilities.setSupportFace(true);
        capabilities.setSupportQrCode(false);
        capabilities.setSupportRemoteOpen(true);
        capabilities.setSupportAlwaysMode(true);
        capabilities.setMaxUsers(10000);
        capabilities.setMaxCards(10000);
        capabilities.setMaxFingerprints(3000);
        capabilities.setMaxFaces(3000);
        return capabilities;
    }

    // ========== 状态解析辅助方法 ==========

    private Integer parseDoorStatus(IotDeviceChannelDO channel) {
        Map<String, Object> config = channel.getConfig();
        if (config != null && config.containsKey("doorStatus")) {
            Object status = config.get("doorStatus");
            if (status instanceof Integer) {
                return (Integer) status;
            }
            if (status instanceof String) {
                String s = (String) status;
                if ("open".equalsIgnoreCase(s) || "打开".equals(s)) return 1;
                if ("closed".equalsIgnoreCase(s) || "关闭".equals(s)) return 0;
            }
        }
        return 2; // 未知
    }

    private Integer parseLockStatus(IotDeviceChannelDO channel) {
        Map<String, Object> config = channel.getConfig();
        if (config != null && config.containsKey("lockStatus")) {
            Object status = config.get("lockStatus");
            if (status instanceof Integer) {
                return (Integer) status;
            }
            if (status instanceof String) {
                String s = (String) status;
                if ("locked".equalsIgnoreCase(s) || "锁定".equals(s) || "已锁".equals(s)) return 0;
                if ("unlocked".equalsIgnoreCase(s) || "解锁".equals(s) || "已解锁".equals(s)) return 1;
            }
        }
        return 2; // 未知
    }

    private Integer parseAlwaysMode(IotDeviceChannelDO channel) {
        Map<String, Object> config = channel.getConfig();
        if (config != null && config.containsKey("alwaysMode")) {
            Object mode = config.get("alwaysMode");
            if (mode instanceof Integer) {
                return (Integer) mode;
            }
            if (mode instanceof String) {
                String s = (String) mode;
                if ("alwaysOpen".equalsIgnoreCase(s) || "常开".equals(s)) return 1;
                if ("alwaysClosed".equalsIgnoreCase(s) || "常闭".equals(s)) return 2;
                if ("normal".equalsIgnoreCase(s) || "正常".equals(s)) return 0;
            }
        }
        return 0; // 正常
    }

    private String getStateDesc(Integer state) {
        if (state == null) return "未知";
        if (state.equals(IotDeviceStateEnum.ONLINE.getState())) return "在线";
        if (state.equals(IotDeviceStateEnum.OFFLINE.getState())) return "离线";
        if (state.equals(IotDeviceStateEnum.INACTIVE.getState())) return "未激活";
        return "未知";
    }

    private String getDoorStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "关闭";
            case 1: return "打开";
            default: return "未知";
        }
    }

    private String getLockStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已锁";
            case 1: return "已解锁";
            default: return "未知";
        }
    }

    private String getAlwaysModeDesc(Integer mode) {
        if (mode == null) return "正常";
        switch (mode) {
            case 0: return "正常";
            case 1: return "常开";
            case 2: return "常闭";
            default: return "正常";
        }
    }

    private Integer getIntFromConfig(Map<String, Object> config, String key, Integer defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        Object value = config.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    // ========== 门控操作方法 ==========

    @Override
    public DoorControlRespVO doorControl(DoorControlReqVO reqVO) {
        long startTime = System.currentTimeMillis();
        
        // 1. 验证设备存在
        IotDeviceDO device = deviceMapper.selectById(reqVO.getDeviceId());
        if (device == null) {
            return DoorControlRespVO.failure(reqVO.getDeviceId(), reqVO.getChannelId(), 
                    reqVO.getAction(), "设备不存在");
        }
        if (!ACCESS_SUBSYSTEM_CODE.equals(device.getSubsystemCode())) {
            return DoorControlRespVO.failure(reqVO.getDeviceId(), reqVO.getChannelId(), 
                    reqVO.getAction(), "设备不是门禁设备");
        }

        // 2. 验证通道存在
        IotDeviceChannelDO channel = channelMapper.selectById(reqVO.getChannelId());
        if (channel == null || !channel.getDeviceId().equals(reqVO.getDeviceId())) {
            return DoorControlRespVO.failure(reqVO.getDeviceId(), reqVO.getChannelId(), 
                    reqVO.getAction(), "通道不存在或不属于该设备");
        }

        // 3. 获取设备连接配置
        Integer port = 37777;
        String username = "admin";
        String password = "";
        Boolean supportVideo = false;
        if (device.getConfig() != null) {
            log.info("[doorControl] 设备配置类型: {}, deviceId={}", 
                    device.getConfig().getClass().getSimpleName(), reqVO.getDeviceId());
            if (device.getConfig() instanceof AccessDeviceConfig) {
                AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
                if (config.getPort() != null) port = config.getPort();
                if (config.getUsername() != null) username = config.getUsername();
                if (config.getPassword() != null) password = config.getPassword();
                supportVideo = config.getSupportVideo();
                log.info("[doorControl] AccessDeviceConfig: port={}, username={}, 密码长度={}, supportVideo={}", 
                        port, username, password != null ? password.length() : 0, supportVideo);
            } else if (device.getConfig() instanceof GenericDeviceConfig) {
                // 兼容 GenericDeviceConfig（deviceType=GENERIC 的情况）
                GenericDeviceConfig config = (GenericDeviceConfig) device.getConfig();
                Object portObj = config.get("port");
                Object usernameObj = config.get("username");
                Object passwordObj = config.get("password");
                Object supportVideoObj = config.get("supportVideo");
                // config.deviceType 可能直接是 ACCESS_GEN1/ACCESS_GEN2，用于准确路由插件
                log.info("[doorControl] GenericDeviceConfig原始值: port={}, username={}, password={}", 
                        portObj, usernameObj, passwordObj);
                if (portObj instanceof Integer) port = (Integer) portObj;
                else if (portObj instanceof Number) port = ((Number) portObj).intValue();
                if (usernameObj instanceof String) username = (String) usernameObj;
                if (passwordObj instanceof String) password = (String) passwordObj;
                if (supportVideoObj instanceof Boolean) supportVideo = (Boolean) supportVideoObj;
                log.info("[doorControl] GenericDeviceConfig解析后: port={}, username={}, 密码长度={}", 
                        port, username, password != null ? password.length() : 0);
            }
        } else {
            log.warn("[doorControl] 设备配置为空: deviceId={}", reqVO.getDeviceId());
        }

        // 4. 映射操作类型到命令类型
        String commandType = mapActionToCommandType(reqVO.getAction());
        if (commandType == null) {
            return DoorControlRespVO.failure(reqVO.getDeviceId(), reqVO.getChannelId(), 
                    reqVO.getAction(), "不支持的操作类型: " + reqVO.getAction());
        }

        // 5. 确定设备类型（ACCESS_GEN1 或 ACCESS_GEN2）
        // 现场可能在 config.deviceType 中直接标识 ACCESS_GEN1/ACCESS_GEN2，优先使用
        String configDeviceType = null;
        if (device.getConfig() instanceof GenericDeviceConfig) {
            Object dt = ((GenericDeviceConfig) device.getConfig()).get("deviceType");
            configDeviceType = dt != null ? dt.toString() : null;
        }
        String deviceType = AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo);

        // 6. 构建命令参数
        Integer channelNo = reqVO.getChannelNo() != null ? reqVO.getChannelNo() : channel.getChannelNo();
        Map<String, Object> params = new HashMap<>();
        params.put("channelId", reqVO.getChannelId());
        params.put("channelNo", channelNo);
        params.put("ipAddress", DeviceConfigHelper.getIpAddress(device));
        params.put("port", port);
        params.put("username", username);
        params.put("password", password);

        // 7. 获取当前操作人信息（用于记录操作日志）
        Long operatorId = getLoginUserId();
        String operatorName = getLoginUserNickname();
        
        // 获取设备名称和通道名称（用于操作日志）
        String deviceName = device.getDeviceName();
        String channelName = channel.getChannelName();

        // 8. 使用统一命令发布器发送命令
        // Requirements: 9.1 - 门禁设备命令通过 DEVICE_SERVICE_INVOKE 主题发送
        try {
            log.info("[doorControl] 执行门控操作: deviceId={}, channelId={}, action={}, deviceType={}, operator={}", 
                    reqVO.getDeviceId(), reqVO.getChannelId(), reqVO.getAction(), deviceType, operatorName);
            
            // 发送命令到统一主题
            String requestId = deviceCommandPublisher.publishCommand(deviceType, reqVO.getDeviceId(), commandType, params);
            log.info("[doorControl] 命令已发布: requestId={}, deviceType={}, commandType={}", 
                    requestId, deviceType, commandType);
            
            // 9. 等待响应（使用现有的消息总线客户端）
            // TODO: 后续迁移到统一的 DeviceServiceResultConsumer 处理响应
            // 构建兼容的命令对象用于等待响应
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .requestId(requestId)
                    .deviceId(reqVO.getDeviceId())
                    .channelId(reqVO.getChannelId())
                    .channelNo(channelNo)
                    .ipAddress(DeviceConfigHelper.getIpAddress(device))
                    .port(port)
                    .username(username)
                    .password(password)
                    .commandType(commandType)
                    .build();
            
            AccessControlDeviceResponse response = messageBusClient.sendAndWait(command, 10);
            long duration = System.currentTimeMillis() - startTime;
            
            if (response != null && Boolean.TRUE.equals(response.getSuccess())) {
                log.info("[doorControl] 门控操作成功: deviceId={}, channelId={}, action={}, duration={}ms", 
                        reqVO.getDeviceId(), reqVO.getChannelId(), reqVO.getAction(), duration);
                
                // 记录成功操作日志 (Requirements: 4.1)
                logOperationSafely(reqVO.getDeviceId(), deviceName, reqVO.getChannelId(), channelName,
                        reqVO.getAction(), operatorId, operatorName, 1, null);

                // 门控命令后的“状态回推校验点”：异步触发一次 QUERY_CHANNELS，同步通道状态到 DB
                triggerAccessChannelSyncAfterDoorControl(deviceType, reqVO.getDeviceId(), params);
                
                return DoorControlRespVO.success(reqVO.getDeviceId(), reqVO.getChannelId(), 
                        reqVO.getAction(), duration);
            } else {
                String errorMsg = response != null ? response.getErrorMessage() : "操作失败";
                log.warn("[doorControl] 门控操作失败: deviceId={}, channelId={}, action={}, error={}", 
                        reqVO.getDeviceId(), reqVO.getChannelId(), reqVO.getAction(), errorMsg);
                
                // 记录失败操作日志 (Requirements: 4.2)
                logOperationSafely(reqVO.getDeviceId(), deviceName, reqVO.getChannelId(), channelName,
                        reqVO.getAction(), operatorId, operatorName, 0, errorMsg);

                // 失败也触发一次通道查询，便于校验真实设备状态是否已变化（避免“假失败”）
                triggerAccessChannelSyncAfterDoorControl(deviceType, reqVO.getDeviceId(), params);
                
                return DoorControlRespVO.failure(reqVO.getDeviceId(), reqVO.getChannelId(), 
                        reqVO.getAction(), errorMsg);
            }
        } catch (TimeoutException e) {
            log.warn("[doorControl] 门控操作超时: deviceId={}, channelId={}, action={}", 
                    reqVO.getDeviceId(), reqVO.getChannelId(), reqVO.getAction());
            
            // 记录超时操作日志 (Requirements: 4.3)
            logOperationSafely(reqVO.getDeviceId(), deviceName, reqVO.getChannelId(), channelName,
                    reqVO.getAction(), operatorId, operatorName, 0, "操作超时，请检查设备连接状态");

            // 超时同样触发一次通道查询，用于校验真实设备是否已执行（避免“假失败”）
            triggerAccessChannelSyncAfterDoorControl(deviceType, reqVO.getDeviceId(), params);
            
            return DoorControlRespVO.failure(reqVO.getDeviceId(), reqVO.getChannelId(), 
                    reqVO.getAction(), "操作超时，请检查设备连接状态");
        } catch (Exception e) {
            log.error("[doorControl] 门控操作异常: deviceId={}, channelId={}, action={}", 
                    reqVO.getDeviceId(), reqVO.getChannelId(), reqVO.getAction(), e);
            
            // 记录异常操作日志
            logOperationSafely(reqVO.getDeviceId(), deviceName, reqVO.getChannelId(), channelName,
                    reqVO.getAction(), operatorId, operatorName, 0, "操作异常: " + e.getMessage());

            // 异常兜底：也尝试触发一次通道查询，辅助排障
            triggerAccessChannelSyncAfterDoorControl(deviceType, reqVO.getDeviceId(), params);
            
            return DoorControlRespVO.failure(reqVO.getDeviceId(), reqVO.getChannelId(), 
                    reqVO.getAction(), "操作异常: " + e.getMessage());
        }
    }

    /**
     * 门控命令后触发一次门禁通道同步（QUERY_CHANNELS）
     *
     * <p>目的：提供“状态回推校验点”，让门状态/常开常闭配置变化能尽快落库并被前端感知。</p>
     */
    private void triggerAccessChannelSyncAfterDoorControl(String deviceType, Long deviceId, Map<String, Object> params) {
        if (deviceId == null || deviceType == null || deviceType.isEmpty()) {
            return;
        }
        if (deviceCommandPublisher == null) {
            log.debug("[doorControl] deviceCommandPublisher 未注入，跳过 QUERY_CHANNELS 触发: deviceId={}", deviceId);
            return;
        }
        // 异步延迟，给设备/网关留一点时间完成状态落地
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1500L);
                String requestId = deviceCommandPublisher.publishCommand(deviceType, deviceId, "QUERY_CHANNELS", params);
                log.info("[doorControl] 已触发 QUERY_CHANNELS 校验: deviceId={}, deviceType={}, requestId={}",
                        deviceId, deviceType, requestId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.warn("[doorControl] 触发 QUERY_CHANNELS 校验失败: deviceId={}, deviceType={}, error={}",
                        deviceId, deviceType, e.getMessage(), e);
            }
        });
    }

    /**
     * 安全地记录操作日志（不影响主流程）
     * 
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param channelId 通道ID
     * @param channelName 通道名称
     * @param operationType 操作类型
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param result 结果（1=成功, 0=失败）
     * @param errorMessage 错误信息
     */
    private void logOperationSafely(Long deviceId, String deviceName, Long channelId, String channelName,
                                    String operationType, Long operatorId, String operatorName, 
                                    Integer result, String errorMessage) {
        try {
            operationLogService.logOperation(deviceId, deviceName, channelId, channelName,
                    operationType, operatorId, operatorName, result, errorMessage);
            log.debug("[logOperationSafely] 操作日志记录成功: deviceId={}, channelId={}, channelName={}, operationType={}, result={}",
                    deviceId, channelId, channelName, operationType, result);
        } catch (Exception e) {
            // 记录日志失败不影响主流程
            log.error("[logOperationSafely] 记录操作日志失败: deviceId={}, channelId={}, operationType={}, error={}",
                    deviceId, channelId, operationType, e.getMessage(), e);
        }
    }

    /**
     * 映射操作类型到命令类型
     */
    private String mapActionToCommandType(String action) {
        if (action == null) {
            return null;
        }
        switch (action) {
            case DoorControlReqVO.Action.OPEN_DOOR:
                return AccessControlDeviceCommand.CommandType.OPEN_DOOR;
            case DoorControlReqVO.Action.CLOSE_DOOR:
                return AccessControlDeviceCommand.CommandType.CLOSE_DOOR;
            case DoorControlReqVO.Action.ALWAYS_OPEN:
                return AccessControlDeviceCommand.CommandType.ALWAYS_OPEN;
            case DoorControlReqVO.Action.ALWAYS_CLOSED:
                return AccessControlDeviceCommand.CommandType.ALWAYS_CLOSED;
            case DoorControlReqVO.Action.CANCEL_ALWAYS:
                return AccessControlDeviceCommand.CommandType.CANCEL_ALWAYS;
            default:
                return null;
        }
    }

    // ========== 授权记录查询方法 (Requirements: 7.1, 7.2) ==========

    @Override
    public PageResult<PersonDeviceAuthVO> getAuthRecordPage(AuthRecordPageReqVO reqVO) {
        // 1. 构建查询条件
        LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO> queryWrapper = new LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO>()
                .eqIfPresent(IotAccessPersonDeviceAuthDO::getPersonId, reqVO.getPersonId())
                .eqIfPresent(IotAccessPersonDeviceAuthDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotAccessPersonDeviceAuthDO::getChannelId, reqVO.getChannelId())
                .eqIfPresent(IotAccessPersonDeviceAuthDO::getAuthStatus, reqVO.getAuthStatus())
                .betweenIfPresent(IotAccessPersonDeviceAuthDO::getCreateTime, 
                        reqVO.getCreateTimeStart(), reqVO.getCreateTimeEnd())
                .orderByDesc(IotAccessPersonDeviceAuthDO::getUpdateTime);

        // 2. 执行分页查询
        IPage<IotAccessPersonDeviceAuthDO> page = personDeviceAuthMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), queryWrapper);

        if (page.getRecords().isEmpty()) {
            return new PageResult<>(new ArrayList<>(), page.getTotal());
        }

        // 3. 收集关联ID
        Set<Long> personIds = page.getRecords().stream()
                .map(IotAccessPersonDeviceAuthDO::getPersonId)
                .collect(Collectors.toSet());
        Set<Long> deviceIds = page.getRecords().stream()
                .map(IotAccessPersonDeviceAuthDO::getDeviceId)
                .collect(Collectors.toSet());
        Set<Long> channelIds = page.getRecords().stream()
                .map(IotAccessPersonDeviceAuthDO::getChannelId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 4. 批量查询关联数据
        Map<Long, IotAccessPersonDO> personMap = CollectionUtils.isEmpty(personIds) 
                ? Map.of() 
                : personMapper.selectBatchIds(personIds).stream()
                        .collect(Collectors.toMap(IotAccessPersonDO::getId, Function.identity()));
        
        Map<Long, IotDeviceDO> deviceMap = CollectionUtils.isEmpty(deviceIds) 
                ? Map.of() 
                : deviceMapper.selectBatchIds(deviceIds).stream()
                        .collect(Collectors.toMap(IotDeviceDO::getId, Function.identity()));
        
        Map<Long, IotDeviceChannelDO> channelMap = CollectionUtils.isEmpty(channelIds) 
                ? Map.of() 
                : channelMapper.selectBatchIds(channelIds).stream()
                        .collect(Collectors.toMap(IotDeviceChannelDO::getId, Function.identity()));

        // 5. 根据人员姓名和设备名称进行过滤（如果有）
        List<IotAccessPersonDeviceAuthDO> filteredRecords = page.getRecords();
        if (reqVO.getPersonName() != null && !reqVO.getPersonName().isEmpty()) {
            filteredRecords = filteredRecords.stream()
                    .filter(auth -> {
                        IotAccessPersonDO person = personMap.get(auth.getPersonId());
                        return person != null && person.getPersonName() != null 
                                && person.getPersonName().contains(reqVO.getPersonName());
                    })
                    .collect(Collectors.toList());
        }
        if (reqVO.getDeviceName() != null && !reqVO.getDeviceName().isEmpty()) {
            filteredRecords = filteredRecords.stream()
                    .filter(auth -> {
                        IotDeviceDO device = deviceMap.get(auth.getDeviceId());
                        return device != null && device.getDeviceName() != null 
                                && device.getDeviceName().contains(reqVO.getDeviceName());
                    })
                    .collect(Collectors.toList());
        }

        // 6. 转换为VO
        List<PersonDeviceAuthVO> voList = filteredRecords.stream()
                .map(auth -> convertToPersonDeviceAuthVO(auth, personMap, deviceMap, channelMap))
                .collect(Collectors.toList());

        return new PageResult<>(voList, page.getTotal());
    }

    /**
     * 转换授权记录DO为VO
     * Requirements: 7.2 - 显示人员姓名、设备名称、通道名称、授权状态、下发时间
     */
    private PersonDeviceAuthVO convertToPersonDeviceAuthVO(
            IotAccessPersonDeviceAuthDO auth,
            Map<Long, IotAccessPersonDO> personMap,
            Map<Long, IotDeviceDO> deviceMap,
            Map<Long, IotDeviceChannelDO> channelMap) {
        
        PersonDeviceAuthVO vo = new PersonDeviceAuthVO();
        vo.setId(auth.getId());
        vo.setPersonId(auth.getPersonId());
        vo.setDeviceId(auth.getDeviceId());
        vo.setChannelId(auth.getChannelId());
        vo.setAuthStatus(auth.getAuthStatus());
        vo.setAuthStatusName(PersonDeviceAuthVO.getAuthStatusName(auth.getAuthStatus()));
        vo.setResult(auth.getLastDispatchResult());
        vo.setLastDispatchTime(auth.getLastDispatchTime());
        vo.setCreateTime(auth.getCreateTime());
        vo.setUpdateTime(auth.getUpdateTime());

        // 关联人员信息
        IotAccessPersonDO person = personMap.get(auth.getPersonId());
        if (person != null) {
            vo.setPersonName(person.getPersonName());
            vo.setPersonCode(person.getPersonCode());
        } else {
            vo.setPersonName("未知人员");
        }

        // 关联设备信息
        IotDeviceDO device = deviceMap.get(auth.getDeviceId());
        if (device != null) {
            vo.setDeviceName(device.getDeviceName());
        } else {
            vo.setDeviceName("未知设备");
        }

        // 关联通道信息
        if (auth.getChannelId() != null) {
            IotDeviceChannelDO channel = channelMap.get(auth.getChannelId());
            if (channel != null) {
                vo.setChannelName(channel.getChannelName());
            } else {
                vo.setChannelName("未知通道");
            }
        }

        return vo;
    }

    @Resource
    private IotAccessAuthDispatchService authDispatchService;

    @Override
    public void retryFailedAuth(Long authId) {
        // 1. 查询授权记录
        IotAccessPersonDeviceAuthDO auth = personDeviceAuthMapper.selectById(authId);
        if (auth == null) {
            log.warn("[retryFailedAuth] 授权记录不存在: authId={}", authId);
            throw exception(ACCESS_AUTH_RECORD_NOT_EXISTS);
        }

        // 2. 检查状态是否为失败
        if (auth.getAuthStatus() == null || auth.getAuthStatus() != IotAccessPersonDeviceAuthDO.AUTH_STATUS_FAILED) {
            log.warn("[retryFailedAuth] 授权记录状态不是失败: authId={}, status={}", authId, auth.getAuthStatus());
            throw exception(ACCESS_AUTH_RECORD_STATUS_NOT_FAILED);
        }

        // 3. 更新状态为授权中
        auth.setAuthStatus(IotAccessPersonDeviceAuthDO.AUTH_STATUS_AUTHORIZING);
        auth.setLastDispatchResult("重试中...");
        auth.setLastDispatchTime(LocalDateTime.now());
        personDeviceAuthMapper.updateById(auth);

        log.info("[retryFailedAuth] 开始重试授权: authId={}, personId={}, deviceId={}, channelId={}", 
                authId, auth.getPersonId(), auth.getDeviceId(), auth.getChannelId());

        // 4. 调用授权下发服务重新执行授权
        try {
            DispatchResult result = authDispatchService.dispatchPersonToDevice(
                    auth.getPersonId(), auth.getDeviceId(), auth.getChannelId());
            
            // 5. 根据下发结果更新授权记录状态
            if (result.isSuccess()) {
                auth.setAuthStatus(IotAccessPersonDeviceAuthDO.AUTH_STATUS_AUTHORIZED);
                auth.setLastDispatchResult("重试成功");
                log.info("[retryFailedAuth] 重试授权成功: authId={}, personId={}, deviceId={}", 
                        authId, auth.getPersonId(), auth.getDeviceId());
            } else {
                auth.setAuthStatus(IotAccessPersonDeviceAuthDO.AUTH_STATUS_FAILED);
                auth.setLastDispatchResult("重试失败: " + result.getErrorMessage());
                log.warn("[retryFailedAuth] 重试授权失败: authId={}, personId={}, deviceId={}, error={}", 
                        authId, auth.getPersonId(), auth.getDeviceId(), result.getErrorMessage());
            }
            auth.setLastDispatchTime(LocalDateTime.now());
            personDeviceAuthMapper.updateById(auth);
            
        } catch (Exception e) {
            // 6. 异常情况下更新为失败状态
            auth.setAuthStatus(IotAccessPersonDeviceAuthDO.AUTH_STATUS_FAILED);
            auth.setLastDispatchResult("重试异常: " + e.getMessage());
            auth.setLastDispatchTime(LocalDateTime.now());
            personDeviceAuthMapper.updateById(auth);
            
            log.error("[retryFailedAuth] 重试授权异常: authId={}, personId={}, deviceId={}", 
                    authId, auth.getPersonId(), auth.getDeviceId(), e);
            throw exception(ACCESS_AUTH_RETRY_FAILED);
        }
    }

}
