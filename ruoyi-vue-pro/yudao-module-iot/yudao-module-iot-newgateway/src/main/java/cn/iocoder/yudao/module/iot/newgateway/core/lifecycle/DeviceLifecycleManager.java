package cn.iocoder.yudao.module.iot.newgateway.core.lifecycle;

import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceStatusInfo;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备生命周期管理器
 * 
 * <p>统一管理设备状态转换，主动连接和被动连接设备使用相同的状态转换规则：</p>
 * <ul>
 *   <li>状态转换: INACTIVE → ONLINE ↔ OFFLINE</li>
 * </ul>
 * 
 * <p>状态变更时会通过 DeviceStateChangePublisher 发送消息到消息总线，
 * 供 Biz 服务消费并更新数据库状态。</p>
 * 
 * <p>复用 iot-core 中的公共类：</p>
 * <ul>
 *   <li>IotDeviceStateEnum - 设备状态枚举</li>
 *   <li>ConnectionMode - 连接模式枚举</li>
 *   <li>DeviceStatusInfo - 设备状态信息</li>
 * </ul>
 * 
 * <p>Requirements: 1.1, 1.4</p>
 *
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Component
public class DeviceLifecycleManager {

    /**
     * 状态变更消息发布器
     */
    private final DeviceStateChangePublisher stateChangePublisher;
    
    /**
     * 设备通用 API（用于同步更新数据库状态）
     */
    @Resource(name = "iotDeviceCommonApiImpl")
    private IotDeviceCommonApi deviceApi;
    
    /**
     * 构造函数
     *
     * @param stateChangePublisher 状态变更消息发布器
     */
    public DeviceLifecycleManager(DeviceStateChangePublisher stateChangePublisher) {
        this.stateChangePublisher = stateChangePublisher;
    }

    /**
     * 设备状态存储
     * Key: deviceId
     * Value: 设备状态
     */
    private final Map<Long, IotDeviceStateEnum> deviceStates = new ConcurrentHashMap<>();

    /**
     * 设备最后活跃时间存储
     * Key: deviceId
     * Value: 最后活跃时间戳（毫秒）
     */
    private final Map<Long, Long> lastSeenTimestamps = new ConcurrentHashMap<>();

    /**
     * 设备连接模式存储
     * Key: deviceId
     * Value: 连接模式
     */
    private final Map<Long, ConnectionMode> deviceConnectionModes = new ConcurrentHashMap<>();

    /**
     * 设备元数据存储（用于构建状态变更消息）
     * Key: deviceId
     * Value: 设备元数据
     */
    private final Map<Long, DeviceMetadata> deviceMetadataMap = new ConcurrentHashMap<>();

    /**
     * 设备锁存储（用于原子操作）
     * Key: deviceId
     * Value: 锁对象
     */
    private final Map<Long, Object> deviceLocks = new ConcurrentHashMap<>();

    // 注意：已移除 lastDbSyncTimestamps 和 DB_SYNC_THROTTLE_MS
    // 状态同步统一通过 RocketMQ 消息总线处理，不再需要节流机制

    // ==================== 原子操作方法（推荐使用） ====================

    /**
     * 获取设备锁对象
     * <p>
     * 每个设备有独立的锁，避免全局锁导致的性能问题。
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备锁对象
     */
    private Object getDeviceLock(Long deviceId) {
        return deviceLocks.computeIfAbsent(deviceId, k -> new Object());
    }

