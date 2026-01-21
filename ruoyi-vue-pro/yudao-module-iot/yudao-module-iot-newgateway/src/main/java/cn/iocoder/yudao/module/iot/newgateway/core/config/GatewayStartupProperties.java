package cn.iocoder.yudao.module.iot.newgateway.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关启动初始化配置属性
 * <p>
 * 控制 newgateway 启动时设备初始化的行为，包括：
 * - 是否启用自动初始化
 * - 启动延迟时间
 * - 并行度和超时设置
 * - 重试策略
 * - 设备类型过滤
 * - 租户ID配置（单租户本地化部署）
 * </p>
 *
 * <p>配置示例：</p>
 * <pre>
 * iot:
 *   gateway:
 *     startup:
 *       enabled: true
 *       tenant-id: 1              # 单租户部署时必填，指定当前租户ID
 *       delay-seconds: 10
 *       parallelism: 10
 *       device-timeout-seconds: 60
 *       batch-size: 50
 *       device-interval-ms: 100
 *       max-retry-count: 3
 *       initial-retry-interval-seconds: 10
 *       sync-state-to-db: true
 *       device-type-whitelist: []
 *       device-type-blacklist: []
 * </pre>
 *
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.core.startup.GatewayStartupInitializer
 */
@Data
@Validated
@ConfigurationProperties(prefix = "iot.gateway.startup")
public class GatewayStartupProperties {

    /**
     * 是否启用启动时初始化
     * <p>
     * 设置为 false 可以禁用自动初始化功能，适用于调试或特殊场景。
     * </p>
     */
    private boolean enabled = true;

    /**
     * 租户ID
     * <p>
     * 单租户本地化部署时，指定当前 Gateway 实例所属的租户ID。
     * 启动时只会获取和初始化该租户的设备。
     * </p>
     * <p>
     * 如果未设置（null），则获取所有设备（向后兼容 SaaS 多租户场景）。
     * </p>
     * <p>
     * <b>单租户部署时必须配置此项。</b>
     * </p>
     */
    private Long tenantId;

    /**
     * 启动延迟时间（秒）
     * <p>
     * 应用启动完成后，等待指定时间再开始初始化设备。
     * 这确保所有依赖服务（如消息总线、数据库连接）都已就绪。
     * </p>
     */
    private int delaySeconds = 10;

    /**
     * 并行度（线程池大小）
     * <p>
     * 控制同时初始化设备的最大数量。
     * 建议根据服务器性能和设备数量调整。
     * </p>
     */
    private int parallelism = 10;

    /**
     * 单设备初始化超时（秒）
     * <p>
     * 单个设备初始化（如 SDK 登录）的最大等待时间。
     * 超时后设备将被标记为初始化失败并加入重试队列。
     * </p>
     */
    private int deviceTimeoutSeconds = 60;

    /**
     * 批次大小
     * <p>
     * 每批次处理的设备数量。
     * 用于控制内存使用和避免同时发起过多连接。
     * </p>
     */
    private int batchSize = 50;

    /**
     * 设备间隔时间（毫秒）
     * <p>
     * 每个设备初始化之间的间隔时间。
     * 用于避免对设备造成连接风暴。
     * </p>
     */
    private int deviceIntervalMs = 100;

    /**
     * 最大重试次数
     * <p>
     * 设备初始化失败后的最大重试次数。
     * 达到最大次数后，设备将被标记为永久失败。
     * </p>
     */
    private int maxRetryCount = 3;

    /**
     * 初始重试间隔（秒）
     * <p>
     * 第一次重试的等待时间。
     * 后续重试使用指数退避：initialRetryIntervalSeconds * 2^retryCount
     * 例如：10s, 20s, 40s
     * </p>
     */
    private int initialRetryIntervalSeconds = 10;

    /**
     * 设备类型白名单
     * <p>
     * 如果非空，只有在此列表中的设备类型才会被初始化。
     * 为空则不过滤（初始化所有设备类型）。
     * </p>
     * <p>
     * 示例值：["NVR", "ACCESS_GEN1", "ACCESS_GEN2"]
     * </p>
     */
    private List<String> deviceTypeWhitelist = new ArrayList<>();

    /**
     * 设备类型黑名单
     * <p>
     * 在此列表中的设备类型将被跳过，不进行初始化。
     * 黑名单优先级高于白名单。
     * </p>
     * <p>
     * 示例值：["ALARM", "CHANGHUI"]
     * </p>
     */
    private List<String> deviceTypeBlacklist = new ArrayList<>();

    /**
     * 是否同步状态到数据库
     * <p>
     * 设置为 true 时，设备初始化结果会通过消息总线发布，
     * biz 层会更新数据库中的设备状态。
     * </p>
     */
    private boolean syncStateToDb = true;

    /**
     * 检查设备类型是否应该被初始化
     *
     * @param deviceType 设备类型
     * @return true 如果应该初始化，false 如果应该跳过
     */
    public boolean shouldInitializeDeviceType(String deviceType) {
        if (deviceType == null || deviceType.isEmpty()) {
            return false;
        }
        
        // 黑名单优先级最高
        if (!deviceTypeBlacklist.isEmpty() && deviceTypeBlacklist.contains(deviceType)) {
            return false;
        }
        
        // 如果白名单为空，则允许所有（除了黑名单中的）
        if (deviceTypeWhitelist.isEmpty()) {
            return true;
        }
        
        // 白名单非空时，只允许白名单中的设备类型
        return deviceTypeWhitelist.contains(deviceType);
    }

    /**
     * 计算指定重试次数的退避时间（毫秒）
     *
     * @param retryCount 当前重试次数（从 0 开始）
     * @return 退避时间（毫秒）
     */
    public long calculateBackoffMs(int retryCount) {
        // 指数退避：initialRetryIntervalSeconds * 2^retryCount
        long backoffSeconds = initialRetryIntervalSeconds * (1L << retryCount);
        return backoffSeconds * 1000;
    }

    /**
     * 生成配置摘要（用于日志输出）
     *
     * @return 配置摘要字符串
     */
    public String toSummary() {
        return String.format(
            "GatewayStartupProperties{enabled=%s, tenantId=%s, delaySeconds=%d, parallelism=%d, " +
            "deviceTimeoutSeconds=%d, batchSize=%d, maxRetryCount=%d, " +
            "whitelist=%s, blacklist=%s, syncStateToDb=%s}",
            enabled, tenantId != null ? tenantId : "ALL_TENANTS", delaySeconds, parallelism, 
            deviceTimeoutSeconds, batchSize, maxRetryCount, 
            deviceTypeWhitelist.isEmpty() ? "ALL" : deviceTypeWhitelist,
            deviceTypeBlacklist.isEmpty() ? "NONE" : deviceTypeBlacklist,
            syncStateToDb
        );
    }
}
