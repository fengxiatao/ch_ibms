package cn.iocoder.yudao.module.iot.client;

import cn.iocoder.yudao.module.iot.config.ZlmConfig;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ZLMediaKit API 客户端
 * 
 * <p>实现与 ZLMediaKit 流媒体服务器的 RESTful API 交互</p>
 * <p>文档：https://docs.zlmediakit.com/guide/media_server/restful_api.html</p>
 *
 * @author IBMS
 */
@Component
@RequiredArgsConstructor
public class ZlmApiClient {

    private static final Logger log = LoggerFactory.getLogger(ZlmApiClient.class);

    private final ZlmConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * ZLMediaKit secret 参数（可选）。
     * 部分部署未开启 secret 校验时，可以不传；若传空字符串，ZLM 会认为缺失并返回 -300。
     */
    private String secretQueryPrefix() {
        if (StrUtil.isBlank(config.getSecret())) {
            return "";
        }
        return "secret=" + config.getSecret() + "&";
    }

    // ==================== 流代理管理 ====================

    /**
     * 添加流代理（拉流）
     * <p>从摄像头 RTSP 地址拉流到 ZLMediaKit，转换为 FLV/HLS 等格式</p>
     * <p>注意：ZLMediaKit API 使用 URL 参数格式，RTSP URL 不需要编码</p>
     *
     * @param app     应用名（如 live）
     * @param stream  流名（如 camera_123）
     * @param rtspUrl 摄像头 RTSP 地址
     * @return 流代理 key，用于删除
     */
    public String addStreamProxy(String app, String stream, String rtspUrl) {
        try {
            // 关键：ZLMediaKit 期望 url 参数是可读的 RTSP URL，但 & 会被误解析为 API 参数分隔符
            // 解决方案：只对 RTSP URL 中的 & 进行编码（变成 %26），保持其他字符不变
            // 例如：rtsp://...?channel=5&subtype=0 → rtsp://...?channel=5%26subtype=0
            String safeRtspUrl = rtspUrl.replace("&", "%26");
            
            // 手动拼接 URL
            String url = String.format(
                    "%s/index/api/addStreamProxy?%svhost=__defaultVhost__&app=%s&stream=%s&url=%s" +
                            "&enable_hls=1&enable_mp4=0&enable_rtsp=1&enable_rtmp=1&enable_ts=1&enable_fmp4=1" +
                            "&rtp_type=0&timeout_sec=15&retry_count=3",
                    config.getApiUrl(), secretQueryPrefix(), app, stream, safeRtspUrl
            );

            log.info("[ZLM] 添加流代理: app={}, stream={}, rtspUrl={}", app, stream, rtspUrl);
            log.debug("[ZLM] 请求URL: {}", url);
            
            ResponseEntity<ZlmResponse> response = restTemplate.getForEntity(url, ZlmResponse.class);

            if (response.getBody() != null && response.getBody().getCode() == 0) {
                String key = String.valueOf(response.getBody().getData().get("key"));
                log.info("[ZLM] ✅ 添加流代理成功: app={}, stream={}, key={}", app, stream, key);
                return key;
            } else if (response.getBody() != null && response.getBody().getMsg() != null 
                    && response.getBody().getMsg().contains("already exists")) {
                // 流已存在，视为成功
                log.info("[ZLM] 流已存在，无需重复添加: app={}, stream={}", app, stream);
                return "exists:" + app + "/" + stream;  // 返回特殊标识
            } else {
                log.error("[ZLM] ❌ 添加流代理失败: code={}, msg={}", 
                    response.getBody() != null ? response.getBody().getCode() : "null",
                    response.getBody() != null ? response.getBody().getMsg() : "null");
                return null;
            }
        } catch (Exception e) {
            log.error("[ZLM] 添加流代理异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除流代理
     */
    public boolean delStreamProxy(String key) {
        String url = String.format("%s/index/api/delStreamProxy?%skey=%s",
                config.getApiUrl(), secretQueryPrefix(), key);

        try {
            ResponseEntity<ZlmResponse> response = restTemplate.getForEntity(url, ZlmResponse.class);
            boolean success = response.getBody() != null && response.getBody().getCode() == 0;
            log.info("[ZLM] 删除流代理: key={}, success={}", key, success);
            return success;
        } catch (Exception e) {
            log.error("[ZLM] 删除流代理异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 关闭指定流（强制停止）
     */
    public boolean closeStream(String app, String stream) {
        String url = String.format("%s/index/api/close_streams?%sapp=%s&stream=%s&force=1",
                config.getApiUrl(), secretQueryPrefix(), app, stream);

        try {
            ResponseEntity<ZlmResponse> response = restTemplate.getForEntity(url, ZlmResponse.class);
            return response.getBody() != null && response.getBody().getCode() == 0;
        } catch (Exception e) {
            log.error("[ZLM] 关闭流异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 关闭所有流（清空所有流代理）
     * 
     * @param app 应用名，传 null 则关闭所有应用的流
     * @return 关闭的流数量
     */
    public int closeAllStreams(String app) {
        String url;
        if (app != null && !app.isEmpty()) {
            url = String.format("%s/index/api/close_streams?%sapp=%s&force=1",
                    config.getApiUrl(), secretQueryPrefix(), app);
        } else {
            url = String.format("%s/index/api/close_streams?%sforce=1",
                    config.getApiUrl(), secretQueryPrefix());
        }

        try {
            ResponseEntity<ZlmResponse> response = restTemplate.getForEntity(url, ZlmResponse.class);
            if (response.getBody() != null && response.getBody().getCode() == 0) {
                Object countObj = response.getBody().getData().get("count_closed");
                int count = countObj != null ? Integer.parseInt(countObj.toString()) : 0;
                log.info("[ZLM] ✅ 关闭所有流成功: app={}, count={}", app, count);
                return count;
            }
            log.warn("[ZLM] 关闭所有流失败: code={}, msg={}", 
                    response.getBody() != null ? response.getBody().getCode() : "null",
                    response.getBody() != null ? response.getBody().getMsg() : "null");
            return 0;
        } catch (Exception e) {
            log.error("[ZLM] 关闭所有流异常: {}", e.getMessage());
            return 0;
        }
    }

    // ==================== 流状态查询 ====================

    /**
     * 检查流是否在线
     */
    public boolean isStreamOnline(String app, String stream) {
        String url = String.format("%s/index/api/getMediaList?%sapp=%s&stream=%s",
                config.getApiUrl(), secretQueryPrefix(), app, stream);

        try {
            ResponseEntity<ZlmResponse> response = restTemplate.getForEntity(url, ZlmResponse.class);
            if (response.getBody() != null && response.getBody().getCode() == 0) {
                Object data = response.getBody().getRawData();
                if (data instanceof List) {
                    return !((List<?>) data).isEmpty();
                }
            }
            return false;
        } catch (Exception e) {
            log.debug("[ZLM] 检查流状态异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取媒体列表
     */
    public ZlmResponse getMediaList(String app, String stream) {
        String url = String.format("%s/index/api/getMediaList?%sapp=%s&stream=%s",
                config.getApiUrl(), secretQueryPrefix(), app, stream);

        try {
            return restTemplate.getForObject(url, ZlmResponse.class);
        } catch (Exception e) {
            log.error("[ZLM] 获取媒体列表异常: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 快照 ====================

    /**
     * 获取流快照（JPEG）。
     *
     * <p>调用 ZLM 的 /index/api/getSnap 接口。ZLM 行为：</p>
     * <ul>
     *   <li>若 sourceUrl 对应的流已经在 ZLM 中存活（例如 addStreamProxy 已生效），从内存帧抓取，毫秒级返回</li>
     *   <li>若流不存在，ZLM 会临时拉一次源（可能 1-3 秒）抓帧，并按 expireSec 缓存 jpeg 在磁盘</li>
     *   <li>expireSec 内重复调用直接读磁盘缓存，极快</li>
     * </ul>
     *
     * @param sourceUrl   源 URL（可填 RTSP，或填 ZLM 自身的 RTMP/FLV 地址，例如 rtmp://127.0.0.1/live/streamKey）
     * @param timeoutSec  最大抓帧等待秒数（建议 5）
     * @param expireSec   生成的 jpeg 在 ZLM 磁盘上的过期时间（建议 300）
     * @return JPEG 字节数组；失败返回 null
     */
    public byte[] getSnap(String sourceUrl, int timeoutSec, int expireSec) {
        try {
            // ZLM 的 url 参数同样可能包含 & ，需要安全编码（参考 addStreamProxy 的做法）
            String safeUrl = sourceUrl.replace("&", "%26");
            String url = String.format(
                    "%s/index/api/getSnap?%surl=%s&timeout_sec=%d&expire_sec=%d",
                    config.getApiUrl(), secretQueryPrefix(), safeUrl, timeoutSec, expireSec
            );
            log.debug("[ZLM] 抓取快照: source={}", sourceUrl);

            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            byte[] body = response.getBody();
            if (body == null || body.length == 0) {
                log.warn("[ZLM] 快照返回空: source={}", sourceUrl);
                return null;
            }
            // 当 ZLM 返回 JSON 错误（例如 secret 校验失败），body 会是 JSON 文本而不是 JPEG
            // JPEG 文件以 0xFFD8 开头
            if (!(body.length >= 2 && (body[0] & 0xFF) == 0xFF && (body[1] & 0xFF) == 0xD8)) {
                String text = new String(body, java.nio.charset.StandardCharsets.UTF_8);
                if (text.length() > 200) {
                    text = text.substring(0, 200);
                }
                log.warn("[ZLM] 快照返回非 JPEG，可能是错误响应: source={}, body={}", sourceUrl, text);
                return null;
            }
            return body;
        } catch (Exception e) {
            log.warn("[ZLM] 抓取快照异常: source={}, err={}", sourceUrl, e.getMessage());
            return null;
        }
    }

    // ==================== 录像管理 ====================

    /**
     * 开始录像
     */
    public boolean startRecord(String app, String stream, int maxSeconds) {
        String url = String.format(
            "%s/index/api/startRecord?%stype=1&vhost=__defaultVhost__&app=%s&stream=%s&max_second=%d",
            config.getApiUrl(), secretQueryPrefix(), app, stream, maxSeconds
        );

        try {
            ResponseEntity<ZlmResponse> response = restTemplate.getForEntity(url, ZlmResponse.class);
            boolean success = response.getBody() != null && response.getBody().getCode() == 0;
            log.info("[ZLM] 开始录像: app={}, stream={}, success={}", app, stream, success);
            return success;
        } catch (Exception e) {
            log.error("[ZLM] 开始录像异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 停止录像
     */
    public boolean stopRecord(String app, String stream) {
        String url = String.format(
            "%s/index/api/stopRecord?%stype=1&vhost=__defaultVhost__&app=%s&stream=%s",
            config.getApiUrl(), secretQueryPrefix(), app, stream
        );

        try {
            ResponseEntity<ZlmResponse> response = restTemplate.getForEntity(url, ZlmResponse.class);
            boolean success = response.getBody() != null && response.getBody().getCode() == 0;
            log.info("[ZLM] 停止录像: app={}, stream={}, success={}", app, stream, success);
            return success;
        } catch (Exception e) {
            log.error("[ZLM] 停止录像异常: {}", e.getMessage());
            return false;
        }
    }

    // ==================== 播放地址生成 ====================

    private String playSecretQuerySuffix() {
        if (StrUtil.isBlank(config.getSecret())) {
            return "";
        }
        return "?secret=" + java.net.URLEncoder.encode(config.getSecret(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 生成多协议播放地址
     * 
     * @param app    应用名
     * @param stream 流名
     * @return 各协议播放地址
     */
    public PlayUrlsVO buildPlayUrls(String app, String stream) {
        String serverIp = config.getServerIp();
        // 使用公网端口（如果配置了的话）
        int httpPort = config.getPublicHttpPort();
        int rtspPort = config.getRtspPort();
        int rtmpPort = config.getRtmpPort();

        String playSecret = playSecretQuerySuffix();

        PlayUrlsVO urls = new PlayUrlsVO();
        
        // WebSocket-FLV (推荐，低延迟 ~500ms)
        urls.setWsFlvUrl(String.format("ws://%s:%d/%s/%s.live.flv%s", serverIp, httpPort, app, stream, playSecret));
        
        // HTTP-FLV (低延迟 ~500ms，但有并发限制)
        urls.setFlvUrl(String.format("http://%s:%d/%s/%s.live.flv%s", serverIp, httpPort, app, stream, playSecret));
        
        // HLS (兼容性好，但延迟高 5-15秒)
        urls.setHlsUrl(String.format("http://%s:%d/%s/%s/hls.m3u8%s", serverIp, httpPort, app, stream, playSecret));
        
        // WebSocket-FMP4 (低延迟，无并发限制)
        urls.setWsFmp4Url(String.format("ws://%s:%d/%s/%s.live.mp4%s", serverIp, httpPort, app, stream, playSecret));
        
        // HTTP-TS
        urls.setTsUrl(String.format("http://%s:%d/%s/%s.live.ts%s", serverIp, httpPort, app, stream, playSecret));
        
        // RTSP (需专用播放器)
        urls.setRtspUrl(String.format("rtsp://%s:%d/%s/%s", serverIp, rtspPort, app, stream));
        
        // RTMP (需专用播放器)
        urls.setRtmpUrl(String.format("rtmp://%s:%d/%s/%s", serverIp, rtmpPort, app, stream));
        
        // WebRTC 信令走 ZLM HTTP API；RTC/UDP 公网地址由 ZLM 的 rtc.externIP/rtc.port 写入 SDP/ICE。
        // 注意：不要把 UDP 隧道域名当成 HTTP 信令 host。
        String webrtcUrl = String.format("http://%s:%d/index/api/webrtc?app=%s&stream=%s&type=play",
                serverIp, httpPort, app, stream);
        // 若开启了 secret 校验，WebRTC 信令同样需要带上 secret
        if (StrUtil.isNotBlank(config.getSecret())) {
            webrtcUrl += "&secret=" + java.net.URLEncoder.encode(config.getSecret(), java.nio.charset.StandardCharsets.UTF_8);
        }
        urls.setWebRtcUrl(webrtcUrl);

        urls.setStreamKey(stream);
        
        return urls;
    }

    // ==================== 内部类 ====================

    @Data
    public static class ZlmResponse {
        private Integer code;
        private String msg;
        private Object data;

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        /**
         * 获取原始 data（可能是 Map、List 或其他类型）
         */
        public Object getRawData() {
            return data;
        }

        /**
         * 获取 data 作为 Map（用于获取单个对象的属性）
         */
        @SuppressWarnings("unchecked")
        public Map<String, Object> getData() {
            if (data instanceof Map) {
                return (Map<String, Object>) data;
            }
            return new HashMap<>();
        }
    }

    @Data
    public static class PlayUrlsVO {
        /** WebSocket-FLV 地址（推荐，低延迟） */
        private String wsFlvUrl;
        /** HTTP-FLV 地址 */
        private String flvUrl;
        /** HLS 地址（高兼容性） */
        private String hlsUrl;
        /** WebSocket-FMP4 地址 */
        private String wsFmp4Url;
        /** HTTP-TS 地址 */
        private String tsUrl;
        /** RTSP 地址 */
        private String rtspUrl;
        /** RTMP 地址 */
        private String rtmpUrl;
        /** WebRTC 地址（极低延迟） */
        private String webRtcUrl;
        /** 流标识 */
        private String streamKey;

        public void setWsFlvUrl(String wsFlvUrl) {
            this.wsFlvUrl = wsFlvUrl;
        }

        public void setFlvUrl(String flvUrl) {
            this.flvUrl = flvUrl;
        }

        public void setHlsUrl(String hlsUrl) {
            this.hlsUrl = hlsUrl;
        }

        public void setWsFmp4Url(String wsFmp4Url) {
            this.wsFmp4Url = wsFmp4Url;
        }

        public void setTsUrl(String tsUrl) {
            this.tsUrl = tsUrl;
        }

        public void setRtspUrl(String rtspUrl) {
            this.rtspUrl = rtspUrl;
        }

        public void setRtmpUrl(String rtmpUrl) {
            this.rtmpUrl = rtmpUrl;
        }

        public void setWebRtcUrl(String webRtcUrl) {
            this.webRtcUrl = webRtcUrl;
        }

        public void setStreamKey(String streamKey) {
            this.streamKey = streamKey;
        }
    }
}