    /**
     * 设备连接（原子操作）- 被动连接设备使用
     * <p>
     * 原子地完成：注册元数据 → 上线，两个状态转换。
     * 使用设备级别的锁，保证同一设备的状态转换是串行的。
     * </p>
     * 
     * <p>适用场景：被动连接设备（如长辉TCP、报警主机）首次连接时调用</p>
     *
     * @param deviceId       设备ID
     * @param connectionInfo 设备连接信息（包含元数据）
     * @return 是否上线成功
     */
    public boolean onDeviceConnected(Long deviceId, cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo connectionInfo) {
        if (deviceId == null) {
            log.warn("[DeviceLifecycleManager] onDeviceConnected: deviceId 不能为空");
            return false;
        }

        synchronized (getDeviceLock(deviceId)) {
            try {
                log.info("[DeviceLifecycleManager] 开始处理设备连接（原子操作）: deviceId={}", deviceId);
                
                // 1. 注册设备元数据
                if (connectionInfo != null) {
                    onDeviceRegisteredInternal(deviceId, connectionInfo);
                } else {
                    onDeviceRegisteredInternal(deviceId, null, null, null, null, ConnectionMode.PASSIVE, null);
                }
                
                // 2. 设备直接上线（不再需要中间的激活状态）
                boolean online = onDeviceOnlineInternal(deviceId);
                if (online) {
                    log.info("[DeviceLifecycleManager] 设备连接处理完成（原子操作）: deviceId={}, state=ONLINE", deviceId);
                } else {
                    log.warn("[DeviceLifecycleManager] 设备上线失败: deviceId={}", deviceId);
                }
                
                return online;
            } catch (Exception e) {
                log.error("[DeviceLifecycleManager] 设备连接处理异常: deviceId={}", deviceId, e);
                return false;
            }
        }
    }

    /**
     * 设备断开（原子操作）
     * <p>
     * 原子地将设备状态设置为离线。
     * 使用设备级别的锁，保证同一设备的状态转换是串行的。
     * </p>
     *
     * @param deviceId 设备ID
     * @param reason   断开原因
     * @return 是否成功设置为离线
     */
    public boolean onDeviceDisconnected(Long deviceId, String reason) {
        if (deviceId == null) {
            log.warn("[DeviceLifecycleManager] onDeviceDisconnected: deviceId 不能为空");
            return false;
        }

        synchronized (getDeviceLock(deviceId)) {
            try {
                log.info("[DeviceLifecycleManager] 开始处理设备断开（原子操作）: deviceId={}, reason={}", deviceId, reason);
                
                boolean offline = onDeviceOfflineInternal(deviceId, reason);
                if (offline) {
                    log.info("[DeviceLifecycleManager] 设备断开处理完成（原子操作）: deviceId={}, state=OFFLINE", deviceId);
                }
                
                return offline;
            } catch (Exception e) {
                log.error("[DeviceLifecycleManager] 设备断开处理异常: deviceId={}", deviceId, e);
                return false;
            }
        }
    }

    /**
     * 设备断开（原子操作，默认原因）
     *
     * @param deviceId 设备ID
     * @return 是否成功设置为离线
     */
    public boolean onDeviceDisconnected(Long deviceId) {
        return onDeviceDisconnected(deviceId, "连接断开");
    }

    /**
     * 设备登录（原子操作）- 主动连接设备使用
     * <p>
     * 原子地完成：注册元数据 → 直接上线（跳过激活），两个状态转换。
     * 使用设备级别的锁，保证同一设备的状态转换是串行的。
     * </p>
     * 
     * <p>适用场景：主动连接设备（如NVR）登录成功时调用</p>
     * <p>状态转换路径：INACTIVE → ONLINE</p>
     *
     * @param deviceId       设备ID
     * @param connectionInfo 设备连接信息（包含元数据）
     * @return 是否上线成功
     */
    public boolean onDeviceLogin(Long deviceId, cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo connectionInfo) {
        if (deviceId == null) {
            log.warn("[DeviceLifecycleManager] onDeviceLogin: deviceId 不能为空");
            return false;
        }

        synchronized (getDeviceLock(deviceId)) {
            try {
                log.info("[DeviceLifecycleManager] 开始处理设备登录（原子操作，主动连接）: deviceId={}", deviceId);
                
                // 1. 注册设备元数据
                if (connectionInfo != null) {
                    onDeviceRegisteredInternal(deviceId, connectionInfo);
                } else {
                    onDeviceRegisteredInternal(deviceId, null, null, null, null, ConnectionMode.ACTIVE, null);
                }
                
                // 2. 直接上线（主动连接设备跳过激活步骤）
                boolean online = onDeviceOnlineInternal(deviceId);
                if (online) {
                    log.info("[DeviceLifecycleManager] 设备登录处理完成（原子操作）: deviceId={}, state=ONLINE", deviceId);
                } else {
                    log.warn("[DeviceLifecycleManager] 设备上线失败: deviceId={}", deviceId);
                }
                
                return online;
            } catch (Exception e) {
                log.error("[DeviceLifecycleManager] 设备登录处理异常: deviceId={}", deviceId, e);
                return false;
            }
        }
    }

