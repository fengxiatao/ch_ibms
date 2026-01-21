package cn.iocoder.yudao.module.iot.newgateway.core.lifecycle;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.newgateway.core.config.GatewayCoreConfig;
import cn.iocoder.yudao.module.iot.newgateway.plugins.changhui.ChanghuiPlugin;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 心跳超时检测器
 * 
 * <p>定期检查被动连接设备的心跳状态，如果超过阈值未收到心跳则判定为离线。</p>
 * 
 * <p>工作原理：</p>
 * <ol>
 *   <li>定期（heartbeat-check-interval）扫描所有在线的被动连接设备</li>
 *   <li>检查每个设备的 lastSeenTimestamp 与当前时间的差值</li>
 *   <li>如果差值超过 device-offline-threshold，则触发设备离线</li>
 * </ol>
 * 
 * <p>默认配置：</p>
 * <ul>
 *   <li>检测间隔：30秒</li>
 *   <li>离线阈值：90秒（建议为心跳间隔的2-3倍）</li>
 * </ul>
 * 
 * <p>适用设备类型：</p>
 * <ul>
 *   <li>长辉TCP设备（心跳间隔约30秒）</li>
 *   <li>报警主机设备（心跳间隔约30秒）</li>
 *   <li>其他被动连接设备</li>
 * </ul>
 * 
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatTimeoutChecker {

    private static final String LOG_PREFIX = "[HeartbeatTimeoutChecker]";

    private final DeviceLifecycleManager lifecycleManager;
    private final GatewayCoreConfig coreConfig;

    /**
     * 长辉设备插件（用于检查升级保护状态）
     * <p>
     * 使用 setter 注入避免循环依赖
     * </p>
     */
    @Autowired(required = false)
    private ChanghuiPlugin changhuiPlugin;

    /**
     * 定时任务执行器
     */
    private ScheduledExecutorService scheduler;

    /**
     * 是否已停止
     */
    private volatile boolean stopped = false;

    /**
     * 初始化：启动定时任务
     */
    @PostConstruct
    public void init() {
        long checkInterval = coreConfig.getHeartbeatCheckInterval();
        long offlineThreshold = coreConfig.getDeviceOfflineThreshold();

        log.info("{} 初始化心跳超时检测器: checkInterval={}ms, offlineThreshold={}ms",
                LOG_PREFIX, checkInterval, offlineThreshold);

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-timeout-checker");
            t.setDaemon(true);
            return t;
        });

        // 延迟启动，等待系统初始化完成
        scheduler.scheduleAtFixedRate(
                this::checkHeartbeatTimeouts,
                checkInterval,     // 初始延迟
                checkInterval,     // 检测间隔
                TimeUnit.MILLISECONDS
        );

        log.info("{} 心跳超时检测器已启动", LOG_PREFIX);
    }

    /**
     * 销毁：停止定时任务
     */
    @PreDestroy
    public void destroy() {
        stopped = true;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("{} 心跳超时检测器已停止", LOG_PREFIX);
    }

    /**
     * 执行心跳超时检测
     * 
     * <p>扫描所有在线的被动连接设备，检查是否心跳超时</p>
     * <p>注意：正在升级保护期内的设备会跳过检测</p>
     */
    private void checkHeartbeatTimeouts() {
        if (stopped) {
            return;
        }

        try {
            long now = System.currentTimeMillis();
            long offlineThreshold = coreConfig.getDeviceOfflineThreshold();
            int checkedCount = 0;
            int offlineCount = 0;
            int skippedUpgradeCount = 0;

            // 获取升级保护设备集合
            Set<Long> upgradeProtectedDevices = getUpgradeProtectedDevices();

            // 获取所有设备状态
            Map<Long, IotDeviceStateEnum> deviceStates = lifecycleManager.getAllDeviceStates();

            for (Map.Entry<Long, IotDeviceStateEnum> entry : deviceStates.entrySet()) {
                Long deviceId = entry.getKey();
                IotDeviceStateEnum state = entry.getValue();

                // 只检查在线状态的设备
                if (state != IotDeviceStateEnum.ONLINE) {
                    continue;
                }

                // 只检查被动连接设备
                ConnectionMode mode = lifecycleManager.getConnectionMode(deviceId);
                if (mode != ConnectionMode.PASSIVE) {
                    continue;
                }

                // 跳过正在升级保护期内的设备（4G网络优化）
                if (upgradeProtectedDevices.contains(deviceId)) {
                    skippedUpgradeCount++;
                    log.trace("{} 跳过升级保护中的设备: deviceId={}", LOG_PREFIX, deviceId);
                    continue;
                }

                checkedCount++;

                // 获取最后活跃时间
                Long lastSeen = lifecycleManager.getLastSeenTimestamp(deviceId);
                if (lastSeen == null) {
                    // 如果没有记录活跃时间，使用当前时间作为基准
                    lifecycleManager.updateLastSeen(deviceId);
                    continue;
                }

                // 计算超时时间
                long elapsed = now - lastSeen;
                if (elapsed > offlineThreshold) {
                    // 超时，触发设备离线
                    String reason = String.format("心跳超时（%d秒未收到心跳）", elapsed / 1000);
                    log.warn("{} 设备心跳超时: deviceId={}, lastSeen={}ms前, threshold={}ms",
                            LOG_PREFIX, deviceId, elapsed, offlineThreshold);

                    boolean offline = lifecycleManager.onDeviceOffline(deviceId, reason);
                    if (offline) {
                        offlineCount++;
                        log.info("{} 设备已标记为离线: deviceId={}", LOG_PREFIX, deviceId);
                    }
                }
            }

            if (checkedCount > 0 || offlineCount > 0 || skippedUpgradeCount > 0) {
                log.debug("{} 心跳检测完成: 检查设备数={}, 离线设备数={}, 跳过升级中设备数={}",
                        LOG_PREFIX, checkedCount, offlineCount, skippedUpgradeCount);
            }

        } catch (Exception e) {
            log.error("{} 心跳超时检测异常: {}", LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * 获取升级保护中的设备集合
     * <p>
     * 从 ChanghuiPlugin 获取当前处于升级保护状态的设备ID
     * </p>
     *
     * @return 升级保护设备ID集合，如果插件不可用则返回空集合
     */
    private Set<Long> getUpgradeProtectedDevices() {
        if (changhuiPlugin == null) {
            return java.util.Collections.emptySet();
        }
        try {
            return changhuiPlugin.getUpgradeProtectedDevices();
        } catch (Exception e) {
            log.warn("{} 获取升级保护设备列表失败: {}", LOG_PREFIX, e.getMessage());
            return java.util.Collections.emptySet();
        }
    }
}
