package cn.iocoder.yudao.module.iot.newgateway.plugins.nvr;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * NVR 插件配置类
 * 
 * <p>定义 NVR 设备插件的配置属性，通过 application.yaml 进行配置。</p>
 * 
 * <p>配置示例：</p>
 * <pre>
 * iot:
 *   newgateway:
 *     plugins:
 *       nvr:
 *         enabled: true
 *         keepalive-interval: 60000
 *         reconnect-interval: 30000
 *         login-timeout: 10000
 *         command-timeout: 10000
 * </pre>
 * 
 * @author IoT Gateway Team
 * @see NvrPlugin
 */
@Data
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "nvr", havingValue = "true", matchIfMissing = true)
@ConfigurationProperties(prefix = "iot.newgateway.plugins.nvr")
public class NvrConfig {

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
    private int maxConnections = 50;

    /**
     * 是否启用调试日志
     */
    private boolean debugEnabled = false;

    /**
     * 是否启用事件订阅
     */
    private boolean eventSubscriptionEnabled = true;

    /**
     * SDK 库路径
     */
    private String sdkLibPath;

    /**
     * 默认端口
     */
    private int defaultPort = 37777;

    /**
     * 是否启用录像回放功能
     */
    private boolean playbackEnabled = true;

    /**
     * 是否启用 PTZ 控制功能
     */
    private boolean ptzEnabled = true;

    /**
     * 是否启用实时预览功能
     */
    private boolean previewEnabled = true;

    /**
     * 录像下载临时目录
     */
    private String downloadTempDir = "/tmp/nvr-downloads";

    /**
     * 最大同时预览通道数
     */
    private int maxPreviewChannels = 16;
}