    /**
     * 设备登出（原子操作）- 主动连接设备使用
     * <p>
     * 原子地将设备状态设置为离线。
     * 使用设备级别的锁，保证同一设备的状态转换是串行的。
     * </p>
     * 
     * <p>适用场景：主动连接设备（如NVR）登出时调用</p>
     *
     * @param deviceId 设备ID
     * @param reason   登出原因
     * @return 是否成功设置为离线
     */
    public boolean onDeviceLogout(Long deviceId, String reason) {
        if (deviceId == null) {
            log.warn("[DeviceLifecycleManager] onDeviceLogout: deviceId 不能为空");
            return false;
        }

        synchronized (getDeviceLock(deviceId)) {
            try {
                log.info("[DeviceLifecycleManager] 开始处理设备登出（原子操作）: deviceId={}, reason={}", deviceId, reason);
                
                boolean offline = onDeviceOfflineInternal(deviceId, reason);
                if (offline) {
                    log.info("[DeviceLifecycleManager] 设备登出处理完成（原子操作）: deviceId={}, state=OFFLINE", deviceId);
                }
                
                return offline;
            } catch (Exception e) {
                log.error("[DeviceLifecycleManager] 设备登出处理异常: deviceId={}", deviceId, e);
                return false;
            }
        }
    }

    /**
     * 设备登出（原子操作，默认原因）
     *
     * @param deviceId 设备ID
     * @return 是否成功设置为离线
     */
    public boolean onDeviceLogout(Long deviceId) {
        return onDeviceLogout(deviceId, "设备登出");
    }

    // ==================== 内部状态转换方法（不加锁，供原子方法调用） ====================

    /**
     * 内部注册方法（不加锁）
     */
    private void onDeviceRegisteredInternal(Long deviceId, cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo connectionInfo) {
        if (connectionInfo == null) {
            onDeviceRegisteredInternal(deviceId, null, null, null, null, null, null);
            return;
        }
        onDeviceRegisteredInternal(
                deviceId,
                connectionInfo.getDeviceName(),
                connectionInfo.getDeviceType(),
                connectionInfo.getProductId(),
                connectionInfo.getTenantId(),
                connectionInfo.getConnectionMode(),
                connectionInfo.getVendor()
        );
    }

    /**
     * 内部注册方法（不加锁）
     */
    private void onDeviceRegisteredInternal(Long deviceId, String deviceName, String deviceType,
                                             Long productId, Long tenantId, ConnectionMode connectionMode,
                                             String vendor) {
        IotDeviceStateEnum newState = IotDeviceStateEnum.INACTIVE;
        
        deviceStates.put(deviceId, newState);
        lastSeenTimestamps.put(deviceId, System.currentTimeMillis());
        if (connectionMode != null) {
            deviceConnectionModes.put(deviceId, connectionMode);
        }
        
        // 存储设备元数据
        DeviceMetadata metadata = deviceMetadataMap.computeIfAbsent(deviceId, k -> new DeviceMetadata());
        metadata.setDeviceId(deviceId);
        if (deviceName != null) metadata.setDeviceName(deviceName);
        if (deviceType != null) metadata.setDeviceType(deviceType);
        if (productId != null) metadata.setProductId(productId);
        if (tenantId != null) metadata.setTenantId(tenantId);
        if (vendor != null) metadata.setVendor(vendor);
        
        log.debug("[DeviceLifecycleManager] 内部注册完成: deviceId={}, state={}", deviceId, newState);
    }

