package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessDeviceCapabilityDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlDeviceCommand;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlDeviceResponse;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessDeviceCapabilityMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.enums.access.CredentialTypeConstants;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.access.dto.CapabilityCheckResult;
import cn.iocoder.yudao.module.iot.service.access.dto.CapabilityCheckResult.CapabilityErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备能力缓存服务实现
 * 
 * <p>通过消息总线与 gateway 模块通信，避免直接依赖 SDK 管理器</p>
 * 
 * @author Kiro
 */
@Slf4j
@Service
public class IotAccessDeviceCapabilityServiceImpl implements IotAccessDeviceCapabilityService {

    @Resource
    private IotAccessDeviceCapabilityMapper capabilityMapper;
    
    @Resource
    private AccessControlMessageBusClient messageBusClient;

    @Resource
    private IotDeviceService iotDeviceService;
    
    @Resource
    @Lazy
    private IotAccessDeviceService deviceService;
    
    /** 缓存过期时间（小时） */
    private static final int CACHE_EXPIRE_HOURS = 24;

    @Override
    public IotAccessDeviceCapabilityDO getCapability(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        
        // 先从缓存获取
        IotAccessDeviceCapabilityDO capability = getCapabilityFromCache(deviceId);
        
        // 如果缓存不存在或已过期，刷新缓存
        if (capability == null || capability.isCacheExpired()) {
            capability = refreshCapability(deviceId);
        }
        
        return capability;
    }

