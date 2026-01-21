package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlDeviceCommand;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlDeviceResponse;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.ActivationResult;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import cn.iocoder.yudao.module.iot.mq.manager.DeviceCommandResponseManager;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * 门禁控制消息总线客户端
 * 
 * <p>提供同步请求-响应模式，封装消息总线的异步通信</p>
 * <p>用于 iot-biz 模块与 iot-newgateway 模块之间的通信</p>
 * 
 * <p>使用统一的 DEVICE_SERVICE_INVOKE topic 发送命令，
 * 监听 DEVICE_SERVICE_RESULT topic 接收响应，
 * 与 newgateway 的插件架构保持一致</p>
 *
 * @author Kiro
 */
@Slf4j
@Component
public class AccessControlMessageBusClient {

    @Resource
    private IotMessageBus messageBus;
    
    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;
    
    @Resource
    private DeviceCommandResponseManager responseManager;

    /** 默认超时时间（秒） */
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    /**
     * 在线检测超时时间（秒）
     *
     * <p>说明：在线检测是一个“轻量命令”，但会经过 MQ、网关消费者线程、网关命令线程池、回传 MQ、
     * Biz 结果消费者等多个环节。固定 5s 在高峰/抖动场景下容易误判离线。</p>
     */
    private static final int ONLINE_CHECK_TIMEOUT_SECONDS = 10;