    /**
     * 内部上线方法（不加锁）
     * <p>
     * 状态更新策略：
     * <ol>
     *   <li>更新 Gateway 本地内存状态</li>
     *   <li>发送 MQ 消息通知 Biz 服务（由 DeviceStateChangeConsumer 统一处理数据库更新、WebSocket推送等）</li>
     * </ol>
     * </p>
     * <p>
     * 注意：移除了同步 RPC 调用 batchUpdateDeviceState，避免与 MQ 消息处理重复。
     * 所有状态变更统一通过 RocketMQ 消息总线，由 Biz 端的 DeviceStateChangeConsumer 处理。
     * </p>
     */
    private boolean onDeviceOnlineInternal(Long deviceId) {
        IotDeviceStateEnum currentState = deviceStates.get(deviceId);
        IotDeviceStateEnum newState = IotDeviceStateEnum.ONLINE;
        ConnectionMode connectionMode = deviceConnectionModes.get(deviceId);
        
        // 如果已经在线，跳过重复处理
        if (currentState == IotDeviceStateEnum.ONLINE) {
            log.debug("[DeviceLifecycleManager] 设备已在线，跳过: deviceId={}", deviceId);
            return true;
        }
        
        // 验证状态转换是否有效
        if (currentState != null && connectionMode != null) {
            if (!IotDeviceStateEnum.isValidTransition(currentState, newState, connectionMode)) {
                log.warn("[DeviceLifecycleManager] 内部上线: 无效的状态转换: deviceId={}, from={}, to={}, mode={}",
                        deviceId, currentState, newState, connectionMode);
                return false;
            }
        }
        
        // 1. 更新 Gateway 本地内存状态
        deviceStates.put(deviceId, newState);
        lastSeenTimestamps.put(deviceId, System.currentTimeMillis());
        
        // 2. 发布状态变更消息（由 Biz 端 DeviceStateChangeConsumer 统一处理）
        // 包括：更新数据库状态、WebSocket 推送、触发通道同步等
        publishStateChange(deviceId, currentState, newState, "设备上线");
        
        log.info("[DeviceLifecycleManager] 设备上线消息已发送: deviceId={}, mode={}", deviceId, connectionMode);
        
        return true;
    }
    
    /**
     * 确保数据库状态与内存状态同步（已废弃）
     * <p>
     * 注意：此方法已废弃，不再执行任何操作。
     * 状态同步统一通过 RocketMQ 消息总线处理，由 Biz 端的 DeviceStateChangeConsumer 负责。
     * </p>
     * <p>
     * 废弃原因：避免 RPC 调用与 MQ 消息处理的重复更新，导致日志刷屏和资源浪费。
     * </p>
     *
     * @param deviceId       设备ID（未使用）
     * @param connectionMode 连接模式（未使用）
     * @deprecated 使用 RocketMQ 消息总线统一处理状态同步
     */
    @Deprecated
    private void ensureDatabaseStateSync(Long deviceId, ConnectionMode connectionMode) {
        // 方法已废弃，不执行任何操作
        // 状态同步统一通过 RocketMQ 消息总线，由 DeviceStateChangeConsumer 处理
        log.trace("[DeviceLifecycleManager] ensureDatabaseStateSync 已废弃，跳过: deviceId={}", deviceId);
    }

    /**
     * 内部离线方法（不加锁）
     */
    private boolean onDeviceOfflineInternal(Long deviceId, String reason) {
        IotDeviceStateEnum currentState = deviceStates.get(deviceId);
        IotDeviceStateEnum newState = IotDeviceStateEnum.OFFLINE;
        ConnectionMode connectionMode = deviceConnectionModes.get(deviceId);
        
        // 如果已经离线，跳过
        if (currentState == IotDeviceStateEnum.OFFLINE) {
            log.debug("[DeviceLifecycleManager] 设备已离线，跳过: deviceId={}", deviceId);
            return true;
        }
        
        // 验证状态转换是否有效
        if (currentState != null && connectionMode != null) {
            if (!IotDeviceStateEnum.isValidTransition(currentState, newState, connectionMode)) {
                log.warn("[DeviceLifecycleManager] 内部离线: 无效的状态转换: deviceId={}, from={}, to={}, mode={}",
                        deviceId, currentState, newState, connectionMode);
                return false;
            }
        }
        
        deviceStates.put(deviceId, newState);
        // 离线时不更新 lastSeenTimestamp，保留最后一次活跃时间
        
        // 发布状态变更消息
        publishStateChange(deviceId, currentState, newState, reason);
        
        return true;
    }

