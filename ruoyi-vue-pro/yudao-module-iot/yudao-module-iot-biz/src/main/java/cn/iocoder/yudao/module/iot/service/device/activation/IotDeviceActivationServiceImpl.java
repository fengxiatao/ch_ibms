package cn.iocoder.yudao.module.iot.service.device.activation;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductRespVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.service.device.discovery.DiscoveredDeviceService;
import cn.iocoder.yudao.module.iot.service.device.discovery.dto.DiscoveredDeviceDTO;
import cn.iocoder.yudao.module.iot.service.ibms.channel.IbmsChannelService;
import cn.iocoder.yudao.module.iot.service.ibms.facade.IbmsBusinessMappingHelper;
import cn.iocoder.yudao.module.iot.service.ibms.product.IbmsProductService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 设备激活：仅维护 IBMS 台账（ibms_device / ibms_channel），通过消息总线下发 CONNECT。
 * <p>
 * 网关侧 {@code deviceId} 与 {@link IbmsDeviceDO#getId()} 一致；接入参数写入 {@code extra} JSON。
 * </p>
 */
@Service
@Slf4j
public class IotDeviceActivationServiceImpl implements IotDeviceActivationService {

    private static final int DEVICE_CODE_RETRY = 12;

    @Resource
    private DeviceActivationStateManager activationStateManager;

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;

    @Resource
    private IbmsProductService ibmsProductService;

    @Resource
    private IotMessageBus messageBus;

    @Resource
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private DiscoveredDeviceService discoveredDeviceService;

    @Resource
    private IbmsChannelService ibmsChannelService;

    @Resource
    private IbmsBusinessMappingHelper businessMappingHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String activateDevice(DiscoveredDeviceDTO discoveredDevice, Long productId,
                                 String username, String password) {
        String activationId = UUID.randomUUID().toString();

        log.info("[activateDevice] IBMS 激活开始: activationId={}, ip={}, ibmsProductId={}",
                activationId, discoveredDevice.getIpAddress(), productId);

        IbmsProductRespVO product = ibmsProductService.getProduct(productId);
        if (product == null) {
            throw ServiceExceptionUtil.exception0(404, "IBMS 产品不存在");
        }

        String vendor = discoveredDevice.getVendor();
        String productName = product.getProductName();
        if (productName != null && (productName.contains("NVR") || productName.contains("DVR"))) {
            if ("Dahua".equalsIgnoreCase(vendor)) {
                vendor = "dahua_sdk";
                log.info("[activateDevice] 大华 NVR/DVR: ip={}, productName={}", discoveredDevice.getIpAddress(), productName);
            } else if ("Hikvision".equalsIgnoreCase(vendor)) {
                vendor = "hikvision_sdk";
                log.info("[activateDevice] 海康 NVR/DVR: ip={}, productName={}", discoveredDevice.getIpAddress(), productName);
            }
        }

        IbmsDeviceDO device = findOrCreateIbmsDevice(product, discoveredDevice, username, password);

        DeviceChannelStrategy strategy = determineChannelStrategy(product, discoveredDevice);
        log.info("[activateDevice] 通道策略: deviceId={}, strategy={}", device.getId(), strategy);

        switch (strategy) {
            case NVR_DEVICE, PTZ_MULTI_CHANNEL -> activationStateManager.markNeedsSyncChannel(device.getId());
            case IPC_SINGLE_CHANNEL -> createSingleIbmsIpcChannel(device, discoveredDevice, username, password);
            case NO_CHANNEL -> log.info("[activateDevice] 无需预建通道");
        }

        if (discoveredDeviceService != null) {
            try {
                discoveredDeviceService.markAsActivated(discoveredDevice.getIpAddress(), device.getId());
                log.info("[activateDevice] 已标记发现列表: ip={}, ibmsDeviceId={}",
                        discoveredDevice.getIpAddress(), device.getId());
            } catch (Exception e) {
                log.error("[activateDevice] 标记发现失败: ip={}, deviceId={}",
                        discoveredDevice.getIpAddress(), device.getId(), e);
            }
        }

        activationStateManager.startActivation(activationId, device.getId());

        IbmsDeviceDO latest = ibmsDeviceMapper.selectById(device.getId());
        if (latest != null && isIbmsDeviceOnlineInExtra(latest)) {
            log.info("[activateDevice] 设备已在 extra 中标记在线，直接完成激活: activationId={}, deviceId={}",
                    activationId, device.getId());
            activationStateManager.completeActivation(device.getId());
        }

        Long tenantId = resolveTenantId(discoveredDevice);
        DeviceActivationEvent event = new DeviceActivationEvent(this,
                activationId,
                device.getId(),
                product.getId(),
                device.getProductKey(),
                device.getName(),
                discoveredDevice.getIpAddress(),
                vendor,
                discoveredDevice.getDeviceType() != null ? discoveredDevice.getDeviceType() : product.getDeviceTypeCode(),
                username,
                password,
                37777,
                "ACTIVE",
                tenantId);
        eventPublisher.publishEvent(event);

        return activationId;
    }

    private static Long resolveTenantId(DiscoveredDeviceDTO discoveredDevice) {
        if (discoveredDevice.getTenantId() != null) {
            return discoveredDevice.getTenantId();
        }
        Long t = TenantContextHolder.getTenantId();
        return t != null ? t : 0L;
    }

    @org.springframework.transaction.event.TransactionalEventListener(phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT)
    public void handleDeviceActivationAfterCommit(DeviceActivationEvent event) {
        log.info("[handleDeviceActivationAfterCommit] CONNECT: ibmsDeviceId={}", event.getDeviceId());

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
        message.setMethod("CONNECT");
        messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, message);

        log.info("[handleDeviceActivationAfterCommit] CONNECT 已发送: deviceId={}", event.getDeviceId());
    }

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
        return activationStateManager.getActivationStatus(activationId);
    }

    @Override
    public Long getActivationResult(String activationId) {
        return activationStateManager.getActivationResult(activationId);
    }

    @Override
    public Map<String, Object> getActivationStatusDetail(String activationId) {
        return activationStateManager.getActivationStatusDetail(activationId);
    }

    @Override
    public void disconnectDevice(Long deviceId) {
        log.info("[disconnectDevice] DISCONNECT: ibmsDeviceId={}", deviceId);
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(deviceId);
        message.setMethod("DISCONNECT");
        messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, message);
    }

    // -------------------------------------------------------------------------

    private IbmsDeviceDO findOrCreateIbmsDevice(IbmsProductRespVO product, DiscoveredDeviceDTO d,
                                                  String username, String password) {
        Long tenantId = resolveTenantId(d);
        String ip = d.getIpAddress();
        IbmsDeviceDO existing = ibmsDeviceMapper.selectByTenantIdAndIp(tenantId, ip);
        if (existing != null) {
            applyActivationFieldsToDevice(existing, product, d, username, password);
            ibmsDeviceMapper.updateById(existing);
            log.info("[findOrCreateIbmsDevice] 更新已有 IBMS 设备: id={}, ip={}", existing.getId(), ip);
            return existing;
        }

        IbmsDeviceDO created = new IbmsDeviceDO();
        created.setTenantId(tenantId);
        created.setName(generateDeviceName(d));
        created.setGroupCode(product.getGroupCode());
        created.setSystemCode(product.getSystemCode());
        created.setDeviceTypeCode(product.getDeviceTypeCode());
        created.setBrand(StrUtil.blankToDefault(product.getManufacturer(), "HIK"));
        created.setProductModel(StrUtil.blankToDefault(product.getModelNumber(), product.getModelCode()));
        created.setAccessType("IP");
        created.setProtocol(StrUtil.blankToDefault(product.getProtocol(), "ONVIF"));
        created.setIp(ip);
        created.setSpace(null);
        created.setSn("SN-" + RandomUtil.randomString(10).toUpperCase());
        created.setProductKey("PK-" + UUID.randomUUID());
        created.setPointCount(0);
        created.setPointsOnline(0);
        created.setPointsAlarm(0);

        String deviceCode = allocateUniqueDeviceCode(product);
        created.setDeviceCode(deviceCode);
        created.setExtra(buildActivationExtraJson(new JSONObject(), d, username, password).toString());

        ibmsDeviceMapper.insert(created);
        log.info("[findOrCreateIbmsDevice] 新建 IBMS 设备: id={}, deviceCode={}, ip={}",
                created.getId(), deviceCode, ip);
        return created;
    }

    private void applyActivationFieldsToDevice(IbmsDeviceDO device, IbmsProductRespVO product,
                                               DiscoveredDeviceDTO d, String username, String password) {
        device.setIp(d.getIpAddress());
        device.setGroupCode(product.getGroupCode());
        device.setSystemCode(product.getSystemCode());
        device.setDeviceTypeCode(product.getDeviceTypeCode());
        if (StrUtil.isNotBlank(product.getManufacturer())) {
            device.setBrand(product.getManufacturer());
        }
        if (StrUtil.isNotBlank(product.getModelNumber())) {
            device.setProductModel(product.getModelNumber());
        }
        if (StrUtil.isNotBlank(product.getProtocol())) {
            device.setProtocol(product.getProtocol());
        }
        JSONObject base = new JSONObject();
        if (StrUtil.isNotBlank(device.getExtra())) {
            try {
                base = JSONUtil.parseObj(device.getExtra().trim());
            } catch (Exception ignored) {
                // keep empty base
            }
        }
        device.setExtra(buildActivationExtraJson(base, d, username, password).toString());
    }

    private JSONObject buildActivationExtraJson(JSONObject base, DiscoveredDeviceDTO d,
                                                String username, String password) {
        base.set("username", username);
        base.set("password", password);
        base.set("tcpPort", 37777);
        if (d.getHttpPort() != null) {
            base.set("httpPort", d.getHttpPort());
        }
        if (d.getRtspPort() != null) {
            base.set("rtspPort", d.getRtspPort());
        }
        if (d.getOnvifPort() != null) {
            base.set("onvifPort", d.getOnvifPort());
        }
        if (StrUtil.isNotBlank(d.getVendor())) {
            base.set("vendor", d.getVendor());
        }
        if (StrUtil.isNotBlank(d.getModel())) {
            base.set("model", d.getModel());
        }
        if (StrUtil.isNotBlank(d.getFirmwareVersion())) {
            base.set("firmwareVersion", d.getFirmwareVersion());
        }
        if (StrUtil.isNotBlank(d.getIpAddress())) {
            base.set("ip", d.getIpAddress().trim());
        }
        String discoveredDeviceType = d.getDeviceType();
        if (AccessDeviceTypeConstants.ACCESS_GEN1.equalsIgnoreCase(discoveredDeviceType)) {
            base.set("deviceType", AccessDeviceTypeConstants.ACCESS_GEN1);
        } else if (AccessDeviceTypeConstants.ACCESS_GEN2.equalsIgnoreCase(discoveredDeviceType)) {
            base.set("deviceType", AccessDeviceTypeConstants.ACCESS_GEN2);
            base.set("supportVideo", true);
        }
        return base;
    }

    private String allocateUniqueDeviceCode(IbmsProductRespVO product) {
        String system = StrUtil.blankToDefault(product.getSystemCode(), "VI").trim();
        String model = StrUtil.blankToDefault(product.getModelCode(), "UNK").trim();
        String devType = StrUtil.blankToDefault(product.getDeviceTypeCode(), "CAM").trim();
        String brand = StrUtil.blankToDefault(product.getManufacturer(), "HIK").trim();
        String prefix = system + "-" + model + "-" + devType + "-" + brand + "-";
        for (int i = 0; i < DEVICE_CODE_RETRY; i++) {
            int seq = RandomUtil.randomInt(1, 999);
            String code = prefix + String.format("%03d", seq);
            if (ibmsDeviceMapper.selectByDeviceCode(code) == null) {
                return code;
            }
        }
        return prefix + RandomUtil.randomString(6).toUpperCase();
    }

    private String generateDeviceName(DiscoveredDeviceDTO d) {
        if (StrUtil.isNotBlank(d.getSerialNumber())) {
            return d.getSerialNumber();
        }
        return "device_" + d.getIpAddress().replace(".", "_");
    }

    private DeviceChannelStrategy determineChannelStrategy(IbmsProductRespVO product, DiscoveredDeviceDTO d) {
        String dt = d.getDeviceType();
        String pn = product.getProductName();
        String dtc = product.getDeviceTypeCode();

        if ("NVR".equalsIgnoreCase(dt) || "DVR".equalsIgnoreCase(dt)
                || (pn != null && (pn.toUpperCase().contains("NVR") || pn.toUpperCase().contains("DVR")))
                || "NVR".equalsIgnoreCase(dtc)) {
            return DeviceChannelStrategy.NVR_DEVICE;
        }
        if (isPtzDevice(dt, d)) {
            return DeviceChannelStrategy.PTZ_MULTI_CHANNEL;
        }
        if ("IPC".equalsIgnoreCase(dt) || "CAMERA".equalsIgnoreCase(dt) || "CAM".equalsIgnoreCase(dtc)) {
            return DeviceChannelStrategy.IPC_SINGLE_CHANNEL;
        }
        if (isDeviceWithChannels(dt)) {
            return DeviceChannelStrategy.NVR_DEVICE;
        }
        return DeviceChannelStrategy.NO_CHANNEL;
    }

    private boolean isPtzDevice(String deviceType, DiscoveredDeviceDTO d) {
        if (deviceType != null) {
            String t = deviceType.toUpperCase();
            if (t.contains("PTZ") || t.contains("DOME") || t.contains("SPEED_DOME")) {
                return true;
            }
        }
        if (d.getDeviceName() != null) {
            String n = d.getDeviceName().toUpperCase();
            if (n.contains("PTZ") || n.contains("DOME")) {
                return true;
            }
        }
        if (d.getModel() != null) {
            String m = d.getModel().toUpperCase();
            if (m.contains("PTZ") || m.contains("DOME")) {
                return true;
            }
        }
        return false;
    }

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

    private void createSingleIbmsIpcChannel(IbmsDeviceDO device, DiscoveredDeviceDTO d,
                                            String username, String password) {
        try {
            String deviceIp = device.getIp();
            Integer rtspPort = d.getRtspPort() != null ? d.getRtspPort() : 554;
            Integer httpPort = d.getHttpPort() != null ? d.getHttpPort() : 80;
            String vendor = d.getVendor();

            Map<String, Object> chExtra = new HashMap<>();
            chExtra.put("streamProtocol", "RTSP");
            if ("dahua".equalsIgnoreCase(vendor)) {
                chExtra.put("streamUrlMain", String.format("rtsp://%s:%s@%s:%d/cam/realmonitor?channel=1&subtype=0",
                        username, password, deviceIp, rtspPort));
                chExtra.put("streamUrlSub", String.format("rtsp://%s:%s@%s:%d/cam/realmonitor?channel=1&subtype=1",
                        username, password, deviceIp, rtspPort));
                chExtra.put("snapshotUrl", String.format("http://%s:%s@%s:%d/cgi-bin/snapshot.cgi?channel=1",
                        username, password, deviceIp, httpPort));
            } else if ("hikvision".equalsIgnoreCase(vendor)) {
                chExtra.put("streamUrlMain", String.format("rtsp://%s:%s@%s:%d/Streaming/Channels/101",
                        username, password, deviceIp, rtspPort));
                chExtra.put("streamUrlSub", String.format("rtsp://%s:%s@%s:%d/Streaming/Channels/102",
                        username, password, deviceIp, rtspPort));
                chExtra.put("snapshotUrl", String.format("http://%s:%s@%s:%d/ISAPI/Streaming/channels/101/picture",
                        username, password, deviceIp, httpPort));
            } else {
                chExtra.put("streamUrlMain", String.format("rtsp://%s:%s@%s:%d/stream1",
                        username, password, deviceIp, rtspPort));
                chExtra.put("streamUrlSub", String.format("rtsp://%s:%s@%s:%d/stream2",
                        username, password, deviceIp, rtspPort));
            }

            IbmsChannelSaveReqVO ch = new IbmsChannelSaveReqVO();
            ch.setDeviceId(device.getId());
            ch.setChannelNo(1);
            ch.setName(device.getName() + "-Main");
            ch.setCode(String.format("CH-%s-IPC-%03d", device.getId(), 1));
            ch.setBusiness(resolveBusinessBySystemType(device.getSystemCode()));
            ch.setTypeCode("VT");
            ch.setCategory("视频通道");
            ch.setSystemType(StrUtil.blankToDefault(device.getSystemCode(), "VI"));
            ch.setDataSource("IPC");
            ch.setStatus("online");
            ch.setIp(deviceIp);
            ch.setDeviceSn(device.getSn());
            ch.setDeviceName(device.getName());
            ch.setSpace(device.getSpace());
            ch.setCurrentValue("在线");
            ch.setExtra(JSONUtil.toJsonStr(chExtra));

            ibmsChannelService.createChannel(ch);
            log.info("[createSingleIbmsIpcChannel] 已创建 IBMS 通道: deviceId={}, channelNo=1", device.getId());
        } catch (Exception e) {
            log.error("[createSingleIbmsIpcChannel] 失败: deviceId={}", device.getId(), e);
        }
    }

    private String resolveBusinessBySystemType(String systemType) {
        // 单一事实源：复用 IbmsBusinessMappingHelper，返回小写大类码（sa/st/sb/se/sf）
        String group = businessMappingHelper.resolveGroupBySystem(systemType);
        return StrUtil.isNotBlank(group) ? group.toLowerCase() : "sa";
    }

    private boolean isIbmsDeviceOnlineInExtra(IbmsDeviceDO device) {
        if (StrUtil.isBlank(device.getExtra())) {
            return false;
        }
        try {
            JSONObject j = JSONUtil.parseObj(device.getExtra().trim());
            Integer state = j.getInt("gatewayRuntimeState");
            return state != null && state.equals(IotDeviceStateEnum.ONLINE.getState());
        } catch (Exception e) {
            return false;
        }
    }
}