    @Override
    public IotAccessDeviceCapabilityDO getCapabilityFromCache(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return capabilityMapper.selectByDeviceId(deviceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotAccessDeviceCapabilityDO refreshCapability(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        
        log.info("[DeviceCapability] 刷新设备能力缓存: deviceId={}", deviceId);
        
        // 查询现有缓存
        IotAccessDeviceCapabilityDO existing = capabilityMapper.selectByDeviceId(deviceId);
        
        // 创建或更新缓存
        IotAccessDeviceCapabilityDO capability;
        if (existing != null) {
            capability = existing;
        } else {
            capability = createDefaultCapability(deviceId);
        }

        // ===== 通过消息总线从网关刷新真实能力（避免默认“全支持”导致错误下发/撤销） =====
        try {
            IotDeviceDO device = deviceService.getAccessDevice(deviceId);
            if (device != null) {
                String ip = getDeviceIp(device);
                Integer port = getDevicePort(device);
                String username = getDeviceUsername(device);
                String password = getDevicePassword(device);
                String deviceType = getAccessDeviceType(device);

                // 尝试先确保设备在线（按需激活），以便能力更准确
                if (ip != null && port != null && username != null && password != null) {
                    messageBusClient.ensureDeviceOnline(deviceId, ip, port, username, password);
                }

                // 查询能力（网关侧根据 deviceType 路由到对应插件）
                AccessControlDeviceCommand cmd = AccessControlDeviceCommand.builder()
                        .deviceId(deviceId)
                        .ipAddress(ip)
                        .port(port)
                        .commandType(AccessControlDeviceCommand.CommandType.QUERY_DEVICE_CAPABILITY)
                        .params(java.util.Map.of("deviceType", deviceType))
                        .build();
                AccessControlDeviceResponse resp = messageBusClient.sendAndWait(cmd, 10);
                if (resp != null && Boolean.TRUE.equals(resp.getSuccess()) && resp.getDeviceCapability() != null) {
                    applyGatewayCapability(capability, deviceType, resp.getDeviceCapability(), resp.getData());
                    // 同步写回到 iot_device.config，供业务侧按能力做“精准下发/撤销”
                    updateDeviceConfigCapabilitySnapshot(device, deviceType, resp.getDeviceCapability(), resp.getData());
                } else {
                    log.warn("[DeviceCapability] 网关能力查询失败，使用缓存/默认能力: deviceId={}, msg={}",
                            deviceId, resp != null ? resp.getErrorMessage() : "timeout");
                    // 失败时不再强行设为“全支持”，保留现有值（新建时已是保守默认）
                }
            }
        } catch (Exception e) {
            log.warn("[DeviceCapability] 网关能力查询异常，使用缓存/默认能力: deviceId={}, error={}",
                    deviceId, e.getMessage());
        }
        
        // 更新缓存时间
        capability.setLastQueryTime(LocalDateTime.now());
        capability.setCacheExpireTime(LocalDateTime.now().plusHours(CACHE_EXPIRE_HOURS));
        
        // 保存或更新
        if (existing != null) {
            capabilityMapper.updateById(capability);
        } else {
            capabilityMapper.insert(capability);
        }
        
        log.info("[DeviceCapability] ✅ 刷新设备能力缓存成功: deviceId={}", deviceId);
        return capability;
    }
    
    private IotAccessDeviceCapabilityDO createDefaultCapability(Long deviceId) {
        return IotAccessDeviceCapabilityDO.builder()
            .deviceId(deviceId)
            // 默认使用保守能力：避免“缓存缺失时默认全支持”导致对不支持的设备执行人脸/指纹命令
            .deviceGeneration(1)
            .maxUsers(10000)
            .maxCards(10000)
            .maxFaces(0)
            .maxFingerprints(0)
            .currentUsers(0)
            .currentCards(0)
            .currentFaces(0)
            .currentFingerprints(0)
            .supFaceService(0)
            .supFingerprintService(0)
            .supCardService(1)
            .supHolidayPlan(0)
            .maxInsertRateUser(10)
            .maxInsertRateCard(10)
            .maxInsertRateFace(5)
            .maxInsertRateFingerprint(10)
            .channels(4)
            .maxCardsPerUser(5)
            .maxFingerprintsPerUser(10)
            .maxFaceImageSize(200)
            .lastQueryTime(LocalDateTime.now())
            .cacheExpireTime(LocalDateTime.now().plusHours(CACHE_EXPIRE_HOURS))
            .build();
    }

    private void applyGatewayCapability(IotAccessDeviceCapabilityDO capability, String deviceType,
                                        AccessControlDeviceResponse.DeviceCapability dc,
                                        java.util.Map<String, Object> rawData) {
        boolean isGen2 = AccessDeviceTypeConstants.ACCESS_GEN2.equalsIgnoreCase(deviceType);
        capability.setDeviceGeneration(isGen2 ? 2 : 1);

        // supXxxService：网关使用 hasXxx（Boolean）表达
        capability.setSupCardService(Boolean.TRUE.equals(dc.getSupCardService()) ? 1 : 0);
        capability.setSupFaceService(Boolean.TRUE.equals(dc.getSupFaceService()) ? 1 : 0);
        capability.setSupFingerprintService(Boolean.TRUE.equals(dc.getSupFingerprintService()) ? 1 : 0);

        // max counts
        if (dc.getMaxCardCount() != null) capability.setMaxCards(dc.getMaxCardCount());
        if (dc.getMaxFaceCount() != null) capability.setMaxFaces(dc.getMaxFaceCount());
        if (dc.getMaxFingerprintCount() != null) capability.setMaxFingerprints(dc.getMaxFingerprintCount());

        // 通道数：newgateway 放在 rawData.capabilities.maxDoorCount
        if (rawData != null) {
            Object capsObj = rawData.get("capabilities");
            if (capsObj instanceof java.util.Map) {
                Object maxDoorCount = ((java.util.Map<?, ?>) capsObj).get("maxDoorCount");
                if (maxDoorCount instanceof Number) {
                    capability.setChannels(((Number) maxDoorCount).intValue());
                } else if (maxDoorCount != null) {
                    try {
                        capability.setChannels(Integer.parseInt(String.valueOf(maxDoorCount)));
                    } catch (NumberFormatException ignore) {
                        // ignore
                    }
                }
            }
        }

        log.info("[DeviceCapability] 网关能力已应用: deviceId={}, gen={}, card={}, face={}, fp={}",
                capability.getDeviceId(), capability.getDeviceGeneration(),
                capability.getSupCardService(), capability.getSupFaceService(), capability.getSupFingerprintService());
    }

    private void updateDeviceConfigCapabilitySnapshot(IotDeviceDO device,
                                                      String deviceType,
                                                      AccessControlDeviceResponse.DeviceCapability dc,
                                                      java.util.Map<String, Object> rawData) {
        if (device == null || device.getId() == null || device.getConfig() == null) {
            return;
        }

        java.util.Map<String, Object> snapshot = new java.util.HashMap<>();
        snapshot.put("deviceType", deviceType);
        snapshot.put("hasCard", Boolean.TRUE.equals(dc.getSupCardService()));
        snapshot.put("hasFace", Boolean.TRUE.equals(dc.getSupFaceService()));
        snapshot.put("hasFingerprint", Boolean.TRUE.equals(dc.getSupFingerprintService()));
        if (dc.getMaxCardCount() != null) snapshot.put("maxCardCount", dc.getMaxCardCount());
        if (dc.getMaxFaceCount() != null) snapshot.put("maxFaceCount", dc.getMaxFaceCount());
        if (dc.getMaxFingerprintCount() != null) snapshot.put("maxFingerprintCount", dc.getMaxFingerprintCount());
        // maxDoorCount 在 rawData.capabilities.maxDoorCount
        if (rawData != null) {
            Object capsObj = rawData.get("capabilities");
            if (capsObj instanceof java.util.Map) {
                Object maxDoorCount = ((java.util.Map<?, ?>) capsObj).get("maxDoorCount");
                if (maxDoorCount instanceof Number) {
                    snapshot.put("maxDoorCount", ((Number) maxDoorCount).intValue());
                } else if (maxDoorCount != null) {
                    snapshot.put("maxDoorCount", String.valueOf(maxDoorCount));
                }
            }
        }
        snapshot.put("updatedAt", java.time.LocalDateTime.now().toString());

        // 写回 config
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig cfg = (AccessDeviceConfig) device.getConfig();
            cfg.setAccessCapabilities(snapshot);
            cfg.setCapabilityTime(java.time.LocalDateTime.now());
            iotDeviceService.updateDeviceConfig(device.getId(), cfg);
        } else if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
            cfg.set("accessCapabilities", snapshot);
            iotDeviceService.updateDeviceConfig(device.getId(), cfg);
        }
    }