    // ==================== 设备注册 ====================

    /**
     * 设备注册（完整版，包含元数据和 vendor）
     * 当设备首次添加到系统时调用
     *
     * @param deviceId       设备ID
     * @param deviceName     设备名称
     * @param deviceType     设备类型
     * @param productId      产品ID
     * @param tenantId       租户ID
     * @param connectionMode 连接模式
     * @param vendor         厂商
     */
    public void onDeviceRegistered(Long deviceId, String deviceName, String deviceType,
                                    Long productId, Long tenantId, ConnectionMode connectionMode,
                                    String vendor) {
        if (deviceId == null) {
            log.warn("[DeviceLifecycleManager] onDeviceRegistered: deviceId 不能为空");
            return;
        }
        
        IotDeviceStateEnum currentState = deviceStates.get(deviceId);
        IotDeviceStateEnum newState = IotDeviceStateEnum.INACTIVE;
        
        // 如果设备已存在且不是 INACTIVE 状态，记录警告但仍然更新
        if (currentState != null && currentState != IotDeviceStateEnum.INACTIVE) {
            log.warn("[DeviceLifecycleManager] 设备已存在且状态为 {}, deviceId={}", currentState, deviceId);
        }
        
        deviceStates.put(deviceId, newState);
        lastSeenTimestamps.put(deviceId, System.currentTimeMillis());
        if (connectionMode != null) {
            deviceConnectionModes.put(deviceId, connectionMode);
        }
        
        // 存储设备元数据（包含 vendor）
        DeviceMetadata metadata = new DeviceMetadata();
        metadata.setDeviceId(deviceId);
        metadata.setDeviceName(deviceName);
        metadata.setDeviceType(deviceType);
        metadata.setProductId(productId);
        metadata.setTenantId(tenantId);
        metadata.setVendor(vendor);
        deviceMetadataMap.put(deviceId, metadata);
        
        log.info("[DeviceLifecycleManager] 设备注册成功: deviceId={}, deviceName={}, deviceType={}, vendor={}, connectionMode={}, state={}",
                deviceId, deviceName, deviceType, vendor, connectionMode, newState);
    }

    /**
     * 设备注册（简化版，仅指定连接模式）
     *
     * @param deviceId       设备ID
     * @param connectionMode 连接模式
     */
    public void onDeviceRegistered(Long deviceId, ConnectionMode connectionMode) {
        onDeviceRegistered(deviceId, null, null, null, null, connectionMode, null);
    }

    /**
     * 设备注册（使用 DeviceConnectionInfo）
     * 从连接信息中提取元数据进行注册
     *
     * @param deviceId       设备ID
     * @param connectionInfo 设备连接信息
     */
    public void onDeviceRegistered(Long deviceId, cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo connectionInfo) {
        if (connectionInfo == null) {
            onDeviceRegistered(deviceId, (ConnectionMode) null);
            return;
        }
        onDeviceRegistered(
                deviceId,
                connectionInfo.getDeviceName(),
                connectionInfo.getDeviceType(),
                connectionInfo.getProductId(),
                connectionInfo.getTenantId(),
                connectionInfo.getConnectionMode(),
                connectionInfo.getVendor()
        );
    }

    /**
     * 设备上线（使用 DeviceConnectionInfo）
     * 同时注册设备元数据并设置为在线状态
     *
     * @param deviceId       设备ID
     * @param connectionInfo 设备连接信息
     * @return 是否上线成功
     */
    public boolean onDeviceOnline(Long deviceId, cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo connectionInfo) {
        // 先注册设备元数据
        if (connectionInfo != null) {
            onDeviceRegistered(deviceId, connectionInfo);
        }
        // 然后设置为在线
        return onDeviceOnline(deviceId);
    }

