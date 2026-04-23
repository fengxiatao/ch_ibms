package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 门禁二代插件配置类
 * 
 * <p>定义门禁二代设备插件的配置属性，通过 application.yaml 进行配置。</p>
 * 
 * <p>配置示例：</p>
 * <pre>
 * iot:
 *   newgateway:
 *     plugins:
 *       access-gen2:
 *         enabled: true
 *         keepalive-interval: 60000
 *         reconnect-interval: 30000
 *         login-timeout: 10000
 *         command-timeout: 10000
 *         face-download-enabled: true
 *         fingerprint-download-enabled: true
 * </pre>
 * 
 * @author IoT Gateway Team
 * @see AccessGen2Plugin
 */
@Data
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "access-gen2", havingValue = "true", matchIfMissing = true)
@ConfigurationProperties(prefix = "iot.newgateway.plugins.access-gen2")
public class AccessGen2Config {

    /**
     * 是否启用插件
     * <p>
     * 设置为 false 可以禁用此插件，插件将不会被加载和初始化。
     * </p>
     */
    private boolean enabled = true;

    /**
     * 保活检测间隔（毫秒）
     * <p>
     * 多长时间执行一次保活检测，检测设备连接是否仍然有效。
     * 默认 60 秒。
     * </p>
     */
    private long keepaliveInterval = 60000;

    /**
     * 重连间隔（毫秒）
     * <p>
     * 当连接断开时，等待多长时间后尝试重连。
     * 默认 30 秒。
     * </p>
     */
    private long reconnectInterval = 30000;


    /**
     * 登录超时时间（毫秒）
     * <p>
     * SDK 登录设备的超时时间。
     * 默认 10 秒。
     * </p>
     */
    private long loginTimeout = 10000;

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

    /**
     * 最大连接数
     * <p>
     * 限制同时连接的设备数量，防止资源耗尽。
     * 设置为 0 表示不限制。
     * </p>
     */
    private int maxConnections = 100;

    /**
     * 是否启用调试日志
     * <p>
     * 启用后会输出详细的 SDK 调用和数据处理日志。
     * 生产环境建议关闭。
     * </p>
     */
    private boolean debugEnabled = false;

    /**
     * 是否启用事件订阅
     * <p>
     * 启用后会订阅设备的门禁事件回调。
     * </p>
     */
    private boolean eventSubscriptionEnabled = true;

    /**
     * 是否启用人脸下发功能
     * <p>
     * 门禁二代设备支持人脸识别，启用后可以下发人脸数据。
     * </p>
     */
    private boolean faceDownloadEnabled = true;

    /**
     * 是否启用指纹下发功能
     * <p>
     * 门禁二代设备支持指纹识别，启用后可以下发指纹数据。
     * </p>
     */
    private boolean fingerprintDownloadEnabled = true;

    /**
     * 人脸图片最大大小（字节）
     * <p>
     * 限制下发的人脸图片大小，防止传输超时。
     * 默认 200KB。
     * </p>
     */
    private int maxFaceImageSize = 200 * 1024;

    /**
     * SDK 库路径
     * <p>
     * 大华 NetSDK 库文件所在目录。
     * 如果为空，则使用系统默认路径。
     * </p>
     */
    private String sdkLibPath;

    /**
     * 默认端口
     * <p>
     * 设备连接的默认端口号。
     * </p>
     */
    private int defaultPort = 37777;
}
