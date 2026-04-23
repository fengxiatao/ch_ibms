package cn.iocoder.yudao.module.iot.newgateway.plugins.template;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 模板插件配置类
 * 
 * <p>定义插件的配置属性，通过 application.yaml 进行配置。</p>
 * 
 * <p>配置示例：</p>
 * <pre>
 * iot:
 *   newgateway:
 *     plugins:
 *       template:
 *         enabled: true
 *         port: 9800
 *         heartbeat-timeout: 60000
 *         connection-timeout: 30000
 * </pre>
 * 
 * <p>开发新插件时，请修改：</p>
 * <ul>
 *   <li>类名：将 TemplateConfig 改为 {YourDevice}Config</li>
 *   <li>配置前缀：将 template 改为你的设备类型（小写）</li>
 *   <li>添加设备特定的配置属性</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see TemplatePlugin
 */
@Data
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "template", havingValue = "true")
@ConfigurationProperties(prefix = "iot.newgateway.plugins.template")
public class TemplateConfig {

    /**
     * 是否启用插件
     * <p>
     * 设置为 false 可以禁用此插件，插件将不会被加载和初始化。
     * </p>
     */
    private boolean enabled = false;

    /**
     * TCP 监听端口
     * <p>
     * 被动连接设备需要监听的端口号。
     * 每个插件应使用不同的端口，避免冲突。
     * </p>
     */
    private int port = 9800;

    /**
     * 心跳超时时间（毫秒）
     * <p>
     * 如果超过此时间未收到设备心跳，则认为设备离线。
     * 建议设置为心跳间隔的 2-3 倍。
     * </p>
     */
    private long heartbeatTimeout = 60000;

    /**
     * 连接超时时间（毫秒）
     * <p>
     * 设备连接后，如果超过此时间未发送有效数据，则断开连接。
     * </p>
     */
    private long connectionTimeout = 30000;

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

    // ==================== 以下为示例配置，开发新插件时可根据需要添加 ====================

    /**
     * 示例：设备特定的配置参数
     * <p>
     * 根据设备协议需要，添加相应的配置属性。
     * 例如：协议版本、加密密钥、重试次数等。
     * </p>
     */
    private String protocolVersion = "1.0";

    /**
     * 示例：重试次数
     */
    private int retryCount = 3;

    /**
     * 示例：重试间隔（毫秒）
     */
    private long retryInterval = 5000;
}
