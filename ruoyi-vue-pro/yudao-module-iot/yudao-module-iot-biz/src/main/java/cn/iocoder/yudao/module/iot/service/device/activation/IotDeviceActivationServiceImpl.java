package cn.iocoder.yudao.module.iot.service.device.activation;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.service.device.discovery.DiscoveredDeviceService;
import cn.iocoder.yudao.module.iot.service.device.discovery.dto.DiscoveredDeviceDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * IoT Device Activation Service Implementation
 *
 * @author Changhui Information Technology Co., Ltd.
 */
@Service
@Slf4j
public class IotDeviceActivationServiceImpl implements IotDeviceActivationService {
    
    /**
     * 激活状态管理器（统一管理激活状态，消除重复逻辑）
     */
    @Resource
    private DeviceActivationStateManager activationStateManager;
    
    @Resource
    private IotDeviceMapper deviceMapper;
    
    @Resource
    private IotProductMapper productMapper;
    
    @Resource
    private IotMessageBus messageBus;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private DiscoveredDeviceService discoveredDeviceService;
    
    @Resource
    private org.springframework.context.ApplicationEventPublisher eventPublisher;
    
    @Resource
    private cn.iocoder.yudao.module.iot.dal.mysql.channel.IotDeviceChannelMapper channelMapper;
    
    @Resource
    private cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher deviceCommandPublisher;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String activateDevice(DiscoveredDeviceDTO discoveredDevice, Long productId, 
                                  String username, String password) {
        // Generate activation ID
        String activationId = UUID.randomUUID().toString();
        
        log.info("[activateDevice] Starting device activation: activationId={}, ip={}, productId={}", 
                activationId, discoveredDevice.getIpAddress(), productId);
        
        // 1. Verify product exists
        IotProductDO product = productMapper.selectById(productId);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        
        // 2. Device verification will be done asynchronously via message bus
        String vendor = discoveredDevice.getVendor();
        String productName = product.getName();
        
        // Determine device type and verification method
        if (productName != null && (productName.contains("NVR") || productName.contains("DVR"))) {
            if ("Dahua".equalsIgnoreCase(vendor)) {
                vendor = "dahua_sdk";
                log.info("[activateDevice] Detected Dahua NVR/DVR, using Dahua SDK: ip={}, productName={}", 
                        discoveredDevice.getIpAddress(), productName);
            } else if ("Hikvision".equalsIgnoreCase(vendor)) {
                vendor = "hikvision_sdk";
                log.info("[activateDevice] Detected Hikvision NVR/DVR, using Hikvision SDK: ip={}, productName={}", 
                        discoveredDevice.getIpAddress(), productName);
            } else {
                log.info("[activateDevice] NVR/DVR with unknown vendor, trying ONVIF: ip={}, vendor={}, productName={}", 
                        discoveredDevice.getIpAddress(), vendor, productName);
            }
        } else {
            log.info("[activateDevice] Verifying device via message bus: ip={}, productName={}", 
                    discoveredDevice.getIpAddress(), productName);
        }

        // 3. Find or create device record
        IotDeviceDO device = findOrCreateDevice(product, discoveredDevice, username, password);
        
        // 4. Determine channel strategy
        DeviceChannelStrategy strategy = determineChannelStrategy(device, discoveredDevice);
        log.info("[activateDevice] Device channel strategy: deviceId={}, strategy={}", device.getId(), strategy);
        
        switch (strategy) {
            case NVR_DEVICE:
                log.info("[activateDevice] NVR device, will sync channels after online");
                activationStateManager.markNeedsSyncChannel(device.getId());
                break;
                
            case IPC_SINGLE_CHANNEL:
                log.info("[activateDevice] IPC device, creating single channel");
                createSingleIpcChannel(device, discoveredDevice, username, password);
                break;
                
            case PTZ_MULTI_CHANNEL:
                log.info("[activateDevice] PTZ device, will query channels after online");
                activationStateManager.markNeedsSyncChannel(device.getId());
                break;
                
            case NO_CHANNEL:
                log.info("[activateDevice] No channel device, skipping channel creation");
                break;
        }
        
        // 5. Mark as activated in discovery list
        if (discoveredDeviceService != null) {
            try {
                discoveredDeviceService.markAsActivated(discoveredDevice.getIpAddress(), device.getId());
                log.info("[activateDevice] Removed from discovery list: ip={}, deviceId={}", 
                         discoveredDevice.getIpAddress(), device.getId());
            } catch (Exception e) {
                log.error("[activateDevice] Failed to mark as activated: ip={}, deviceId={}", 
                          discoveredDevice.getIpAddress(), device.getId(), e);
            }
        }
        
        // 6. Record activation status（使用统一的激活状态管理器）
        activationStateManager.startActivation(activationId, device.getId());
        
        log.info("[activateDevice] Device record saved: activationId={}, deviceId={}", 
                activationId, device.getId());
        
        // 7. Publish event for after transaction commit
        DeviceActivationEvent event = new DeviceActivationEvent(this,
                activationId,
                device.getId(),
                device.getProductId(),
                device.getProductKey(),
                device.getDeviceName(),
                DeviceConfigHelper.getIpAddress(device),
                discoveredDevice.getVendor(),
                discoveredDevice.getDeviceType() != null ? discoveredDevice.getDeviceType() : discoveredDevice.getModel(),
                username,
                password,
                37777,
                "ACTIVE",
                device.getTenantId());
        eventPublisher.publishEvent(event);
        
        return activationId;
    }

