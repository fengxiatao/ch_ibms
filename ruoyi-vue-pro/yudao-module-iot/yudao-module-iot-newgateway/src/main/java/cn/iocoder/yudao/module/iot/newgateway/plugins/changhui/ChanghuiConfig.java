package cn.iocoder.yudao.module.iot.newgateway.plugins.changhui;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 长辉TCP插件配置类
 * 
 * <p>定义长辉设备插件的配置属性，通过 application.yaml 进行配置。</p>
 * 
 * <p>配置示例：</p>
 * <pre>
 * iot:
 *   newgateway:
 *     plugins:
 *       changhui:
 *         enabled: true
 *         port: 9700
 *         heartbeat-timeout: 60000
 *         connection-timeout: 30000
 *         upgrade-timeout: 300000
 * </pre>
 * 
 * @author IoT Gateway Team
 * @see ChanghuiPlugin
 */
@Data
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "changhui", havingValue = "true", matchIfMissing = true)
@ConfigurationProperties(prefix = "iot.newgateway.plugins.changhui")
public class ChanghuiConfig {

    /**
     * 是否启用插件
     * <p>
     * 设置为 false 可以禁用此插件，插件将不会被加载和初始化。
     * </p>
     */
    private boolean enabled = true;

    /**
     * TCP 监听端口
     * <p>
     * 长辉设备连接的端口号，默认 9700。
     * </p>
     */
    private int port = 9700;

    /**
     * 心跳超时时间（毫秒）
     * <p>
     * 如果超过此时间未收到设备心跳，则认为设备离线。
     * 
     * 对于4G网络（1分钟心跳间隔）：
     *   建议设置为 心跳间隔 × 2 = 120秒
     * </p>
     */
    private long heartbeatTimeout = 120000;

    /**
     * 连接空闲超时时间（毫秒）
     * <p>
     * 设备连接后，如果超过此时间未发送任何数据，则断开连接。
     * 
     * 对于4G网络（1分钟心跳间隔）：
     *   建议设置为 离线阈值 + 30秒缓冲 = 270秒（4.5分钟）
     *   应大于离线阈值，给网关侧处理离线逻辑留出时间。
     * </p>
     */
    private long connectionTimeout = 270000;

    /**
     * 升级超时时间（毫秒）
     * <p>
     * 设备升级操作的超时时间。
     * 默认 5 分钟（300000 毫秒）。
     * </p>
     */
    private long upgradeTimeout = 300000;

    /**
     * 最大连接数
     * <p>
     * 限制同时连接的设备数量，防止资源耗尽。
     * 设置为 0 表示不限制。
     * </p>
     */
    private int maxConnections = 1000;

    /**
     * 是否启用调试日志
     * <p>
     * 启用后会输出详细的协议解析和数据处理日志。
     * 生产环境建议关闭。
     * </p>
     */
    private boolean debugEnabled = false;

    /**
     * 协议版本
     * <p>
     * 长辉协议版本号。
     * </p>
     */
    private String protocolVersion = "1.0";

    /**
     * 命令重试次数
     * <p>
     * 命令发送失败时的重试次数。
     * </p>
     */
    private int retryCount = 3;

    /**
     * 命令重试间隔（毫秒）
     * <p>
     * 命令重试之间的等待时间。
     * 4G网络建议设置较长间隔，给网络恢复留出时间。
     * 默认 10 秒。
     * </p>
     */
    private long retryInterval = 10000;

    // ==================== 4G网络优化配置 ====================

    /**
     * 是否启用4G网络优化
     * <p>
     * 启用后会使用更短的心跳间隔和更长的命令超时时间，
     * 以应对运营商NAT超时和网络不稳定问题。
     * </p>
     */
    private boolean enable4GOptimization = true;

    /**
     * 4G网络命令超时时间（毫秒）
     * <p>
     * 4G网络延迟较大，建议设置较长的超时时间。
     * 默认 45 秒，考虑到4G高延迟场景。
     * </p>
     */
    private long command4GTimeout = 45000;

    /**
     * 固件下载超时时间（毫秒）
     * <p>
     * 4G网络下载固件速度较慢，设置较长超时时间。
     * 默认 10 分钟（600000 毫秒）。
     * </p>
     */
    private long firmwareDownloadTimeout = 600000;

    /**
     * 升级状态无更新超时时间（毫秒）
     * <p>
     * 如果升级状态超过此时间无更新，会主动查询或重试。
     * 默认 60 秒。
     * </p>
     */
    private long upgradeStatusTimeout = 60000;

    /**
     * 升级期间心跳保护时间（毫秒）
     * <p>
     * 升级开始后，在此时间内不进行心跳超时检测，防止设备被误判离线。
     * 默认 10 分钟（600000 毫秒），应与固件下载超时保持一致。
     * </p>
     */
    private long upgradeHeartbeatProtectionTime = 600000;
}