    /**
     * 发送命令并同步等待响应
     *
     * @param command 命令
     * @return 响应
     * @throws TimeoutException 超时异常
     */
    public AccessControlDeviceResponse sendAndWait(AccessControlDeviceCommand command) throws TimeoutException {
        return sendAndWait(command, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 发送命令并同步等待响应
     * 
     * <p>使用统一的 DEVICE_SERVICE_INVOKE topic 发送命令到 newgateway</p>
     *
     * @param command 命令
     * @param timeoutSeconds 超时时间（秒）
     * @return 响应
     * @throws TimeoutException 超时异常
     */
    /**
     * 发送命令并同步等待响应
     * 
     * <p>使用统一的 DeviceCommandResponseManager 来等待响应，
     * 响应由 DeviceServiceResultConsumer 统一处理并通知管理器。</p>
     */
    public AccessControlDeviceResponse sendAndWait(AccessControlDeviceCommand command, int timeoutSeconds) 
            throws TimeoutException {
        // 生成请求ID
        String requestId = command.getRequestId();
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
            command.setRequestId(requestId);
        }
        
        // 自动设置租户ID (Requirements: 8.4)
        ensureTenantId(command);

        // 1. 注册请求到响应管理器，保存返回的 future 以避免竞态条件
        java.util.concurrent.CompletableFuture<IotDeviceMessage> future = responseManager.registerRequest(requestId);
        
        try {
            // 2. 构建参数并发送到统一 topic
            String deviceType = getDeviceType(command);
            String commandType = getCommandType(command);
            Map<String, Object> params = buildParams(command);
            
            log.debug("[AccessControlMessageBusClient] 发送命令: requestId={}, deviceType={}, commandType={}, deviceId={}, tenantId={}",
                    requestId, deviceType, commandType, command.getDeviceId(), command.getTenantId());
            
            // 使用 DeviceCommandPublisher 发送到统一 topic
            publishCommand(deviceType, command.getDeviceId(), commandType, params, requestId);

            // 3. 等待响应 - 使用保存的 future 避免竞态条件
            IotDeviceMessage response = responseManager.waitForResponse(requestId, future, timeoutSeconds);
            return convertToResponse(response, requestId);
        } catch (TimeoutException e) {
            throw new TimeoutException("等待响应超时: requestId=" + requestId + ", timeout=" + timeoutSeconds + "s");
        } catch (Exception e) {
            responseManager.cancelRequest(requestId);
            log.error("[AccessControlMessageBusClient] 发送命令异常: requestId={}", requestId, e);
            throw new RuntimeException("发送命令异常: " + e.getMessage(), e);
        }
    }

    /**
     * 异步发送命令
     *
     * <p>使用统一的 DeviceCommandResponseManager 来等待响应，
     * 响应由 DeviceServiceResultConsumer 统一处理并通知管理器。</p>
     *
     * @param command 命令
     * @return CompletableFuture
     */
    public java.util.concurrent.CompletableFuture<AccessControlDeviceResponse> sendAsync(AccessControlDeviceCommand command) {
        // 生成请求ID
        String requestId = command.getRequestId();
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
            command.setRequestId(requestId);
        }
        
        // 自动设置租户ID (Requirements: 8.4)
        ensureTenantId(command);

        // 1. 注册请求到响应管理器
        java.util.concurrent.CompletableFuture<IotDeviceMessage> future = responseManager.registerRequest(requestId);

        // 2. 构建参数并发送到统一 topic
        String deviceType = getDeviceType(command);
        String commandType = getCommandType(command);
        Map<String, Object> params = buildParams(command);
        
        log.debug("[AccessControlMessageBusClient] 异步发送命令: requestId={}, deviceType={}, commandType={}, deviceId={}, tenantId={}",
                requestId, deviceType, commandType, command.getDeviceId(), command.getTenantId());
        
        publishCommand(deviceType, command.getDeviceId(), commandType, params, requestId);

        // 3. 设置超时自动取消并转换响应
        String finalRequestId = requestId;
        return future.orTimeout(DEFAULT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        responseManager.cancelRequest(finalRequestId);
                    }
                })
                .thenApply(msg -> convertToResponse(msg, finalRequestId));
    }

    /**
     * 发送命令不等待响应（fire-and-forget）
     *
     * @param command 命令
     */
    public void send(AccessControlDeviceCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isEmpty()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
        // 自动设置租户ID (Requirements: 8.4)
        ensureTenantId(command);
        
        String deviceType = getDeviceType(command);
        String commandType = getCommandType(command);
        Map<String, Object> params = buildParams(command);
        
        log.debug("[AccessControlMessageBusClient] 发送命令(不等待): deviceType={}, commandType={}, deviceId={}, tenantId={}",
                deviceType, commandType, command.getDeviceId(), command.getTenantId());
        
        publishCommand(deviceType, command.getDeviceId(), commandType, params, command.getRequestId());
    }
    
    /**
     * 确保命令包含租户ID
     * 如果命令中没有设置租户ID，则从当前上下文获取
     * 
     * Requirements: 8.4 - 租户上下文传播
     *
     * @param command 命令
     */
    private void ensureTenantId(AccessControlDeviceCommand command) {
        if (command.getTenantId() == null) {
            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId != null) {
                command.setTenantId(tenantId);
            }
        }
    }
    
    /**
     * 获取设备类型
     * 优先从命令的 params 中获取 deviceType，如果没有则默认使用 ACCESS_GEN1
     * 
     * Requirements: 修复门禁二代设备类型判断问题
     */
    private String getDeviceType(AccessControlDeviceCommand command) {
        // 1. 优先从命令参数中获取已设置的设备类型
        if (command.getParams() != null) {
            Object deviceType = command.getParams().get("deviceType");
            if (deviceType != null && !deviceType.toString().isEmpty()) {
                return deviceType.toString();
            }
        }
        
        // 2. 默认使用 ACCESS_GEN1
        return AccessDeviceTypeConstants.ACCESS_GEN1;
    }
    
    /**
     * 获取命令类型字符串
     */
    private String getCommandType(AccessControlDeviceCommand command) {
        String type = command.getCommandType();
        return type != null ? type : "UNKNOWN";
    }
    
    /**
     * 构建命令参数
     */
    private Map<String, Object> buildParams(AccessControlDeviceCommand command) {
        Map<String, Object> params = new HashMap<>();
        
        // 基础信息
        if (command.getDeviceId() != null) {
            params.put("deviceId", command.getDeviceId());
        }
        if (command.getChannelId() != null) {
            params.put("channelId", command.getChannelId());
        }
        if (command.getChannelNo() != null) {
            params.put("channelNo", command.getChannelNo());
        }
        if (command.getIpAddress() != null) {
            params.put("ipAddress", command.getIpAddress());
        }
        if (command.getPort() != null) {
            params.put("port", command.getPort());
        }
        if (command.getUsername() != null) {
            params.put("username", command.getUsername());
        }
        if (command.getPassword() != null) {
            params.put("password", command.getPassword());
        }
        if (command.getTenantId() != null) {
            params.put("tenantId", command.getTenantId());
        }
        
        // 人员信息
        if (command.getPersonId() != null) {
            params.put("personId", command.getPersonId());
        }
        if (command.getPersonCode() != null) {
            params.put("personCode", command.getPersonCode());
        }
        if (command.getPersonName() != null) {
            params.put("personName", command.getPersonName());
        }
        
        // 卡片信息
        if (command.getCardNo() != null) {
            params.put("cardNo", command.getCardNo());
        }
        if (command.getCardInfo() != null) {
            params.put("cardInfo", command.getCardInfo());
        }
        if (command.getCardInfoList() != null) {
            params.put("cardInfoList", command.getCardInfoList());
        }
        
        // 人脸信息
        if (command.getFaceInfo() != null) {
            params.put("faceInfo", command.getFaceInfo());
        }
        if (command.getFaceInfoList() != null) {
            params.put("faceInfoList", command.getFaceInfoList());
        }
        
        // 指纹信息
        if (command.getFingerprintInfo() != null) {
            params.put("fingerprintInfo", command.getFingerprintInfo());
        }
        if (command.getFingerprintInfoList() != null) {
            params.put("fingerprintInfoList", command.getFingerprintInfoList());
        }
        
        // 用户信息
        if (command.getUserInfo() != null) {
            params.put("userInfo", command.getUserInfo());
        }
        if (command.getUserId() != null) {
            params.put("userId", command.getUserId());
        }
        
        // 权限信息
        if (command.getValidStart() != null) {
            params.put("validStart", command.getValidStart());
        }
        if (command.getValidEnd() != null) {
            params.put("validEnd", command.getValidEnd());
        }
        if (command.getDoorPermissions() != null) {
            params.put("doorPermissions", command.getDoorPermissions());
        }
        
        // 任务信息
        if (command.getTaskId() != null) {
            params.put("taskId", command.getTaskId());
        }
        if (command.getTaskDetailId() != null) {
            params.put("taskDetailId", command.getTaskDetailId());
        }
        
        // 其他参数
        if (command.getParams() != null) {
            params.putAll(command.getParams());
        }
        
        return params;
    }
    
    /**
     * 发布命令到统一 topic
     */
    private void publishCommand(String deviceType, Long deviceId, String commandType, 
                                 Map<String, Object> params, String requestId) {
        // 构建 IotDeviceMessage
        IotDeviceMessage message = IotDeviceMessage.requestOf(requestId, commandType, params);
        message.setDeviceId(deviceId);
        
        // 确保 params 中包含 deviceType 和 commandType
        if (params != null) {
            params.put("deviceType", deviceType);
            params.put("commandType", commandType);
        }
        
        // 发送到统一 topic
        messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, message);
    }
    
    /**
     * 将 IotDeviceMessage 转换为 AccessControlDeviceResponse
     */
    @SuppressWarnings("unchecked")
    private AccessControlDeviceResponse convertToResponse(IotDeviceMessage message, String requestId) {
        if (message == null) {
            return AccessControlDeviceResponse.builder()
                    .requestId(requestId)
                    .success(false)
                    .errorCode(500)
                    .errorMessage("响应为空")
                    .build();
        }
        
        boolean success = message.getCode() != null && message.getCode() == 0;
        
        AccessControlDeviceResponse.AccessControlDeviceResponseBuilder builder = AccessControlDeviceResponse.builder()
                .requestId(requestId)
                .success(success)
                .errorCode(message.getCode())
                .errorMessage(message.getMsg());
        
        // 解析 data 中的额外信息
        Object data = message.getData();
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            
            // 保存完整的 data 供调用方使用
            builder.data(dataMap);

            // 设备能力（QUERY_DEVICE_CAPABILITY）
            // newgateway 返回结构示例：
            // { deviceId, deviceType, capabilities: { hasCard, hasFace, hasFingerprint, maxCardCount, maxDoorCount, ... } }
            Object capsObj = dataMap.get("capabilities");
            if (capsObj instanceof Map) {
                Map<String, Object> caps = (Map<String, Object>) capsObj;
                builder.deviceCapability(AccessControlDeviceResponse.DeviceCapability.builder()
                        .supCardService(getBoolean(caps, "hasCard"))
                        .supFaceService(getBoolean(caps, "hasFace"))
                        .supFingerprintService(getBoolean(caps, "hasFingerprint"))
                        .maxCardCount(getInteger(caps, "maxCardCount"))
                        .maxFaceCount(getInteger(caps, "maxFaceCount"))
                        .maxFingerprintCount(getInteger(caps, "maxFingerprintCount"))
                        .maxUserCount(getInteger(caps, "maxUserCount")) // 可选字段，未返回则为 null
                        .videoChannelCount(getInteger(caps, "videoChannelCount")) // 可选字段
                        .supportVideo(getBoolean(caps, "supportVideo")) // 可选字段
                        // 通道数在 newgateway 中一般是 maxDoorCount
                        .build());
                // maxDoorCount -> videoChannelCount 不合适，这里通过 dataMap 透出给调用方；业务侧可用 dataMap 解析 channels
            }
            
            // 在线状态
            if (dataMap.containsKey("isOnline")) {
                Object isOnlineObj = dataMap.get("isOnline");
                if (isOnlineObj instanceof Boolean) {
                    builder.isOnline((Boolean) isOnlineObj);
                }
            }
            
            // 登录句柄
            if (dataMap.containsKey("loginHandle")) {
                Object handle = dataMap.get("loginHandle");
                if (handle instanceof Number) {
                    builder.loginHandle(((Number) handle).longValue());
                }
            }
            
            // 设备能力
            if (dataMap.containsKey("deviceCapability")) {
                // TODO: 解析设备能力
            }
            
            // 设备状态列表
            if (dataMap.containsKey("deviceStatuses")) {
                // TODO: 解析设备状态列表
            }
        }
        
        return builder.build();
    }

    private static Boolean getBoolean(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        if (v != null) {
            return Boolean.parseBoolean(String.valueOf(v));
        }
        return null;
    }

    private static Integer getInteger(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Integer) {
            return (Integer) v;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        if (v != null) {
            try {
                return Integer.parseInt(String.valueOf(v));
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    // ========== 便捷方法 ==========

    /**
     * 检查设备是否在线
     *
     * @param deviceId 设备ID
     * @param ip 设备IP
     * @param port 设备端口
     * @return 是否在线
     */
    public boolean isDeviceOnline(Long deviceId, String ip, Integer port) {
        // 兼容旧调用：默认按门禁一代路由（历史行为）。强烈建议调用带 deviceType 的重载方法。
        return isDeviceOnline(deviceId, ip, port, AccessDeviceTypeConstants.ACCESS_GEN1);
    }

    /**
     * 检查设备是否在线（显式指定 deviceType，避免被错误路由到错误插件）
     */
    public boolean isDeviceOnline(Long deviceId, String ip, Integer port, String deviceType) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("deviceType", deviceType);
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .commandType(AccessControlDeviceCommand.CommandType.CHECK_DEVICE_ONLINE)
                    .params(params)
                    .build();
            
            AccessControlDeviceResponse response = sendAndWait(command, ONLINE_CHECK_TIMEOUT_SECONDS);
            return response != null && Boolean.TRUE.equals(response.getIsOnline());
        } catch (Exception e) {
            log.warn("[AccessControlMessageBusClient] 检查设备在线状态失败: deviceId={}, error={}", deviceId, e.getMessage());
            return false;
        }
    }

    /**
     * 获取设备登录句柄
     *
     * @param deviceId 设备ID
     * @param ip 设备IP
     * @param port 设备端口
     * @return 登录句柄，失败返回null
     */
    public Long getLoginHandle(Long deviceId, String ip, Integer port) {
        try {
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .commandType(AccessControlDeviceCommand.CommandType.GET_LOGIN_HANDLE)
                    .build();
            
            AccessControlDeviceResponse response = sendAndWait(command, ONLINE_CHECK_TIMEOUT_SECONDS);
            if (response != null && Boolean.TRUE.equals(response.getSuccess())) {
                return response.getLoginHandle();
            }
            return null;
        } catch (Exception e) {
            log.warn("[AccessControlMessageBusClient] 获取登录句柄失败: deviceId={}, error={}", deviceId, e.getMessage());
            return null;
        }
    }

    /**
     * 查询设备能力
     *
     * @param deviceId 设备ID
     * @param ip 设备IP
     * @param port 设备端口
     * @return 设备能力信息，失败返回null
     */
    public AccessControlDeviceResponse.DeviceCapability queryDeviceCapability(Long deviceId, String ip, Integer port) {
        try {
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .commandType(AccessControlDeviceCommand.CommandType.QUERY_DEVICE_CAPABILITY)
                    .build();
            
            AccessControlDeviceResponse response = sendAndWait(command, 10);
            if (response != null && Boolean.TRUE.equals(response.getSuccess())) {
                return response.getDeviceCapability();
            }
            return null;
        } catch (Exception e) {
            log.warn("[AccessControlMessageBusClient] 查询设备能力失败: deviceId={}, error={}", deviceId, e.getMessage());
            return null;
        }
    }

    /**
     * 激活设备（登录设备）- 兼容旧调用
     * 
     * 当设备未激活时，尝试通过Gateway激活设备
     *
     * @param deviceId 设备ID
     * @param ip 设备IP
     * @param port 设备端口
     * @param username 用户名
     * @param password 密码
     * @return 激活结果
     */
    public ActivationResult activateDevice(Long deviceId, String ip, Integer port, String username, String password) {
        return activateDevice(deviceId, ip, port, username, password, AccessDeviceTypeConstants.ACCESS_GEN1);
    }

    /**
     * 激活设备（登录设备）- 显式指定 deviceType
     * 
     * 当设备未激活时，尝试通过Gateway激活设备
     *
     * @param deviceId 设备ID
     * @param ip 设备IP
     * @param port 设备端口
     * @param username 用户名
     * @param password 密码
     * @param deviceType 设备类型（ACCESS_GEN1, ACCESS_GEN2, NVR 等）
     * @return 激活结果
     */
    public ActivationResult activateDevice(Long deviceId, String ip, Integer port, String username, String password, String deviceType) {
        try {
            log.info("[AccessControlMessageBusClient] 尝试激活设备: deviceId={}, ip={}, deviceType={}", deviceId, ip, deviceType);
            
            Map<String, Object> params = new HashMap<>();
            params.put("deviceType", deviceType);
            
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .username(username)
                    .password(password)
                    .commandType(AccessControlDeviceCommand.CommandType.ACTIVATE_DEVICE)
                    .params(params)
                    .build();
            
            long startTime = System.currentTimeMillis();
            AccessControlDeviceResponse response = sendAndWait(command, 30); // 激活可能需要较长时间
            long activationTime = System.currentTimeMillis() - startTime;
            
            if (response != null && Boolean.TRUE.equals(response.getSuccess())) {
                log.info("[AccessControlMessageBusClient] 设备激活成功: deviceId={}, deviceType={}, 耗时={}ms", deviceId, deviceType, activationTime);
                return ActivationResult.success(deviceId, ip, port, activationTime);
            } else {
                String errorMsg = response != null ? response.getErrorMessage() : "激活超时";
                log.warn("[AccessControlMessageBusClient] 设备激活失败: deviceId={}, deviceType={}, error={}", deviceId, deviceType, errorMsg);
                return ActivationResult.failure(deviceId, ip, port, errorMsg);
            }
        } catch (TimeoutException e) {
            log.warn("[AccessControlMessageBusClient] 设备激活超时: deviceId={}, deviceType={}", deviceId, deviceType);
            return ActivationResult.failure(deviceId, ip, port, "激活超时");
        } catch (Exception e) {
            log.error("[AccessControlMessageBusClient] 设备激活异常: deviceId={}, deviceType={}", deviceId, deviceType, e);
            return ActivationResult.failure(deviceId, ip, port, e.getMessage());
        }
    }

    /**
     * 检查设备是否在线，如果不在线则尝试激活 - 兼容旧调用
     *
     * @param deviceId 设备ID
     * @param ip 设备IP
     * @param port 设备端口
     * @param username 用户名
     * @param password 密码
     * @return 是否在线（包括激活后在线）
     */
    public boolean ensureDeviceOnline(Long deviceId, String ip, Integer port, String username, String password) {
        return ensureDeviceOnline(deviceId, ip, port, username, password, AccessDeviceTypeConstants.ACCESS_GEN1);
    }

    /**
     * 检查设备是否在线，如果不在线则尝试激活 - 显式指定 deviceType
     *
     * @param deviceId 设备ID
     * @param ip 设备IP
     * @param port 设备端口
     * @param username 用户名
     * @param password 密码
     * @param deviceType 设备类型（ACCESS_GEN1, ACCESS_GEN2, NVR 等）
     * @return 是否在线（包括激活后在线）
     */
    public boolean ensureDeviceOnline(Long deviceId, String ip, Integer port, String username, String password, String deviceType) {
        // 先检查是否已在线（使用正确的 deviceType）
        if (isDeviceOnline(deviceId, ip, port, deviceType)) {
            return true;
        }
        
        // 尝试激活（使用正确的 deviceType）
        log.info("[AccessControlMessageBusClient] 设备未在线，尝试激活: deviceId={}, deviceType={}", deviceId, deviceType);
        ActivationResult result = activateDevice(deviceId, ip, port, username, password, deviceType);
        
        // 激活成功：直接视为在线，避免"激活已成功但二次在线检测 5s 超时"导致误判离线
        // 在线状态的落库/推送由网关生命周期事件 + biz 侧结果消费者兜底完成。
        return result.isSuccess();
    }

    /**
     * 查询所有设备激活状态
     *
     * @return 设备激活状态列表
     */
    public java.util.List<cn.iocoder.yudao.module.iot.core.gateway.dto.access.DeviceActivationStatus> queryAllDeviceStatus() {
        try {
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .commandType(AccessControlDeviceCommand.CommandType.QUERY_ALL_DEVICE_STATUS)
                    .build();
            
            AccessControlDeviceResponse response = sendAndWait(command, 10);
            if (response != null && Boolean.TRUE.equals(response.getSuccess())) {
                return response.getDeviceStatuses();
            }
            return java.util.Collections.emptyList();
        } catch (Exception e) {
            log.warn("[AccessControlMessageBusClient] 查询设备状态失败: error={}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    // ========== 纯异步下发方法（不等待响应，响应通过 AccessDispatchResponseConsumer 处理）==========

    /**
     * 发送下发用户命令（纯异步，不等待响应）
     * 
     * Requirements: 4.1 - 纯异步发送，不等待响应
     *
     * @param command 下发命令
     */
    public void sendDispatchUserCommand(AccessControlDeviceCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isEmpty()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
        command.setCommandType(AccessControlDeviceCommand.CommandType.DISPATCH_USER);
        // 自动设置租户ID (Requirements: 8.4)
        ensureTenantId(command);
        
        String deviceType = getDeviceType(command);
        Map<String, Object> params = buildParams(command);
        
        log.debug("[sendDispatchUserCommand] 发送下发用户命令: requestId={}, deviceId={}, personId={}, tenantId={}",
                command.getRequestId(), command.getDeviceId(), command.getPersonId(), command.getTenantId());
        
        publishCommand(deviceType, command.getDeviceId(), "DISPATCH_USER", params, command.getRequestId());
    }

    /**
     * 发送下发卡片命令（纯异步，不等待响应）
     * 
     * Requirements: 4.1 - 纯异步发送，不等待响应
     *
     * @param command 下发命令
     */
    public void sendDispatchCardCommand(AccessControlDeviceCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isEmpty()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
        command.setCommandType(AccessControlDeviceCommand.CommandType.DISPATCH_CARD);
        // 自动设置租户ID (Requirements: 8.4)
        ensureTenantId(command);
        
        String deviceType = getDeviceType(command);
        Map<String, Object> params = buildParams(command);
        
        log.debug("[sendDispatchCardCommand] 发送下发卡片命令: requestId={}, deviceId={}, cardNo={}, tenantId={}",
                command.getRequestId(), command.getDeviceId(), command.getCardNo(), command.getTenantId());
        
        publishCommand(deviceType, command.getDeviceId(), "DISPATCH_CARD", params, command.getRequestId());
    }

    /**
     * 发送下发人脸命令（纯异步，不等待响应）
     * 
     * Requirements: 4.1 - 纯异步发送，不等待响应
     *
     * @param command 下发命令
     */
    public void sendDispatchFaceCommand(AccessControlDeviceCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isEmpty()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
        command.setCommandType(AccessControlDeviceCommand.CommandType.DISPATCH_FACE);
        // 自动设置租户ID (Requirements: 8.4)
        ensureTenantId(command);
        
        String deviceType = getDeviceType(command);
        Map<String, Object> params = buildParams(command);
        
        log.debug("[sendDispatchFaceCommand] 发送下发人脸命令: requestId={}, deviceId={}, personId={}, tenantId={}",
                command.getRequestId(), command.getDeviceId(), command.getPersonId(), command.getTenantId());
        
        publishCommand(deviceType, command.getDeviceId(), "DISPATCH_FACE", params, command.getRequestId());
    }

    /**
     * 发送下发指纹命令（纯异步，不等待响应）
     * 
     * Requirements: 4.1 - 纯异步发送，不等待响应
     *
     * @param command 下发命令
     */
    public void sendDispatchFingerprintCommand(AccessControlDeviceCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isEmpty()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
        command.setCommandType(AccessControlDeviceCommand.CommandType.DISPATCH_FINGERPRINT);
        // 自动设置租户ID (Requirements: 8.4)
        ensureTenantId(command);
        
        String deviceType = getDeviceType(command);
        Map<String, Object> params = buildParams(command);
        
        log.debug("[sendDispatchFingerprintCommand] 发送下发指纹命令: requestId={}, deviceId={}, personId={}, tenantId={}",
                command.getRequestId(), command.getDeviceId(), command.getPersonId(), command.getTenantId());
        
        publishCommand(deviceType, command.getDeviceId(), "DISPATCH_FINGERPRINT", params, command.getRequestId());
    }

    /**
     * 发送撤销用户命令（纯异步，不等待响应）
     * 
     * Requirements: 4.1 - 纯异步发送，不等待响应
     *
     * @param command 撤销命令
     */
    public void sendRevokeUserCommand(AccessControlDeviceCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isEmpty()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
        command.setCommandType(AccessControlDeviceCommand.CommandType.REVOKE_USER);
        // 自动设置租户ID (Requirements: 8.4)
        ensureTenantId(command);
        
        String deviceType = getDeviceType(command);
        Map<String, Object> params = buildParams(command);
        
        log.debug("[sendRevokeUserCommand] 发送撤销用户命令: requestId={}, deviceId={}, personId={}, tenantId={}",
                command.getRequestId(), command.getDeviceId(), command.getPersonId(), command.getTenantId());
        
        publishCommand(deviceType, command.getDeviceId(), "REVOKE_USER", params, command.getRequestId());
    }

    /**
     * 发送通用下发命令（纯异步，不等待响应）
     * 
     * Requirements: 4.1 - 纯异步发送，不等待响应
     *
     * @param command 下发命令（需要预先设置 commandType）
     */
    public void sendDispatchCommand(AccessControlDeviceCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isEmpty()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
        // 自动设置租户ID (Requirements: 8.4)
        ensureTenantId(command);
        
        String deviceType = getDeviceType(command);
        String commandType = getCommandType(command);
        Map<String, Object> params = buildParams(command);
        
        log.debug("[sendDispatchCommand] 发送下发命令: requestId={}, commandType={}, deviceId={}, personId={}, tenantId={}",
                command.getRequestId(), commandType, command.getDeviceId(), command.getPersonId(), command.getTenantId());
        
        publishCommand(deviceType, command.getDeviceId(), commandType, params, command.getRequestId());
    }
}
