package cn.iocoder.yudao.module.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ZLMediaKit 流媒体服务配置
 *
 * @author IBMS
 */
@Data
@Component
@ConfigurationProperties(prefix = "iot.video.zlmedia")
public class ZlmConfig {

    /**
     * ZLMediaKit API 地址
     * 例如：http://192.168.1.246:8080
     */
    private String apiUrl = "http://192.168.1.246:8080";

    /**
     * ZLMediaKit API 密钥
     */
    private String secret = "";

    /**
     * HTTP/FLV/HLS 端口
     */
    private Integer httpPort = 8080;

    /**
     * RTSP 端口
     */
    private Integer rtspPort = 8554;

    /**
     * RTMP 端口
     */
    private Integer rtmpPort = 1935;

    /**
     * WebRTC UDP 端口
     */
    private Integer rtcPort = 8000;

    /**
     * 公网访问地址（域名或 IP）
     * 如果配置了此项，播放地址会使用公网地址，而非内网 IP
     * 例如：ibms.gzchanghui.cn
     */
    private String publicHost = "";

    /**
     * 公网 HTTP 端口（默认与 httpPort 相同）
     * 如果通过 Nginx 反向代理，可能需要单独配置
     */
    private Integer publicHttpPort;

    /**
     * WebRTC 公网 UDP 端口（通过 natapp UDP 隧道映射）
     * 例如：48088
     */
    private Integer publicRtcPort;

    /**
     * WebRTC 公网 IP/域名（natapp 分配的）
     * 例如：39.108.87.226 或 94a29c966e617365.natapp.cc
     */
    private String publicRtcHost = "";

    /**
     * 获取流媒体服务器 IP（用于生成播放地址）
     * 优先使用 publicHost，否则从 apiUrl 解析
     */
    public String getServerIp() {
        // 优先使用公网地址
        if (publicHost != null && !publicHost.isEmpty()) {
            return publicHost;
        }
        // 从 apiUrl 解析
        if (apiUrl == null) return "127.0.0.1";
        try {
            return new java.net.URL(apiUrl).getHost();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    /**
     * 获取公网 HTTP 端口
     */
    public Integer getPublicHttpPort() {
        if (publicHttpPort != null && publicHttpPort > 0) {
            return publicHttpPort;
        }
        return httpPort;
    }

    /**
     * 获取 WebRTC 公网端口
     */
    public Integer getPublicRtcPort() {
        if (publicRtcPort != null && publicRtcPort > 0) {
            return publicRtcPort;
        }
        return rtcPort;
    }

    /**
     * 获取 WebRTC 公网 IP/域名
     * 优先使用 publicRtcHost，否则使用 publicHost，最后从 apiUrl 解析
     */
    public String getRtcServerIp() {
        if (publicRtcHost != null && !publicRtcHost.isEmpty()) {
            return publicRtcHost;
        }
        return getServerIp();
    }
}
