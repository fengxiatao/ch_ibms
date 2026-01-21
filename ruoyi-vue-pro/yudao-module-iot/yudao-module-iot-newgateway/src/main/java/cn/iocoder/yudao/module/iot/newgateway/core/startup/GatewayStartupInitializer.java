package cn.iocoder.yudao.module.iot.newgateway.core.startup;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.newgateway.core.config.DeviceConfigParser;
import cn.iocoder.yudao.module.iot.newgateway.core.config.GatewayStartupProperties;
import cn.iocoder.yudao.module.iot.newgateway.core.handler.ActiveDeviceHandler;
import cn.iocoder.yudao.module.iot.newgateway.core.handler.DeviceHandler;
import cn.iocoder.yudao.module.iot.newgateway.core.lifecycle.DeviceLifecycleManager;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo;
import cn.iocoder.yudao.module.iot.newgateway.core.model.LoginResult;
import cn.iocoder.yudao.module.iot.newgateway.core.registry.DevicePluginRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 网关启动初始化器
 * <p>
 * 在应用启动时自动加载和初始化所有设备。
 * 支持并行处理、重试机制和状态同步。
 * </p>
 *
 * @author IoT Gateway Team
 */
@Slf4j
@Component
public class GatewayStartupInitializer {

    @Resource
    private GatewayStartupProperties properties;

    @Resource
    private DevicePluginRegistry pluginRegistry;

    @Resource
    private DeviceLifecycleManager lifecycleManager;

    @Resource
    private DeviceConfigParser configParser;

    @Resource
    private DeviceInitRetryManager retryManager;

    @Resource(name = "iotDeviceCommonApiImpl")
    private IotDeviceCommonApi deviceApi;

    /**
     * 初始化统计
     */
    private final InitializationStatistics statistics = new InitializationStatistics();

    /**
     * 线程池
     */
    private ExecutorService executor;

    /**
     * 初始化状态
     */
    private volatile InitializationStatus status = InitializationStatus.NOT_STARTED;

