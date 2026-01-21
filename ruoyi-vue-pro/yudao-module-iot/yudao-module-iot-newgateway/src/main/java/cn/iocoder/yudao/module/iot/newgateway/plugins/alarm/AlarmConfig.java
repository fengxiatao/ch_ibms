package cn.iocoder.yudao.module.iot.newgateway.plugins.alarm;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 报警主机插件配置类
 * 
 * <p>定义报警主机设备插件的配置属性，通过 application.yaml 进行配置。</p>
 * 
 * <p>配置示例：</p>
 * <pre>
 * iot:
 *   newgateway:
 *     plugins:
 *       alarm:
 *         enabled: true
 *         port: 9500
 *         heartbeat-timeout: 60000
 *         connection-timeout: 30000
 *         default-password: "1234"
 * </pre>
 * 
 * @author IoT Gateway Team
 * @see AlarmPlugin
 */
@Data
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "alarm", havingValue = "true", matchIfMissing = true)
@ConfigurationProperties(prefix = "iot.newgateway.plugins.alarm")
public class AlarmConfig {

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
     * 报警主机设备连接的端口号，默认 9500。
     * </p>
     */
    private int port = 9500;

    /**
     * 心跳超时时间（毫秒）
     * <p>
     * 如果超过此时间未收到设备心跳，则认为设备离线。
     * 默认 60 秒。
     * </p>
     */
    private long heartbeatTimeout = 60000;

    /**
     * 连接超时时间（毫秒）
     * <p>
     * 设备连接后，如果超过此时间未发送有效数据，则断开连接。
     * 默认 30 秒。
     * </p>
     */
    private long connectionTimeout = 30000;

    /**
     * 默认密码
     * <p>
     * 当设备未配置密码时使用的默认密码。
     * </p>
     */
    private String defaultPassword = "1234";

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
     * 命令超时时间（毫秒）
     * <p>
     * 等待设备响应命令的超时时间。
     * 默认 10 秒。
     * </p>
     */
    private long commandTimeout = 10000;

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
     * </p>
     */
    private long retryInterval = 3000;
}
