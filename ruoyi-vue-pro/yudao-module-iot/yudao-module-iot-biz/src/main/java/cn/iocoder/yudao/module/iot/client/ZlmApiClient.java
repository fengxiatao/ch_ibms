package cn.iocoder.yudao.module.iot.client;

import cn.iocoder.yudao.module.iot.config.ZlmConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
@RequiredArgsConstructor
public class ZlmApiClient {

    private final ZlmConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

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
                "%s/index/api/addStreamProxy?secret=%s&vhost=__defaultVhost__&app=%s&stream=%s&url=%s" +
                "&enable_hls=1&enable_mp4=0&enable_rtsp=1&enable_rtmp=1&enable_ts=1&enable_fmp4=1" +
                "&rtp_type=0&timeout_sec=15&retry_count=3",
                config.getApiUrl(), config.getSecret(), app, stream, safeRtspUrl
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
        String url = String.format("%s/index/api/delStreamProxy?secret=%s&key=%s",
                config.getApiUrl(), config.getSecret(), key);

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
        String url = String.format("%s/index/api/close_streams?secret=%s&app=%s&stream=%s&force=1",
                config.getApiUrl(), config.getSecret(), app, stream);

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
            url = String.format("%s/index/api/close_streams?secret=%s&app=%s&force=1",
                    config.getApiUrl(), config.getSecret(), app);
        } else {
            url = String.format("%s/index/api/close_streams?secret=%s&force=1",
                    config.getApiUrl(), config.getSecret());
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
        String url = String.format("%s/index/api/getMediaList?secret=%s&app=%s&stream=%s",
                config.getApiUrl(), config.getSecret(), app, stream);

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
        String url = String.format("%s/index/api/getMediaList?secret=%s&app=%s&stream=%s",
                config.getApiUrl(), config.getSecret(), app, stream);

        try {
            return restTemplate.getForObject(url, ZlmResponse.class);
        } catch (Exception e) {
            log.error("[ZLM] 获取媒体列表异常: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 录像管理 ====================

    /**
     * 开始录像
     */
    public boolean startRecord(String app, String stream, int maxSeconds) {
        String url = String.format(
            "%s/index/api/startRecord?secret=%s&type=1&vhost=__defaultVhost__&app=%s&stream=%s&max_second=%d",
            config.getApiUrl(), config.getSecret(), app, stream, maxSeconds
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
            "%s/index/api/stopRecord?secret=%s&type=1&vhost=__defaultVhost__&app=%s&stream=%s",
            config.getApiUrl(), config.getSecret(), app, stream
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

        PlayUrlsVO urls = new PlayUrlsVO();
        
        // WebSocket-FLV (推荐，低延迟 ~500ms)
        urls.setWsFlvUrl(String.format("ws://%s:%d/%s/%s.live.flv", serverIp, httpPort, app, stream));
        
        // HTTP-FLV (低延迟 ~500ms，但有并发限制)
        urls.setFlvUrl(String.format("http://%s:%d/%s/%s.live.flv", serverIp, httpPort, app, stream));
        
        // HLS (兼容性好，但延迟高 5-15秒)
        urls.setHlsUrl(String.format("http://%s:%d/%s/%s/hls.m3u8", serverIp, httpPort, app, stream));
        
        // WebSocket-FMP4 (低延迟，无并发限制)
        urls.setWsFmp4Url(String.format("ws://%s:%d/%s/%s.live.mp4", serverIp, httpPort, app, stream));
        
        // HTTP-TS
        urls.setTsUrl(String.format("http://%s:%d/%s/%s.live.ts", serverIp, httpPort, app, stream));
        
        // RTSP (需专用播放器)
        urls.setRtspUrl(String.format("rtsp://%s:%d/%s/%s", serverIp, rtspPort, app, stream));
        
        // RTMP (需专用播放器)
        urls.setRtmpUrl(String.format("rtmp://%s:%d/%s/%s", serverIp, rtmpPort, app, stream));
        
        // WebRTC (极低延迟 ~200ms)
        urls.setWebRtcUrl(String.format("http://%s:%d/index/api/webrtc?app=%s&stream=%s&type=play", 
                serverIp, httpPort, app, stream));

        urls.setStreamKey(stream);
        
        return urls;
    }

    // ==================== 内部类 ====================

    @Data
    public static class ZlmResponse {
        private Integer code;
        private String msg;
        private Object data;

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
    }
}
