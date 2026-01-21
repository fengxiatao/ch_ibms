package cn.iocoder.yudao.module.iot.controller.admin.video;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.service.video.ZlmStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ZLMediaKit Hook 回调控制器
 * 
 * <p>处理 ZLMediaKit 的 Web Hook 回调事件：</p>
 * <ul>
 *     <li>on_stream_not_found: 流未找到时触发，用于按需拉流</li>
 *     <li>on_stream_none_reader: 无人观看时触发，用于释放资源</li>
 *     <li>on_stream_changed: 流注册/注销时触发</li>
 *     <li>on_play: 播放请求时触发，用于鉴权</li>
 * </ul>
 *
 * @author IBMS
 */
@Tag(name = "ZLMediaKit Hook 回调")
@RestController
@RequestMapping("/iot/video/zlm/hook")
@Validated
@Slf4j
public class ZlmHookController {

    @Resource
    private ZlmStreamService zlmStreamService;

    /**
     * 流未找到回调 - 按需拉流
     * 
     * <p>当客户端请求播放的流不存在时，ZLMediaKit 会调用此接口。</p>
     * <p>我们利用这个机制实现"先播后推"：</p>
     * <ol>
     *     <li>前端请求播放 ws://xxx/live/channel_123.live.flv</li>
     *     <li>ZLMediaKit 发现流不存在，调用此 hook</li>
     *     <li>后端收到回调，从摄像头拉取 RTSP 流</li>
     *     <li>ZLMediaKit 等待流注册成功后返回给客户端</li>
     * </ol>
     */
    @PostMapping("/on_stream_not_found")
    @Operation(summary = "流未找到回调")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> onStreamNotFound(@RequestBody ZlmHookVO hookVO) {
        log.info("[ZLM Hook] on_stream_not_found: app={}, stream={}, ip={}", 
                hookVO.getApp(), hookVO.getStream(), hookVO.getIp());

        try {
            zlmStreamService.handleStreamNotFound(hookVO.getApp(), hookVO.getStream());
        } catch (Exception e) {
            log.error("[ZLM Hook] 处理 stream_not_found 失败", e);
        }

        return successResponse();
    }

    /**
     * 无人观看回调 - 释放资源
     * 
     * <p>当流的观看者数量降为 0 时触发，用于释放拉流代理资源。</p>
     * <p>返回 close=true 表示关闭该流。</p>
     */
    @PostMapping("/on_stream_none_reader")
    @Operation(summary = "无人观看回调")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> onStreamNoneReader(@RequestBody ZlmHookVO hookVO) {
        log.info("[ZLM Hook] on_stream_none_reader: app={}, stream={}", 
                hookVO.getApp(), hookVO.getStream());

        try {
            zlmStreamService.handleStreamNoneReader(hookVO.getApp(), hookVO.getStream());
        } catch (Exception e) {
            log.error("[ZLM Hook] 处理 stream_none_reader 失败", e);
        }

        // 返回 close=true 让 ZLMediaKit 关闭流
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("close", true);
        return response;
    }

    /**
     * 流状态变化回调
     * 
     * <p>流注册（regist=true）或注销（regist=false）时触发</p>
     */
    @PostMapping("/on_stream_changed")
    @Operation(summary = "流状态变化回调")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> onStreamChanged(@RequestBody ZlmHookVO hookVO) {
        log.info("[ZLM Hook] on_stream_changed: app={}, stream={}, regist={}, schema={}", 
                hookVO.getApp(), hookVO.getStream(), hookVO.getRegist(), hookVO.getSchema());
        
        // 可在此发送 WebSocket 通知前端流状态变化
        
        return successResponse();
    }

    /**
     * 播放鉴权回调
     * 
     * <p>每次客户端请求播放时触发，可用于权限校验</p>
     */
    @PostMapping("/on_play")
    @Operation(summary = "播放鉴权回调")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> onPlay(@RequestBody ZlmHookVO hookVO) {
        log.debug("[ZLM Hook] on_play: app={}, stream={}, ip={}, params={}", 
                hookVO.getApp(), hookVO.getStream(), hookVO.getIp(), hookVO.getParams());
        
        // TODO: 可在此进行播放权限校验
        // 例如：验证 params 中的 token
        
        return successResponse();
    }

    /**
     * 推流鉴权回调
     */
    @PostMapping("/on_publish")
    @Operation(summary = "推流鉴权回调")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> onPublish(@RequestBody ZlmHookVO hookVO) {
        log.info("[ZLM Hook] on_publish: app={}, stream={}, ip={}", 
                hookVO.getApp(), hookVO.getStream(), hookVO.getIp());
        
        return successResponse();
    }

    /**
     * 服务器启动回调
     */
    @PostMapping("/on_server_started")
    @Operation(summary = "服务器启动回调")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> onServerStarted(@RequestBody Map<String, Object> body) {
        log.info("[ZLM Hook] on_server_started: mediaServerId={}", body.get("mediaServerId"));
        return successResponse();
    }

    /**
     * 服务器保活回调
     */
    @PostMapping("/on_server_keepalive")
    @Operation(summary = "服务器保活回调")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> onServerKeepalive(@RequestBody Map<String, Object> body) {
        log.debug("[ZLM Hook] on_server_keepalive");
        return successResponse();
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> successResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        return response;
    }

    // ==================== VO ====================

    @Data
    public static class ZlmHookVO {
        /** 流媒体服务器ID */
        private String mediaServerId;
        /** 应用名 */
        private String app;
        /** 流名 */
        private String stream;
        /** 虚拟主机 */
        private String vhost;
        /** 协议类型 (rtsp/rtmp/hls/http-flv...) */
        private String schema;
        /** 客户端IP */
        private String ip;
        /** 客户端端口 */
        private Integer port;
        /** 注册/注销标志 */
        private Boolean regist;
        /** URL 参数 */
        private String params;
    }
}
