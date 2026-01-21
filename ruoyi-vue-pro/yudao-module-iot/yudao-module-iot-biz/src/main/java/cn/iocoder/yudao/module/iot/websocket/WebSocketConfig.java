package cn.iocoder.yudao.module.iot.websocket;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置类
 *
 * 注册 IoT WebSocket 端点
 * 
 * <p>所有 WebSocket 端点统一使用 /ws/iot 前缀，便于：</p>
 * <ul>
 *   <li>统一的安全配置（只需放行 /ws/iot/**）</li>
 *   <li>统一的代理配置</li>
 *   <li>清晰的 API 结构</li>
 * </ul>
 * 
 * <p>端点列表：</p>
 * <ul>
 *   <li>/ws/iot - IoT 通用端点</li>
 *   <li>/ws/iot/device/status - 设备状态推送</li>
 *   <li>/ws/iot/access/device/status - 门禁设备状态推送</li>
 *   <li>/ws/iot/access/event - 门禁事件推送</li>
 *   <li>/ws/iot/access/auth-task/progress - 授权任务进度推送</li>
 *   <li>/ws/iot/alarm/event - 报警事件推送</li>
 * </ul>
 *
 * @author 芋道源码
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource(name = "iotWebSocketHandler")  // 明确指定 Bean 名称
    private IotWebSocketHandler iotWebSocketHandler;

    @Resource(name = "alertWebSocketHandler")  // 报警事件 WebSocket Handler
    private AlertWebSocketHandler alertWebSocketHandler;

    @Resource(name = "deviceStatusWebSocketHandler")  // 统一设备状态 WebSocket Handler
    private DeviceStatusWebSocketHandler deviceStatusWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("[WebSocket Config] 注册 IoT WebSocket 端点（统一前缀: /ws/iot）");

        // 1. 注册 IoT 通用 WebSocket 端点
        registry.addHandler(iotWebSocketHandler, "/ws/iot")
                .setAllowedOriginPatterns("*");  // ⚠️ 生产环境应配置具体域名
        log.info("[WebSocket Config] ✅ /ws/iot - IoT 通用端点");

        // 2. 注册报警事件 WebSocket 端点
        registry.addHandler(alertWebSocketHandler, "/ws/iot/alarm/event")
                .setAllowedOriginPatterns("*");  // ⚠️ 生产环境应配置具体域名
        log.info("[WebSocket Config] ✅ /ws/iot/alarm/event - 报警事件");

        // 3. 注册统一设备状态 WebSocket 端点
        // 包含：设备状态、设备事件、命令结果、门禁事件等
        // Requirements: 7.3, 7.5, 8.1, 8.2, 8.3
        registry.addHandler(deviceStatusWebSocketHandler, 
                "/ws/iot/device/status",              // 通用设备状态
                "/ws/iot/access/device/status",       // 门禁设备状态
                "/ws/iot/access/event",               // 门禁事件
                "/ws/iot/access/auth-task/progress")  // 授权任务进度
                .setAllowedOriginPatterns("*");  // ⚠️ 生产环境应配置具体域名
        log.info("[WebSocket Config] ✅ /ws/iot/device/status, /ws/iot/access/* - 设备状态与门禁");

        log.info("[WebSocket Config] 🎉 所有 WebSocket 端点注册完成");
    }
}
