package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceStateChangeMessage;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 设备消息 — 调试端点（v23 抽离 + 守卫）。
 *
 * <p>仅在 application.yaml 配置 {@code yudao.iot.debug-endpoints.enabled=true}
 * 时才会注册为 Spring Bean，生产环境默认 404。</p>
 *
 * <p>v21 由 {@code IotDeviceMessageController} 引入 {@code /test-publish-state-change}
 * 端点用于直接 post 到 DEVICE_STATE_CHANGED 主题验证 STATE_UPDATE 路径数据流转规则。
 * v23 将该端点连同其专属依赖 {@code iotMessageBus} 抽离至本类，并加入 profile 级守卫，
 * 避免线上误调。</p>
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "[调试] IoT 设备消息")
@RestController
@RequestMapping("/iot/device/message")
@ConditionalOnProperty(name = "yudao.iot.debug-endpoints.enabled", havingValue = "true")
@Validated
public class IotDebugDeviceMessageController {

    /**
     * RocketMQ 消息总线，用于直接 post 到指定主题。
     */
    @Resource
    @Qualifier("iotRocketMQMessageBus")
    private IotMessageBus iotMessageBus;

    /**
     * 调试端点：模拟发布 {@link DeviceStateChangeMessage} 到 DEVICE_STATE_CHANGED 主题，
     * 用于测试 {@code DeviceStateChangeConsumer} 中数据流转规则触发链路（thing.state.update 路径）。
     *
     * <p>正常生产链路：Gateway 端 DeviceLifecycleManager / GatewayMessagePublisher
     * 在设备真实上下线时发布。此端点仅用于本机/集成测试，不接入业务流程。</p>
     *
     * @param req 状态变更请求体（deviceId/newState/reason/deviceType/productId/tenantId）
     * @return 是否发布成功
     */
    @PostMapping("/test-publish-state-change")
    @Operation(summary = "[调试] 发布设备状态变更消息", description = "测试 DeviceStateChangeConsumer + 数据流转规则链路；需 yudao.iot.debug-endpoints.enabled=true")
    @PreAuthorize("@ss.hasPermission('iot:device:message-end')")
    public CommonResult<Boolean> testPublishStateChange(@RequestBody Map<String, Object> req) {
        Long deviceId = ((Number) req.get("deviceId")).longValue();
        Integer newState = req.get("newState") != null ? ((Number) req.get("newState")).intValue()
                : IotDeviceStateEnum.ONLINE.getState();
        Integer previousState = req.get("previousState") != null ? ((Number) req.get("previousState")).intValue()
                : IotDeviceStateEnum.OFFLINE.getState();
        String reason = (String) req.getOrDefault("reason", "[debug] state change debug trigger");
        String deviceType = (String) req.getOrDefault("deviceType", "UNKNOWN");
        String deviceName = (String) req.getOrDefault("deviceName", "test-device");
        Long productId = req.get("productId") != null ? ((Number) req.get("productId")).longValue() : null;
        Long tenantId = req.get("tenantId") != null ? ((Number) req.get("tenantId")).longValue() : 1L;

        DeviceStateChangeMessage message = DeviceStateChangeMessage.of(
                deviceId, deviceName, deviceType, productId,
                previousState, newState, tenantId, ConnectionMode.PASSIVE, reason);
        iotMessageBus.post(IotMessageTopics.DEVICE_STATE_CHANGED, message);
        return success(true);
    }

}