    /**
     * 设备注册（不指定连接模式）
     *
     * @param deviceId 设备ID
     */
    public void onDeviceRegistered(Long deviceId) {
        onDeviceRegistered(deviceId, (ConnectionMode) null);
    }

    // ==================== 状态转换 ====================

    /**
     * 设备上线
     * - 主动连接设备：SDK 登录成功后调用
     * - 被动连接设备：激活后收到心跳时调用
     *
     * @param deviceId 设备ID
     * @return 是否上线成功
     */
    public boolean onDeviceOnline(Long deviceId) {
        if (deviceId == null) {
            log.warn("[DeviceLifecycleManager] onDeviceOnline: deviceId 不能为空");
            return false;
        }
        
        IotDeviceStateEnum currentState = deviceStates.get(deviceId);
        IotDeviceStateEnum newState = IotDeviceStateEnum.ONLINE;
        ConnectionMode connectionMode = deviceConnectionModes.get(deviceId);
        
        // 防重复检查：如果已经在线，跳过
        if (currentState == IotDeviceStateEnum.ONLINE) {
            log.debug("[DeviceLifecycleManager] onDeviceOnline: 设备已在线，跳过: deviceId={}", deviceId);
            return true;
        }
        
        // 验证状态转换是否有效
        if (currentState != null && connectionMode != null) {
            if (!IotDeviceStateEnum.isValidTransition(currentState, newState, connectionMode)) {
                log.warn("[DeviceLifecycleManager] onDeviceOnline: 无效的状态转换: deviceId={}, from={}, to={}, mode={}",
                        deviceId, currentState, newState, connectionMode);
                return false;
            }
        }
        
        deviceStates.put(deviceId, newState);
        lastSeenTimestamps.put(deviceId, System.currentTimeMillis());
        
        log.info("[DeviceLifecycleManager] 设备上线成功: deviceId={}, previousState={}, newState={}",
                deviceId, currentState, newState);
        
        // 发布状态变更消息
        publishStateChange(deviceId, currentState, newState, "设备上线");
        
        return true;
    }

    /**
     * 设备离线
     * - 主动连接设备：登录失败或连接断开时调用
     * - 被动连接设备：心跳超时时调用
     *
     * @param deviceId 设备ID
     * @return 是否离线成功
     */
    public boolean onDeviceOffline(Long deviceId) {
        return onDeviceOffline(deviceId, "设备离线");
    }

    /**
     * 设备离线（带原因）
     *
     * @param deviceId 设备ID
     * @param reason   离线原因
     * @return 是否离线成功
     */
    public boolean onDeviceOffline(Long deviceId, String reason) {
        if (deviceId == null) {
            log.warn("[DeviceLifecycleManager] onDeviceOffline: deviceId 不能为空");
            return false;
        }
        
        IotDeviceStateEnum currentState = deviceStates.get(deviceId);
        IotDeviceStateEnum newState = IotDeviceStateEnum.OFFLINE;
        ConnectionMode connectionMode = deviceConnectionModes.get(deviceId);
        
        // 验证状态转换是否有效
        if (currentState != null && connectionMode != null) {
            if (!IotDeviceStateEnum.isValidTransition(currentState, newState, connectionMode)) {
                log.warn("[DeviceLifecycleManager] onDeviceOffline: 无效的状态转换: deviceId={}, from={}, to={}, mode={}",
                        deviceId, currentState, newState, connectionMode);
                return false;
            }
        }
        
        deviceStates.put(deviceId, newState);
        // 离线时不更新 lastSeenTimestamp，保留最后一次活跃时间
        
        log.info("[DeviceLifecycleManager] 设备离线: deviceId={}, previousState={}, newState={}, reason={}",
                deviceId, currentState, newState, reason);
        
        // 发布状态变更消息
        publishStateChange(deviceId, currentState, newState, reason);
        
        return true;
    }

    // ==================== 心跳更新 ====================

