package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * IPC 插件配置类
 * 
 * <p>定义 IPC 设备插件的配置属性，通过 application.yaml 进行配置。</p>
 * 
 * <p>配置示例：</p>
 * <pre>
 * iot:
 *   newgateway:
 *     plugins:
 *       ipc:
 *         enabled: true
 *         keepalive-interval: 60000
 *         reconnect-interval: 30000
 *         default-port: 37777
 * </pre>
 * 
 * @author IoT Gateway Team
 * @see IpcPlugin
 */
@Data
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "ipc", havingValue = "true", matchIfMissing = false)
@ConfigurationProperties(prefix = "iot.newgateway.plugins.ipc")
public class IpcConfig {

    /**
     * 是否启用插件
     */
    private boolean enabled = true;

    /**
     * 保活检测间隔（毫秒）
     */
    private long keepaliveInterval = 60000;

    /**
     * 重连间隔（毫秒）
     */
    private long reconnectInterval = 30000;

    /**
     * 登录超时时间（毫秒）
     */
    private long loginTimeout = 10000;

    /**
     * 命令超时时间（毫秒）
     */
    private long commandTimeout = 10000;

    /**
     * 命令重试次数
     */
    private int retryCount = 3;

    /**
     * 命令重试间隔（毫秒）
     */
    private long retryInterval = 3000;

    /**
     * 最大连接数
     */
    private int maxConnections = 100;

    /**
     * 是否启用调试日志
     */
    private boolean debugEnabled = false;

    /**
     * 是否启用事件订阅（移动侦测、遮挡报警等）
     */
    private boolean eventSubscriptionEnabled = true;

    /**
     * SDK 库路径
     */
    private String sdkLibPath;

    /**
     * 默认端口（大华设备默认 37777）
     */
    private int defaultPort = 37777;

    /**
     * RTSP 端口
     */
    private int rtspPort = 554;

    /**
     * 是否启用 PTZ 控制功能
     */
    private boolean ptzEnabled = true;

    /**
     * 是否启用实时预览功能
     */
    private boolean previewEnabled = true;

    /**
     * 是否启用抓图功能
     */
    private boolean captureEnabled = true;

    /**
     * 是否启用 AI 事件订阅（人脸检测、车牌识别等）
     */
    private boolean aiEventEnabled = false;

    /**
     * 抓图临时目录
     */
    private String captureTempDir = "/tmp/ipc-captures";

    /**
     * 默认用户名
     */
    private String defaultUsername = "admin";

    /**
     * 默认密码
     */
    private String defaultPassword = "admin123";
}