    /**
     * Handle after transaction commit: send device connect message
     */
    @org.springframework.transaction.event.TransactionalEventListener(phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT)
    public void handleDeviceActivationAfterCommit(DeviceActivationEvent event) {
        log.info("[handleDeviceActivationAfterCommit] Transaction committed, sending device connect request: deviceId={}",
                event.getDeviceId());

        // Build connect request message
        IotDeviceMessage message = new IotDeviceMessage();
        message.setRequestId(event.getRequestId());
        message.setDeviceId(event.getDeviceId());
        message.setTenantId(event.getTenantId());
        message.setProductId(event.getProductId());
        message.setProductKey(event.getProductKey());
        message.setDeviceName(event.getDeviceName());

        Map<String, Object> params = new HashMap<>();
        params.put("ip", event.getIp());
        params.put("vendor", event.getVendor());
        params.put("deviceType", event.getDeviceType());
        params.put("username", event.getUsername());
        params.put("password", event.getPassword());
        params.put("port", event.getPort());
        params.put("connectionMode", event.getConnectionMode());
        params.put("reconnect", false);
        message.setParams(params);

        // Publish device connect request to Gateway (使用统一通道)
        message.setMethod("CONNECT");
        messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, message);

        log.info("[handleDeviceActivationAfterCommit] Device connect request sent: deviceId={}, requestId={}",
                event.getDeviceId(), event.getRequestId());
    }

    /**
     * Device Activation Event
     */
    @lombok.Getter
    public static class DeviceActivationEvent extends org.springframework.context.ApplicationEvent {

        private final String requestId;
        private final Long deviceId;
        private final Long productId;
        private final String productKey;
        private final String deviceName;
        private final String ip;
        private final String vendor;
        private final String deviceType;
        private final String username;
        private final String password;
        private final Integer port;
        private final String connectionMode;
        private final Long tenantId;

        public DeviceActivationEvent(Object source,
                                    String requestId,
                                    Long deviceId,
                                    Long productId,
                                    String productKey,
                                    String deviceName,
                                    String ip,
                                    String vendor,
                                    String deviceType,
                                    String username,
                                    String password,
                                    Integer port,
                                    String connectionMode,
                                    Long tenantId) {
            super(source);
            this.requestId = requestId;
            this.deviceId = deviceId;
            this.productId = productId;
            this.productKey = productKey;
            this.deviceName = deviceName;
            this.ip = ip;
            this.vendor = vendor;
            this.deviceType = deviceType;
            this.username = username;
            this.password = password;
            this.port = port;
            this.connectionMode = connectionMode;
            this.tenantId = tenantId;
        }
    }

    @Override
    public String getActivationStatus(String activationId) {
        // 委托给统一的激活状态管理器
        return activationStateManager.getActivationStatus(activationId);
    }
    
    @Override
    public Long getActivationResult(String activationId) {
        // 委托给统一的激活状态管理器
        return activationStateManager.getActivationResult(activationId);
    }
    
    /**
     * Get activation status detail (including error message)
     *
     * @param activationId activation ID
     * @return activation status and result
     */
    public Map<String, Object> getActivationStatusDetail(String activationId) {
        // 委托给统一的激活状态管理器
        return activationStateManager.getActivationStatusDetail(activationId);
    }
    
    @Override
    public void disconnectDevice(Long deviceId) {
        log.info("[disconnectDevice] Disconnecting device: deviceId={}", deviceId);
        
        // Build disconnect request message
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(deviceId);
        
        // Publish device disconnect request to Gateway (使用统一通道)
        message.setMethod("DISCONNECT");
        messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, message);
    }

    /**
     * Find or create device record
     */
    private IotDeviceDO findOrCreateDevice(IotProductDO product, DiscoveredDeviceDTO discoveredDevice,
                                            String username, String password) {
        // 1. Try to find existing device by IP address
        IotDeviceDO existingDevice = findDeviceByIpInConfig(discoveredDevice.getIpAddress());
        
        if (existingDevice != null) {
            log.info("[findOrCreateDevice] Found existing device: deviceId={}, ip={}", 
                    existingDevice.getId(), discoveredDevice.getIpAddress());
            
            // Update device config
            updateDeviceConfig(existingDevice, discoveredDevice, username, password);
            deviceMapper.updateById(existingDevice);
            
            return existingDevice;
        }
        
        // 2. Create new device
        IotDeviceDO newDevice = new IotDeviceDO();
        
        // Basic info
        newDevice.setDeviceName(generateDeviceName(discoveredDevice));
        newDevice.setNickname(discoveredDevice.getVendor() + " " + discoveredDevice.getModel());
        newDevice.setSerialNumber(discoveredDevice.getSerialNumber());
        
        // Product info
        newDevice.setProductId(product.getId());
        newDevice.setProductKey(product.getProductKey());
        newDevice.setDeviceType(product.getDeviceType());
        
        // Status info
        newDevice.setState(IotDeviceStateEnum.INACTIVE.getState());
        newDevice.setAddress(discoveredDevice.getIpAddress());
        
        // Device unique identifier
        String deviceKey = generateDeviceKey(product.getProductKey(), discoveredDevice.getSerialNumber());
        newDevice.setDeviceKey(deviceKey);
        
        // Set Boolean field defaults
        newDevice.setSubsystemOverride(false);
        newDevice.setMenuOverride(false);
        
        // Build device config
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("ipAddress", discoveredDevice.getIpAddress());
        configMap.put("username", username);
        configMap.put("password", password);
        configMap.put("vendor", discoveredDevice.getVendor());
        configMap.put("model", discoveredDevice.getModel());
        configMap.put("firmwareVersion", discoveredDevice.getFirmwareVersion());
        configMap.put("httpPort", discoveredDevice.getHttpPort());
        configMap.put("rtspPort", discoveredDevice.getRtspPort());
        configMap.put("onvifPort", discoveredDevice.getOnvifPort());
        configMap.put("onvifSupported", discoveredDevice.getOnvifSupported());
        AccessDeviceConfig config = new AccessDeviceConfig();
        config.fromMap(configMap);
        newDevice.setConfig(config);
        
        // Insert to database
        deviceMapper.insert(newDevice);
        
        log.info("[findOrCreateDevice] Created new device: deviceId={}, deviceName={}, ip={}", 
                newDevice.getId(), newDevice.getDeviceName(), DeviceConfigHelper.getIpAddress(newDevice));
        
        return newDevice;
    }

    /**
     * Update device config
     */
    private void updateDeviceConfig(IotDeviceDO device, DiscoveredDeviceDTO discoveredDevice,
                                     String username, String password) {
        device.setAddress(discoveredDevice.getIpAddress());
        
        // Parse existing config
        Map<String, Object> configMap = device.getConfig() != null 
            ? device.getConfig().toMap()
            : new HashMap<>();
        
        // Update connection info
        configMap.put("ipAddress", discoveredDevice.getIpAddress());
        configMap.put("username", username);
        configMap.put("password", password);
        configMap.put("vendor", discoveredDevice.getVendor());
        configMap.put("model", discoveredDevice.getModel());
        configMap.put("firmwareVersion", discoveredDevice.getFirmwareVersion());
        configMap.put("httpPort", discoveredDevice.getHttpPort());
        configMap.put("rtspPort", discoveredDevice.getRtspPort());
        configMap.put("onvifPort", discoveredDevice.getOnvifPort());
        configMap.put("onvifSupported", discoveredDevice.getOnvifSupported());
        
        AccessDeviceConfig config = new AccessDeviceConfig();
        config.fromMap(configMap);
        device.setConfig(config);
    }
    
    /**
     * Find device by IP in config
     * 
     * @param ip IP address
     * @return device object, or null if not found
     */
    private IotDeviceDO findDeviceByIpInConfig(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        
        // Query all devices and match IP in config
        List<IotDeviceDO> devices = deviceMapper.selectList();
        for (IotDeviceDO device : devices) {
            String deviceIp = DeviceConfigHelper.getIpAddress(device);
            if (ip.equals(deviceIp)) {
                return device;
            }
            
            // Compatibility: check config.ip field
            if (device.getConfig() != null) {
                try {
                    Map<String, Object> configMap = device.getConfig().toMap();
                    Object configIp = configMap.get("ip");
                    if (configIp != null && ip.equals(configIp.toString())) {
                        return device;
                    }
                } catch (Exception ignored) {}
            }
        }
        
        return null;
    }
    
    /**
     * Generate device name
     */
    private String generateDeviceName(DiscoveredDeviceDTO discoveredDevice) {
        // Use serial number as device name, or IP address if no serial number
        if (discoveredDevice.getSerialNumber() != null && !discoveredDevice.getSerialNumber().isEmpty()) {
            return discoveredDevice.getSerialNumber();
        }
        return "device_" + discoveredDevice.getIpAddress().replace(".", "_");
    }
    
    /**
     * Generate device unique identifier (DeviceKey)
     * 
     * @param productKey product key
     * @param serialNumber serial number (can be null)
     * @return device unique identifier
     */
    private String generateDeviceKey(String productKey, String serialNumber) {
        if (serialNumber != null && !serialNumber.isEmpty()) {
            return productKey + "_" + serialNumber;
        } else {
            return productKey + "_" + IdUtil.fastSimpleUUID();
        }
    }

    // ==================== 设备状态处理已迁移到 DeviceStateChangeConsumer ====================
    // 原来的 handleDeviceOnline 方法逻辑已统一到 DeviceStateChangeConsumer.handleDeviceOnlineUnified
    // 激活状态管理已统一到 DeviceActivationStateManager

    /**
     * Check if device is an access control device (ACCESS_GEN1 or ACCESS_GEN2)
     * 
     * <p>Requirements 9.6: Determine if device type is access control device
     * 
     * @param deviceType device type string (ACCESS_GEN1 or ACCESS_GEN2)
     * @return true if device is ACCESS_GEN1 or ACCESS_GEN2
     */
    public boolean isAccessDevice(String deviceType) {
        if (deviceType == null) {
            return false;
        }
        return AccessDeviceTypeConstants.ACCESS_GEN1.equals(deviceType) 
                || AccessDeviceTypeConstants.ACCESS_GEN2.equals(deviceType);
    }

    /**
     * Get access device type from device config
     * 
     * <p>Requirements 9.6: Determine device type based on supportVideo flag in config
     * <ul>
     *   <li>supportVideo = true → ACCESS_GEN2 (二代门禁，人脸一体机)</li>
     *   <li>supportVideo = false/null → ACCESS_GEN1 (一代门禁控制器)</li>
     * </ul>
     * 
     * <p>Note: Non-access devices are filtered out by config type checks. Only devices
     * with AccessDeviceConfig or GenericDeviceConfig containing access-related fields
     * (supportVideo, ipAddress) will be identified as access devices.
     * 
     * @param device device object
     * @return ACCESS_GEN1 or ACCESS_GEN2 if device is access device, null otherwise
     */
    public String getAccessDeviceType(IotDeviceDO device) {
        if (device == null || device.getConfig() == null) {
            return null;
        }

        // Device category filtering is handled by config type checks below.
        // The device.getDeviceType() field is an Integer representing connection type
        // (0=DIRECT, 1=GATEWAY_SUB, 2=GATEWAY), not the device category.
        // Non-access devices (ALARM, NVR, etc.) will be filtered out because they
        // don't have AccessDeviceConfig or the supportVideo flag.

        Boolean supportVideo = null;
        
        // Try to get supportVideo from AccessDeviceConfig
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            supportVideo = config.getSupportVideo();
            
            // If supportVideo is set, this is an access device
            if (supportVideo != null) {
                return AccessDeviceTypeConstants.getDeviceType(supportVideo);
            }
            
            // Check if config has ipAddress (access devices typically have this)
            if (config.getIpAddress() != null && !config.getIpAddress().isEmpty()) {
                // Default to ACCESS_GEN1 if no supportVideo flag
                return AccessDeviceTypeConstants.ACCESS_GEN1;
            }
        }
        
        // Try to get supportVideo from GenericDeviceConfig
        if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig config = (GenericDeviceConfig) device.getConfig();
            Object deviceTypeObj = config.get("deviceType");
            Object supportVideoObj = config.get("supportVideo");
            String configDeviceType = deviceTypeObj != null ? deviceTypeObj.toString() : null;
            if (supportVideoObj instanceof Boolean) {
                supportVideo = (Boolean) supportVideoObj;
                return AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo);
            }
            // 如果仅配置了 deviceType（ACCESS_GEN1/ACCESS_GEN2），也可以直接判定
            if (configDeviceType != null && !configDeviceType.trim().isEmpty()) {
                return AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, null);
            }
            
            // Check if this looks like an access device config
            Object ipAddress = config.get("ipAddress");
            if (ipAddress != null) {
                // Default to ACCESS_GEN1 if no supportVideo flag
                return AccessDeviceTypeConstants.ACCESS_GEN1;
            }
        }
        
        // Try to get from config map
        try {
            Map<String, Object> configMap = device.getConfig().toMap();
            Object deviceTypeObj = configMap.get("deviceType");
            Object supportVideoObj = configMap.get("supportVideo");
            String configDeviceType = deviceTypeObj != null ? deviceTypeObj.toString() : null;
            if (supportVideoObj instanceof Boolean) {
                supportVideo = (Boolean) supportVideoObj;
                return AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo);
            }
            if (configDeviceType != null && !configDeviceType.trim().isEmpty()) {
                return AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, null);
            }
            
            // Check if this looks like an access device config
            if (configMap.containsKey("ipAddress") || configMap.containsKey("ip")) {
                // Check product type to determine if this is an access device
                // For now, return null if we can't determine
                return null;
            }
        } catch (Exception e) {
            log.debug("[getAccessDeviceType] Failed to parse config: deviceId={}", device.getId(), e);
        }
        
        return null;
    }

    // 原来的 triggerAccessChannelSync 方法已迁移到 DeviceStateChangeConsumer.triggerChannelSync

    // 原来的 handleDeviceOffline 方法逻辑已统一到 DeviceStateChangeConsumer.handleDeviceOfflineUnified
    
    /**
     * Determine device channel strategy
     */
    private DeviceChannelStrategy determineChannelStrategy(IotDeviceDO device, DiscoveredDeviceDTO discoveredDevice) {
        String deviceType = discoveredDevice.getDeviceType();
        Long productId = device.getProductId();
        
        // 1. NVR/DVR: need to sync channels
        if ("NVR".equalsIgnoreCase(deviceType) || "DVR".equalsIgnoreCase(deviceType) 
                || (productId != null && productId.equals(4L))) {
            return DeviceChannelStrategy.NVR_DEVICE;
        }
        
        // 2. PTZ: need to query channel count
        if (isPtzDevice(deviceType, discoveredDevice)) {
            return DeviceChannelStrategy.PTZ_MULTI_CHANNEL;
        }
        
        // 3. Normal IPC: single channel
        if ("IPC".equalsIgnoreCase(deviceType) || "CAMERA".equalsIgnoreCase(deviceType) 
                || (productId != null && productId.equals(3L))) {
            return DeviceChannelStrategy.IPC_SINGLE_CHANNEL;
        }
        
        // 4. Other devices with channels
        if (isDeviceWithChannels(deviceType)) {
            return DeviceChannelStrategy.NVR_DEVICE;
        }
        
        // 5. No channel device
        return DeviceChannelStrategy.NO_CHANNEL;
    }

    /**
     * Check if device is PTZ device
     */
    private boolean isPtzDevice(String deviceType, DiscoveredDeviceDTO discoveredDevice) {
        // Method 1: device type contains PTZ keywords
        if (deviceType != null) {
            String type = deviceType.toUpperCase();
            if (type.contains("PTZ") || type.contains("DOME") || type.contains("SPEED_DOME")) {
                return true;
            }
        }
        
        // Method 2: device name contains PTZ keywords
        String deviceName = discoveredDevice.getDeviceName();
        if (deviceName != null) {
            String name = deviceName.toUpperCase();
            if (name.contains("PTZ") || name.contains("DOME")) {
                return true;
            }
        }
        
        // Method 3: check device model
        String model = discoveredDevice.getModel();
        if (model != null) {
            String m = model.toUpperCase();
            if (m.contains("PTZ") || m.contains("DOME")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if device needs channels
     */
    private boolean isDeviceWithChannels(String deviceType) {
        if (deviceType == null) {
            return false;
        }
        
        return deviceType.equalsIgnoreCase("NVR") 
            || deviceType.equalsIgnoreCase("DVR")
            || deviceType.equalsIgnoreCase("ACCESS_CONTROLLER")
            || deviceType.equalsIgnoreCase("FIRE_PANEL")
            || deviceType.equalsIgnoreCase("METER")
            || deviceType.equalsIgnoreCase("BROADCAST");
    }

    /**
     * Create single channel for IPC
     */
    private void createSingleIpcChannel(IotDeviceDO device, DiscoveredDeviceDTO discoveredDevice, 
                                         String username, String password) {
        try {
            IotDeviceChannelDO channel = new IotDeviceChannelDO();
            
            // Basic info
            channel.setDeviceId(device.getId());
            channel.setDeviceType("IPC");
            channel.setProductId(device.getProductId());
            channel.setChannelNo(1);
            channel.setChannelName(device.getDeviceName() + "-Main");
            channel.setChannelType("VIDEO");
            channel.setChannelSubType("IPC");
            
            // Connection info
            String deviceIp = DeviceConfigHelper.getIpAddress(device);
            channel.setTargetIp(deviceIp);
            channel.setTargetChannelNo(1);
            channel.setProtocol("RTSP");
            channel.setUsername(username);
            channel.setPassword(password);
            
            // Video stream URLs (generate based on vendor)
            String vendor = discoveredDevice.getVendor();
            Integer rtspPort = discoveredDevice.getRtspPort() != null ? discoveredDevice.getRtspPort() : 554;
            Integer httpPort = discoveredDevice.getHttpPort() != null ? discoveredDevice.getHttpPort() : 80;
            
            if ("dahua".equalsIgnoreCase(vendor)) {
                // Dahua IPC stream URL format
                channel.setStreamUrlMain(String.format("rtsp://%s:%s@%s:%d/cam/realmonitor?channel=1&subtype=0", 
                        username, password, deviceIp, rtspPort));
                channel.setStreamUrlSub(String.format("rtsp://%s:%s@%s:%d/cam/realmonitor?channel=1&subtype=1", 
                        username, password, deviceIp, rtspPort));
                channel.setSnapshotUrl(String.format("http://%s:%s@%s:%d/cgi-bin/snapshot.cgi?channel=1", 
                        username, password, deviceIp, httpPort));
            } else if ("hikvision".equalsIgnoreCase(vendor)) {
                // Hikvision IPC stream URL format
                channel.setStreamUrlMain(String.format("rtsp://%s:%s@%s:%d/Streaming/Channels/101", 
                        username, password, deviceIp, rtspPort));
                channel.setStreamUrlSub(String.format("rtsp://%s:%s@%s:%d/Streaming/Channels/102", 
                        username, password, deviceIp, rtspPort));
                channel.setSnapshotUrl(String.format("http://%s:%s@%s:%d/ISAPI/Streaming/channels/101/picture", 
                        username, password, deviceIp, httpPort));
            } else {
                // Generic ONVIF format
                channel.setStreamUrlMain(String.format("rtsp://%s:%s@%s:%d/stream1", 
                        username, password, deviceIp, rtspPort));
                channel.setStreamUrlSub(String.format("rtsp://%s:%s@%s:%d/stream2", 
                        username, password, deviceIp, rtspPort));
            }
            
            // Capability info (IPC usually does not support PTZ)
            channel.setPtzSupport(false);
            channel.setAudioSupport(false);
            
            // Status info
            channel.setOnlineStatus(device.getState());
            channel.setEnableStatus(1);
            channel.setLastSyncTime(LocalDateTime.now());
            
            // Insert to database
            channelMapper.insert(channel);
            
            log.info("[createSingleIpcChannel] IPC single channel created: deviceId={}, channelId={}, channelName={}", 
                    device.getId(), channel.getId(), channel.getChannelName());
                    
        } catch (Exception e) {
            log.error("[createSingleIpcChannel] Failed to create IPC single channel: deviceId={}", device.getId(), e);
            // Don't throw exception to avoid affecting device activation
        }
    }
}