    /**
     * 更新设备最后活跃时间
     * 通常在收到心跳或其他设备消息时调用
     *
     * @param deviceId 设备ID
     */
    public void updateLastSeen(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        lastSeenTimestamps.put(deviceId, System.currentTimeMillis());
        log.debug("[DeviceLifecycleManager] 更新设备最后活跃时间: deviceId={}", deviceId);
        
        // 如果设备在线，定期同步数据库状态（解决状态不一致问题）
        IotDeviceStateEnum currentState = deviceStates.get(deviceId);
        if (currentState == IotDeviceStateEnum.ONLINE) {
            ConnectionMode connectionMode = deviceConnectionModes.get(deviceId);
            ensureDatabaseStateSync(deviceId, connectionMode);
        }
    }


    // ==================== 状态查询 ====================

    /**
     * 获取设备状态信息
     *
     * @param deviceId 设备ID
     * @return 设备状态信息，包含状态和最后活跃时间戳
     */
    public DeviceStatusInfo getDeviceStatus(Long deviceId) {
        if (deviceId == null) {
            return DeviceStatusInfo.of(deviceId, IotDeviceStateEnum.INACTIVE, null);
        }
        
        IotDeviceStateEnum state = deviceStates.getOrDefault(deviceId, IotDeviceStateEnum.INACTIVE);
        Long lastSeen = lastSeenTimestamps.get(deviceId);
        
        return DeviceStatusInfo.of(deviceId, state, lastSeen);
    }

    /**
     * 获取设备当前状态
     *
     * @param deviceId 设备ID
     * @return 设备状态，如果设备不存在则返回 INACTIVE
     */
    public IotDeviceStateEnum getState(Long deviceId) {
        if (deviceId == null) {
            return IotDeviceStateEnum.INACTIVE;
        }
        return deviceStates.getOrDefault(deviceId, IotDeviceStateEnum.INACTIVE);
    }

    /**
     * 获取所有设备状态
     * 
     * <p>用于心跳超时检测等场景，需要遍历所有设备状态</p>
     *
     * @return 设备状态映射（设备ID -> 状态），返回副本以避免并发修改
     */
    public Map<Long, IotDeviceStateEnum> getAllDeviceStates() {
        return new java.util.HashMap<>(deviceStates);
    }

    /**
     * 获取设备最后活跃时间
     *
     * @param deviceId 设备ID
     * @return 最后活跃时间戳（毫秒），如果设备不存在则返回 null
     */
    public Long getLastSeenTimestamp(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return lastSeenTimestamps.get(deviceId);
    }

    /**
     * 获取设备连接模式
     *
     * @param deviceId 设备ID
     * @return 连接模式，如果设备不存在则返回 null
     */
    public ConnectionMode getConnectionMode(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return deviceConnectionModes.get(deviceId);
    }

    /**
     * 设置设备连接模式
     *
     * @param deviceId       设备ID
     * @param connectionMode 连接模式
     */
    public void setConnectionMode(Long deviceId, ConnectionMode connectionMode) {
        if (deviceId == null || connectionMode == null) {
            return;
        }
        deviceConnectionModes.put(deviceId, connectionMode);
        log.debug("[DeviceLifecycleManager] 设置设备连接模式: deviceId={}, mode={}", deviceId, connectionMode);
    }

    /**
     * 判断设备是否在线
     *
     * @param deviceId 设备ID
     * @return 是否在线
     */
    public boolean isOnline(Long deviceId) {
        return getState(deviceId) == IotDeviceStateEnum.ONLINE;
    }

    /**
     * 判断设备是否离线
     *
     * @param deviceId 设备ID
     * @return 是否离线
     */
    public boolean isOffline(Long deviceId) {
        return getState(deviceId) == IotDeviceStateEnum.OFFLINE;
    }

    /**
     * 判断设备是否未激活
     *
     * @param deviceId 设备ID
     * @return 是否未激活
     */
    public boolean isInactive(Long deviceId) {
        return getState(deviceId) == IotDeviceStateEnum.INACTIVE;
    }


    // ==================== 设备管理 ====================