    /**
     * 应用启动完成后触发初始化
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!properties.isEnabled()) {
            log.info("[GatewayStartup] 启动初始化已禁用");
            status = InitializationStatus.DISABLED;
            return;
        }

        log.info("[GatewayStartup] 配置信息: {}", properties.toSummary());
        log.info("[GatewayStartup] 将在 {} 秒后开始初始化设备...", properties.getDelaySeconds());

        // 设置重试回调
        retryManager.setRetryCallback(this::handleRetry);

        // 延迟启动
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(properties.getDelaySeconds());
                initializeDevices();
            } catch (InterruptedException e) {
                log.error("[GatewayStartup] 延迟启动被中断", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * 执行设备初始化
     */
    public void initializeDevices() {
        if (status == InitializationStatus.IN_PROGRESS) {
            log.warn("[GatewayStartup] 初始化正在进行中，忽略重复请求");
            return;
        }

        status = InitializationStatus.IN_PROGRESS;
        statistics.reset();
        statistics.start();

        log.info("[GatewayStartup] ========== 开始设备初始化 ==========");

        try {
            // 1. 获取设备列表
            List<IotDeviceRespDTO> devices = fetchDeviceList();
            if (devices.isEmpty()) {
                log.info("[GatewayStartup] 未找到需要初始化的设备");
                status = InitializationStatus.COMPLETED;
                statistics.finish();
                return;
            }

            // 2. 重置被动连接设备状态（包括黑名单中的设备）
            // 重要：被动连接设备在网关重启后应该显示为离线，等待设备主动连接
            resetPassiveDeviceStates(devices);

            // 3. 过滤设备（只保留需要主动初始化的设备）
            List<IotDeviceRespDTO> filteredDevices = filterDevices(devices);
            statistics.setTotal(filteredDevices.size());
            log.info("[GatewayStartup] 过滤后设备数（主动连接）: {}", filteredDevices.size());

            // 3. 创建线程池
            executor = Executors.newFixedThreadPool(properties.getParallelism(), r -> {
                Thread t = new Thread(r, "device-init");
                t.setDaemon(true);
                return t;
            });

            // 4. 并行初始化
            List<Future<InitializationResult>> futures = new ArrayList<>();
            for (IotDeviceRespDTO device : filteredDevices) {
                futures.add(executor.submit(() -> initializeDevice(device)));

                // 设备间隔
                if (properties.getDeviceIntervalMs() > 0) {
                    try {
                        Thread.sleep(properties.getDeviceIntervalMs());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            // 5. 等待所有任务完成
            for (Future<InitializationResult> future : futures) {
                try {
                    InitializationResult result = future.get(
                            properties.getDeviceTimeoutSeconds(), TimeUnit.SECONDS);
                    statistics.record(result);
                } catch (TimeoutException e) {
                    log.warn("[GatewayStartup] 设备初始化超时");
                } catch (Exception e) {
                    log.error("[GatewayStartup] 获取初始化结果异常: {}", e.getMessage());
                }
            }

            // 6. 关闭线程池
            executor.shutdown();

            status = InitializationStatus.COMPLETED;
            statistics.finish();

            // 7. 输出统计报告
            log.info(statistics.generateReport());

        } catch (Exception e) {
            log.error("[GatewayStartup] 设备初始化异常", e);
            status = InitializationStatus.FAILED;
            statistics.finish();
        }
    }

    /**
     * 初始化单个设备
     */
    public InitializationResult initializeDevice(IotDeviceRespDTO device) {
        Long deviceId = device.getId();
        String deviceName = device.getDeviceName();
        long startTime = System.currentTimeMillis();

        try {
            // 1. 解析配置
            DeviceConnectionInfo connectionInfo = configParser.parse(device.getConfig(), null);
            if (connectionInfo == null) {
                log.warn("[GatewayStartup] 设备配置解析失败，跳过: deviceId={}", deviceId);
                return InitializationResult.skipped(deviceId, null, "配置解析失败");
            }

            // 补充设备信息
            connectionInfo.setDeviceId(deviceId);
            connectionInfo.setDeviceName(deviceName);
            connectionInfo.setProductId(device.getProductId());
            connectionInfo.setTenantId(device.getTenantId());

            String deviceType = connectionInfo.getDeviceType();

            // 2. 验证配置
            DeviceConfigParser.ValidationResult validation = configParser.validate(connectionInfo);
            if (!validation.isValid()) {
                log.warn("[GatewayStartup] 设备配置验证失败，跳过: deviceId={}, reason={}",
                        deviceId, validation.getErrorMessage());
                return InitializationResult.skipped(deviceId, deviceType, validation.getErrorMessage());
            }

            // 3. 检查连接模式（被动连接设备已在 resetPassiveDeviceStates 中处理）
            if (connectionInfo.getConnectionMode() == ConnectionMode.PASSIVE) {
                log.debug("[GatewayStartup] 被动连接设备，跳过主动初始化: deviceId={}, deviceType={}",
                        deviceId, deviceType);
                return InitializationResult.skipped(deviceId, deviceType, "被动连接设备");
            }

            // 4. 查找插件
            DeviceHandler handler = pluginRegistry.getHandler(deviceType);
            if (handler == null) {
                log.warn("[GatewayStartup] 未找到匹配的插件，跳过: deviceId={}, deviceType={}",
                        deviceId, deviceType);
                return InitializationResult.skipped(deviceId, deviceType, "未找到匹配插件");
            }

            // 5. 检查是否为主动连接处理器
            if (!(handler instanceof ActiveDeviceHandler)) {
                log.debug("[GatewayStartup] 插件不支持主动连接，跳过: deviceId={}, handler={}",
                        deviceId, handler.getPluginId());
                return InitializationResult.skipped(deviceId, deviceType, "插件不支持主动连接");
            }

            ActiveDeviceHandler activeHandler = (ActiveDeviceHandler) handler;

            // 6. 执行登录
            log.info("[GatewayStartup] 开始初始化设备: deviceId={}, ip={}, port={}, deviceType={}",
                    deviceId, connectionInfo.getIpAddress(), connectionInfo.getPort(), deviceType);

            LoginResult loginResult = activeHandler.login(connectionInfo);
            long durationMs = System.currentTimeMillis() - startTime;

            if (loginResult.isSuccess()) {
                log.info("[GatewayStartup] ✅ 设备初始化成功: deviceId={}, ip={}, 耗时={}ms",
                        deviceId, connectionInfo.getIpAddress(), durationMs);

                // 注意：不需要再调用 lifecycleManager.onDeviceOnline()
                // 因为 ActiveDeviceHandler.login() 内部已经通过 onDeviceLogin() 更新了状态
                // 重复调用会导致两次 DEVICE_STATE_CHANGED 消息

                return InitializationResult.success(deviceId, deviceType, durationMs);
            } else {
                log.warn("[GatewayStartup] ❌ 设备初始化失败: deviceId={}, ip={}, reason={}",
                        deviceId, connectionInfo.getIpAddress(), loginResult.getErrorMessage());

                // 更新设备状态为离线
                lifecycleManager.onDeviceOffline(deviceId, loginResult.getErrorMessage());

                // 加入重试队列
                retryManager.scheduleRetry(deviceId, deviceName, connectionInfo, loginResult.getErrorMessage());

                return InitializationResult.failed(deviceId, deviceType, loginResult.getErrorMessage(), durationMs);
            }

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("[GatewayStartup] 设备初始化异常: deviceId={}, error={}", deviceId, e.getMessage(), e);

            // 加入重试队列
            DeviceConnectionInfo connectionInfo = configParser.parse(device.getConfig(), null);
            if (connectionInfo != null) {
                retryManager.scheduleRetry(deviceId, deviceName, connectionInfo, e.getMessage());
            }

            return InitializationResult.failed(deviceId, null, e.getMessage(), durationMs);
        }
    }

    /**
     * 处理重试回调
     */
    private void handleRetry(RetryContext context) {
        Long deviceId = context.getDeviceId();
        DeviceConnectionInfo connectionInfo = context.getConnectionInfo();

        if (connectionInfo == null) {
            log.warn("[GatewayStartup] 重试时连接信息为空: deviceId={}", deviceId);
            return;
        }

        String deviceType = connectionInfo.getDeviceType();

        // 查找插件
        DeviceHandler handler = pluginRegistry.getHandler(deviceType);
        if (handler == null || !(handler instanceof ActiveDeviceHandler)) {
            log.warn("[GatewayStartup] 重试时未找到有效插件: deviceId={}, deviceType={}", deviceId, deviceType);
            return;
        }

        ActiveDeviceHandler activeHandler = (ActiveDeviceHandler) handler;

        try {
            log.info("[GatewayStartup] 执行设备重试: deviceId={}, ip={}, retryCount={}/{}",
                    deviceId, connectionInfo.getIpAddress(),
                    context.getRetryCount(), properties.getMaxRetryCount());

            LoginResult loginResult = activeHandler.login(connectionInfo);

            if (loginResult.isSuccess()) {
                log.info("[GatewayStartup] ✅ 设备重试成功: deviceId={}, ip={}",
                        deviceId, connectionInfo.getIpAddress());

                // 注意：不需要再调用 lifecycleManager.onDeviceOnline()
                // 因为 ActiveDeviceHandler.login() 内部已经通过 onDeviceLogin() 更新了状态

                // 重置重试计数
                retryManager.resetRetryCount(deviceId);
            } else {
                log.warn("[GatewayStartup] ❌ 设备重试失败: deviceId={}, ip={}, reason={}",
                        deviceId, connectionInfo.getIpAddress(), loginResult.getErrorMessage());

                // 继续调度下一次重试
                retryManager.recordFailureAndScheduleNext(deviceId, loginResult.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("[GatewayStartup] 设备重试异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            retryManager.recordFailureAndScheduleNext(deviceId, e.getMessage());
        }
    }

    /**
     * 获取设备列表
     * <p>
     * 根据配置的 tenantId 决定获取设备的方式：
     * - 如果配置了 tenantId：只获取该租户的设备（单租户本地化部署）
     * - 如果未配置 tenantId：获取所有设备（向后兼容 SaaS 多租户场景）
     * </p>
     */
    private List<IotDeviceRespDTO> fetchDeviceList() {
        try {
            Long tenantId = properties.getTenantId();
            
            if (tenantId != null) {
                // 单租户模式：只获取指定租户的设备
                log.info("[GatewayStartup] 单租户模式: 从 biz 层获取租户 {} 的设备列表...", tenantId);
                var result = deviceApi.getDevicesByTenantId(tenantId);
                if (result.isSuccess() && result.getData() != null) {
                    log.info("[GatewayStartup] 获取到租户 {} 的 {} 个设备", tenantId, result.getData().size());
                    return result.getData();
                }
                log.warn("[GatewayStartup] 获取租户 {} 设备列表失败: {}", tenantId, result.getMsg());
                return new ArrayList<>();
            } else {
                // 兼容模式：获取所有设备（向后兼容，适用于 SaaS 多租户场景）
                log.info("[GatewayStartup] 兼容模式: 从 biz 层获取所有设备列表（未指定 tenantId）...");
                var result = deviceApi.getAllDevices();
                if (result.isSuccess() && result.getData() != null) {
                    log.info("[GatewayStartup] 获取到 {} 个设备", result.getData().size());
                    return result.getData();
                }
                log.warn("[GatewayStartup] 获取设备列表失败: {}", result.getMsg());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[GatewayStartup] 获取设备列表异常: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 重置被动连接设备状态
     * 
     * <p>网关重启后，被动连接设备（如长辉、报警主机）应该显示为离线，
     * 等待设备主动连接后再更新为在线。这个方法会遍历所有设备，
     * 将被动连接设备的状态重置为离线。</p>
     * 
     * <p>状态同步策略：</p>
     * <ol>
     *   <li>同步调用 Biz API 更新数据库状态（确保状态一致性）</li>
     *   <li>更新 Gateway 本地内存状态</li>
     *   <li>发送 MQ 消息通知其他服务（异步通知）</li>
     * </ol>
     * 
     * <p>注意：这个方法会处理所有设备（包括黑名单中的），
     * 因为被动连接设备需要在网关重启时重置状态。</p>
     * 
     * @param devices 设备列表
     */
    private void resetPassiveDeviceStates(List<IotDeviceRespDTO> devices) {
        List<Long> passiveDeviceIds = new ArrayList<>();
        int resetCount = 0;
        
        for (IotDeviceRespDTO device : devices) {
            try {
                DeviceConnectionInfo info = configParser.parse(device.getConfig(), null);
                if (info == null) {
                    continue;
                }
                
                // 只处理被动连接设备
                if (info.getConnectionMode() != ConnectionMode.PASSIVE) {
                    continue;
                }
                
                Long deviceId = device.getId();
                String deviceType = info.getDeviceType();
                String deviceName = device.getDeviceName();
                
                // 设置设备元数据
                info.setDeviceId(deviceId);
                info.setDeviceName(deviceName);
                info.setProductId(device.getProductId());
                info.setTenantId(device.getTenantId());
                
                // 注册设备元数据到本地内存
                lifecycleManager.onDeviceRegistered(deviceId, info);
                
                // 收集需要重置的设备ID
                passiveDeviceIds.add(deviceId);
                
                resetCount++;
                log.debug("[GatewayStartup] 重置被动连接设备状态: deviceId={}, deviceType={}", 
                        deviceId, deviceType);
                
            } catch (Exception e) {
                log.error("[GatewayStartup] 重置设备状态失败: deviceId={}, error={}", 
                        device.getId(), e.getMessage(), e);
            }
        }
        
        if (resetCount > 0) {
            // 通过 RocketMQ 消息总线统一处理状态更新
            // 注意：移除了同步 RPC 调用 batchUpdateDeviceState，避免与 MQ 消息处理重复
            // 由 Biz 端的 DeviceStateChangeConsumer 统一处理数据库更新、WebSocket 推送等
            for (Long deviceId : passiveDeviceIds) {
                lifecycleManager.onDeviceOffline(deviceId, "Gateway重启，等待设备主动连接");
            }
            
            log.info("[GatewayStartup] 已发送 {} 个被动连接设备的离线消息", resetCount);
        }
    }

    /**
     * 过滤设备
     */
    private List<IotDeviceRespDTO> filterDevices(List<IotDeviceRespDTO> devices) {
        return devices.stream()
                .filter(device -> {
                    // 解析配置获取设备类型
                    DeviceConnectionInfo info = configParser.parse(device.getConfig(), null);
                    if (info == null) {
                        return false;
                    }
                    String deviceType = info.getDeviceType();
                    return properties.shouldInitializeDeviceType(deviceType);
                })
                .toList();
    }

    /**
     * 手动重试失败设备
     */
    public void retryFailedDevices() {
        log.info("[GatewayStartup] 手动触发失败设备重试");
        retryManager.retryAllFailed();
    }

    /**
     * 获取初始化状态
     */
    public InitializationStatus getStatus() {
        return status;
    }

    /**
     * 获取统计信息
     */
    public InitializationStatistics getStatistics() {
        return statistics;
    }

    /**
     * 初始化状态枚举
     */
    public enum InitializationStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        DISABLED
    }
}