    private String getDeviceIp(IotDeviceDO device) {
        return device != null && device.getConfig() != null ? device.getConfig().getIpAddress() : null;
    }

    private Integer getDevicePort(IotDeviceDO device) {
        if (device == null || device.getConfig() == null) {
            return 37777;
        }
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig cfg = (AccessDeviceConfig) device.getConfig();
            return cfg.getPort() != null ? cfg.getPort() : 37777;
        }
        // GenericDeviceConfig 走默认端口
        return 37777;
    }

    private String getDeviceUsername(IotDeviceDO device) {
        if (device == null || device.getConfig() == null) {
            return null;
        }
        if (device.getConfig() instanceof AccessDeviceConfig) {
            return ((AccessDeviceConfig) device.getConfig()).getUsername();
        }
        if (device.getConfig() instanceof GenericDeviceConfig) {
            Object v = ((GenericDeviceConfig) device.getConfig()).get("username");
            return v != null ? String.valueOf(v) : null;
        }
        return null;
    }

    private String getDevicePassword(IotDeviceDO device) {
        if (device == null || device.getConfig() == null) {
            return null;
        }
        if (device.getConfig() instanceof AccessDeviceConfig) {
            return ((AccessDeviceConfig) device.getConfig()).getPassword();
        }
        if (device.getConfig() instanceof GenericDeviceConfig) {
            Object v = ((GenericDeviceConfig) device.getConfig()).get("password");
            return v != null ? String.valueOf(v) : null;
        }
        return null;
    }

    private String getAccessDeviceType(IotDeviceDO device) {
        Boolean supportVideo = null;
        String configDeviceType = null;
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig cfg = (AccessDeviceConfig) device.getConfig();
            supportVideo = cfg.getSupportVideo();
        } else if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
            Object dt = cfg.get("deviceType");
            Object sv = cfg.get("supportVideo");
            configDeviceType = dt != null ? String.valueOf(dt) : null;
            if (sv instanceof Boolean) {
                supportVideo = (Boolean) sv;
            }
        }
        return AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentUsage(Long deviceId, Integer currentUsers, Integer currentCards,
                                   Integer currentFaces, Integer currentFingerprints) {
        if (deviceId == null) {
            return;
        }
        
        IotAccessDeviceCapabilityDO capability = getCapability(deviceId);
        if (capability == null) {
            return;
        }
        
        if (currentUsers != null) {
            capability.setCurrentUsers(currentUsers);
        }
        if (currentCards != null) {
            capability.setCurrentCards(currentCards);
        }
        if (currentFaces != null) {
            capability.setCurrentFaces(currentFaces);
        }
        if (currentFingerprints != null) {
            capability.setCurrentFingerprints(currentFingerprints);
        }
        
        capabilityMapper.updateById(capability);
        log.debug("[DeviceCapability] 更新设备使用量: deviceId={}, users={}, cards={}, faces={}, fingerprints={}",
            deviceId, currentUsers, currentCards, currentFaces, currentFingerprints);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementUsage(Long deviceId, int usersDelta, int cardsDelta,
                              int facesDelta, int fingerprintsDelta) {
        if (deviceId == null) {
            return;
        }
        
        IotAccessDeviceCapabilityDO capability = getCapability(deviceId);
        if (capability == null) {
            return;
        }
        
        if (usersDelta != 0 && capability.getCurrentUsers() != null) {
            capability.setCurrentUsers(capability.getCurrentUsers() + usersDelta);
        }
        if (cardsDelta != 0 && capability.getCurrentCards() != null) {
            capability.setCurrentCards(capability.getCurrentCards() + cardsDelta);
        }
        if (facesDelta != 0 && capability.getCurrentFaces() != null) {
            capability.setCurrentFaces(capability.getCurrentFaces() + facesDelta);
        }
        if (fingerprintsDelta != 0 && capability.getCurrentFingerprints() != null) {
            capability.setCurrentFingerprints(capability.getCurrentFingerprints() + fingerprintsDelta);
        }
        
        capabilityMapper.updateById(capability);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementUsage(Long deviceId, int usersDelta, int cardsDelta,
                              int facesDelta, int fingerprintsDelta) {
        incrementUsage(deviceId, -usersDelta, -cardsDelta, -facesDelta, -fingerprintsDelta);
    }

    @Override
    public boolean hasCapacity(Long deviceId, int userCount, int cardCount,
                              int faceCount, int fingerprintCount) {
        IotAccessDeviceCapabilityDO capability = getCapability(deviceId);
        if (capability == null) {
            return false;
        }
        
        // 检查用户容量
        if (userCount > 0 && capability.getRemainingUserCapacity() < userCount) {
            log.warn("[DeviceCapability] 用户容量不足: deviceId={}, need={}, remaining={}",
                deviceId, userCount, capability.getRemainingUserCapacity());
            return false;
        }
        
        // 检查卡片容量
        if (cardCount > 0 && capability.getRemainingCardCapacity() < cardCount) {
            log.warn("[DeviceCapability] 卡片容量不足: deviceId={}, need={}, remaining={}",
                deviceId, cardCount, capability.getRemainingCardCapacity());
            return false;
        }
        
        // 检查人脸容量
        if (faceCount > 0 && capability.getRemainingFaceCapacity() < faceCount) {
            log.warn("[DeviceCapability] 人脸容量不足: deviceId={}, need={}, remaining={}",
                deviceId, faceCount, capability.getRemainingFaceCapacity());
            return false;
        }
        
        // 检查指纹容量
        if (fingerprintCount > 0 && capability.getRemainingFingerprintCapacity() < fingerprintCount) {
            log.warn("[DeviceCapability] 指纹容量不足: deviceId={}, need={}, remaining={}",
                deviceId, fingerprintCount, capability.getRemainingFingerprintCapacity());
            return false;
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCapability(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        capabilityMapper.deleteByDeviceId(deviceId);
        log.info("[DeviceCapability] 删除设备能力缓存: deviceId={}", deviceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredCache() {
        // 清理过期缓存的逻辑可以通过定时任务调用
        // 这里暂时不实现具体的清理逻辑，因为缓存会在下次访问时自动刷新
        log.info("[DeviceCapability] 清理过期缓存");
    }

    @Override
    public CapabilityCheckResult preCheckCapability(Long deviceId, List<IotAccessPersonCredentialDO> credentials) {
        if (deviceId == null) {
            return CapabilityCheckResult.failure("设备ID不能为空");
        }
        
        log.info("[DeviceCapability] 开始设备能力预检查: deviceId={}, credentialCount={}", 
                deviceId, credentials != null ? credentials.size() : 0);
        
        CapabilityCheckResult result = CapabilityCheckResult.builder()
                .passed(true)
                .canProceed(true)
                .supportedCredentialTypes(new ArrayList<>())
                .unsupportedCredentialTypes(new ArrayList<>())
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();
        
        // 1. 检查设备是否在线
        if (!isDeviceOnline(deviceId)) {
            result.addError(CapabilityErrorType.DEVICE_NOT_CONNECTED, "设备未连接或离线");
            result.setCanProceed(false);
            return result;
        }
        
        // 2. 获取设备能力
        IotAccessDeviceCapabilityDO capability = getCapability(deviceId);
        if (capability == null) {
            result.addWarning("无法获取设备能力信息，将使用默认配置");
            // 仍然可以继续，使用默认配置
            return result;
        }
        
        // 3. 设置剩余容量信息
        result.setRemainingUserCapacity(capability.getRemainingUserCapacity());
        result.setRemainingCardCapacity(capability.getRemainingCardCapacity());
        result.setRemainingFaceCapacity(capability.getRemainingFaceCapacity());
        result.setRemainingFingerprintCapacity(capability.getRemainingFingerprintCapacity());
        
        // 4. 检查用户容量（每个人员需要1个用户位置）
        boolean userCapacitySufficient = capability.getRemainingUserCapacity() >= 1;
        if (!userCapacitySufficient) {
            result.addError(CapabilityErrorType.USER_CAPACITY_INSUFFICIENT, 
                    String.format("设备用户容量不足，剩余容量: %d", capability.getRemainingUserCapacity()));
            result.setCanProceed(false);
            // 用户容量不足时，直接返回，不再检查凭证
            return result;
        }
        
        // 5. 检查各类凭证的容量和功能支持
        if (credentials != null && !credentials.isEmpty()) {
            checkCredentialCapabilities(capability, credentials, result);
        }
        
        // 6. 确定是否可以继续
        // 如果有支持的凭证类型，即使部分不支持也可以继续
        // 注意：只有在用户容量充足的情况下才能继续
        if (!result.getSupportedCredentialTypes().isEmpty()) {
            result.setCanProceed(true);
        }
        
        log.info("[DeviceCapability] 设备能力预检查完成: deviceId={}, passed={}, canProceed={}, errors={}", 
                deviceId, result.isPassed(), result.isCanProceed(), result.getErrors().size());
        
        return result;
    }
    
    /**
     * 检查凭证相关的能力
     * 
     * 注意：凭证类型比较使用 CredentialTypeConstants 的方法进行大小写不敏感匹配，
     * 以兼容数据库中可能存在的不同大小写格式
     */
    private void checkCredentialCapabilities(IotAccessDeviceCapabilityDO capability,
            List<IotAccessPersonCredentialDO> credentials, CapabilityCheckResult result) {
        
        int faceCount = 0;
        int cardCount = 0;
        int fingerprintCount = 0;
        
        for (IotAccessPersonCredentialDO credential : credentials) {
            String type = credential.getCredentialType();
            if (type == null) {
                result.addWarning("凭证类型为空: credentialId=" + credential.getId());
                continue;
            }
            
            // 使用 CredentialTypeConstants 的方法进行大小写不敏感比较
            if (CredentialTypeConstants.isFace(type)) {
                faceCount++;
            } else if (CredentialTypeConstants.isCard(type)) {
                cardCount++;
            } else if (CredentialTypeConstants.isFingerprint(type)) {
                fingerprintCount++;
            } else if (CredentialTypeConstants.isPassword(type)) {
                // 密码包含在用户信息中，不需要额外容量
                result.addSupportedCredentialType(CredentialTypeConstants.PASSWORD);
            } else {
                result.addWarning("未知凭证类型: " + type + "，期望格式: FACE, CARD, PASSWORD, FINGERPRINT（大小写不敏感）");
            }
        }
        
        // 检查人脸
        if (faceCount > 0) {
            if (!Boolean.TRUE.equals(capability.getSupFaceService() == 1)) {
                result.addError(CapabilityErrorType.FACE_NOT_SUPPORTED, "设备不支持人脸识别功能");
                result.addUnsupportedCredentialType(CredentialTypeConstants.FACE);
            } else if (capability.getRemainingFaceCapacity() < faceCount) {
                result.addError(CapabilityErrorType.FACE_CAPACITY_INSUFFICIENT,
                        String.format("人脸容量不足，需要: %d, 剩余: %d", faceCount, capability.getRemainingFaceCapacity()));
                result.addUnsupportedCredentialType(CredentialTypeConstants.FACE);
            } else {
                result.addSupportedCredentialType(CredentialTypeConstants.FACE);
            }
        }
        
        // 检查卡片
        if (cardCount > 0) {
            // 一代设备不支持独立卡片服务，但卡片信息包含在用户信息中
            if (capability.getDeviceGeneration() != null && capability.getDeviceGeneration() == 1) {
                // 一代设备，卡片通过用户信息下发
                result.addSupportedCredentialType(CredentialTypeConstants.CARD);
                result.addWarning("一代设备卡片信息将包含在用户信息中下发");
            } else if (capability.getRemainingCardCapacity() < cardCount) {
                result.addError(CapabilityErrorType.CARD_CAPACITY_INSUFFICIENT,
                        String.format("卡片容量不足，需要: %d, 剩余: %d", cardCount, capability.getRemainingCardCapacity()));
                result.addUnsupportedCredentialType(CredentialTypeConstants.CARD);
            } else {
                result.addSupportedCredentialType(CredentialTypeConstants.CARD);
            }
        }
        
        // 检查指纹
        if (fingerprintCount > 0) {
            if (!Boolean.TRUE.equals(capability.getSupFingerprintService() == 1)) {
                result.addError(CapabilityErrorType.FINGERPRINT_NOT_SUPPORTED, "设备不支持指纹识别功能");
                result.addUnsupportedCredentialType(CredentialTypeConstants.FINGERPRINT);
            } else if (capability.getRemainingFingerprintCapacity() < fingerprintCount) {
                result.addError(CapabilityErrorType.FINGERPRINT_CAPACITY_INSUFFICIENT,
                        String.format("指纹容量不足，需要: %d, 剩余: %d", fingerprintCount, capability.getRemainingFingerprintCapacity()));
                result.addUnsupportedCredentialType(CredentialTypeConstants.FINGERPRINT);
            } else {
                result.addSupportedCredentialType(CredentialTypeConstants.FINGERPRINT);
            }
        }
    }

    @Override
    public boolean supportsCredentialType(Long deviceId, String credentialType) {
        if (deviceId == null || credentialType == null) {
            return false;
        }
        
        IotAccessDeviceCapabilityDO capability = getCapability(deviceId);
        if (capability == null) {
            // 无法获取能力信息，默认支持
            return true;
        }
        
        // 使用 CredentialTypeConstants 的方法进行大小写不敏感比较
        if (CredentialTypeConstants.isFace(credentialType)) {
            return capability.getSupFaceService() != null && capability.getSupFaceService() == 1;
        }
        if (CredentialTypeConstants.isFingerprint(credentialType)) {
            return capability.getSupFingerprintService() != null && capability.getSupFingerprintService() == 1;
        }
        if (CredentialTypeConstants.isCard(credentialType)) {
            // 卡片总是支持的（一代设备通过用户信息，二代设备通过卡片服务）
            return true;
        }
        if (CredentialTypeConstants.isPassword(credentialType)) {
            // 密码总是支持的
            return true;
        }
        return false;
    }

    @Override
    public List<String> getSupportedCredentialTypes(Long deviceId) {
        List<String> types = new ArrayList<>();
        
        // 密码和卡片总是支持的
        types.add(CredentialTypeConstants.PASSWORD);
        types.add(CredentialTypeConstants.CARD);
        
        IotAccessDeviceCapabilityDO capability = getCapability(deviceId);
        if (capability == null) {
            // 无法获取能力信息，默认支持所有类型
            types.add(CredentialTypeConstants.FACE);
            types.add(CredentialTypeConstants.FINGERPRINT);
            return types;
        }
        
        if (capability.getSupFaceService() != null && capability.getSupFaceService() == 1) {
            types.add(CredentialTypeConstants.FACE);
        }
        
        if (capability.getSupFingerprintService() != null && capability.getSupFingerprintService() == 1) {
            types.add(CredentialTypeConstants.FINGERPRINT);
        }
        
        return types;
    }

    @Override
    public boolean isDeviceOnline(Long deviceId) {
        if (deviceId == null) {
            return false;
        }
        
        try {
            // 获取设备信息
            IotDeviceDO device = deviceService.getAccessDevice(deviceId);
            if (device == null) {
                log.warn("[DeviceCapability] 设备不存在: deviceId={}", deviceId);
                return false;
            }
            
            // 获取设备连接信息
            String ip = getDeviceIp(device);
            Integer port = getDevicePort(device);
            String deviceType = getAccessDeviceType(device);
            
            if (ip == null || ip.isEmpty()) {
                log.warn("[DeviceCapability] 设备IP为空: deviceId={}", deviceId);
                return false;
            }
            
            // 通过消息总线检查设备在线状态
            return messageBusClient.isDeviceOnline(deviceId, ip, port, deviceType);
        } catch (Exception e) {
            log.warn("[DeviceCapability] 检查设备在线状态异常: deviceId={}", deviceId, e);
            return false;
        }
    }
}