    /**
     * 移除设备
     * 当设备从系统中删除时调用
     *
     * @param deviceId 设备ID
     */
    public void removeDevice(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        deviceStates.remove(deviceId);
        lastSeenTimestamps.remove(deviceId);
        deviceConnectionModes.remove(deviceId);
        deviceMetadataMap.remove(deviceId);
        deviceLocks.remove(deviceId);  // 清理设备锁
        log.info("[DeviceLifecycleManager] 设备已移除: deviceId={}", deviceId);
    }

    /**
     * 获取当前管理的设备数量
     *
     * @return 设备数量
     */
    public int getDeviceCount() {
        return deviceStates.size();
    }

    /**
     * 清空所有设备状态（仅用于测试）
     */
    public void clear() {
        deviceStates.clear();
        lastSeenTimestamps.clear();
        deviceConnectionModes.clear();
        deviceMetadataMap.clear();
        deviceLocks.clear();  // 清理设备锁
        log.info("[DeviceLifecycleManager] 已清空所有设备状态");
    }

    // ==================== 设备元数据管理 ====================

    /**
     * 更新设备元数据（包含 vendor）
     *
     * @param deviceId   设备ID
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param productId  产品ID
     * @param tenantId   租户ID
     * @param vendor     厂商
     */
    public void updateDeviceMetadata(Long deviceId, String deviceName, String deviceType,
                                      Long productId, Long tenantId, String vendor) {
        if (deviceId == null) {
            return;
        }
        
        DeviceMetadata metadata = deviceMetadataMap.computeIfAbsent(deviceId, k -> new DeviceMetadata());
        metadata.setDeviceId(deviceId);
        if (deviceName != null) {
            metadata.setDeviceName(deviceName);
        }
        if (deviceType != null) {
            metadata.setDeviceType(deviceType);
        }
        if (productId != null) {
            metadata.setProductId(productId);
        }
        if (tenantId != null) {
            metadata.setTenantId(tenantId);
        }
        if (vendor != null) {
            metadata.setVendor(vendor);
        }
        
        log.debug("[DeviceLifecycleManager] 更新设备元数据: deviceId={}, deviceName={}, deviceType={}, vendor={}",
                deviceId, deviceName, deviceType, vendor);
    }

    /**
     * 获取设备元数据
     *
     * @param deviceId 设备ID
     * @return 设备元数据，如果不存在则返回 null
     */
    public DeviceMetadata getDeviceMetadata(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return deviceMetadataMap.get(deviceId);
    }

    // ==================== 状态变更消息发布 ====================

    /**
     * 发布状态变更消息
     *
     * @param deviceId      设备ID
     * @param previousState 变更前状态
     * @param newState      变更后状态
     * @param reason        变更原因
     */
    private void publishStateChange(Long deviceId, IotDeviceStateEnum previousState,
                                     IotDeviceStateEnum newState, String reason) {
        if (stateChangePublisher == null) {
            log.debug("[DeviceLifecycleManager] stateChangePublisher 未注入，跳过消息发布");
            return;
        }
        
        DeviceMetadata metadata = deviceMetadataMap.get(deviceId);
        ConnectionMode connectionMode = deviceConnectionModes.get(deviceId);
        
        String deviceName = metadata != null ? metadata.getDeviceName() : null;
        String deviceType = metadata != null ? metadata.getDeviceType() : null;
        Long productId = metadata != null ? metadata.getProductId() : null;
        Long tenantId = metadata != null ? metadata.getTenantId() : null;
        
        stateChangePublisher.publishStateChange(
                deviceId, deviceName, deviceType, productId,
                previousState, newState, tenantId, connectionMode, reason);
    }

    // ==================== 内部类 ====================

    /**
     * 设备元数据
     * 用于存储设备的基本信息，构建状态变更消息时使用
     */
    @Data
    public static class DeviceMetadata {
        /**
         * 设备ID
         */
        private Long deviceId;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 设备类型
         */
        private String deviceType;

        /**
         * 产品ID
         */
        private Long productId;

        /**
         * 租户ID
         */
        private Long tenantId;

        /**
         * 厂商
         */
        private String vendor;
    }
}
