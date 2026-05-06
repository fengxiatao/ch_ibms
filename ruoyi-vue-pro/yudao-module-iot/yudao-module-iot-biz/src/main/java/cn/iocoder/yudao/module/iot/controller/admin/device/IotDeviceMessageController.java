package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessageRespPairVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessageRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessageSendReqVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceStateChangeMessage;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceMessageMapper;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import org.springframework.beans.factory.annotation.Qualifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - IoT 设备消息")
@RestController
@RequestMapping("/iot/device/message")
@Validated
public class IotDeviceMessageController {

    @Resource
    private IotDeviceMessageService deviceMessageService;
    @Resource
    private IotThingModelService thingModelService;
    @Resource
    private IotDeviceMessageMapper deviceMessageMapper;

    /**
     * RocketMQ 消息总线（v21 新增：调试端点 /test-publish-state-change 直接 post 到 DEVICE_STATE_CHANGED 主题）
     */
    @Resource
    @Qualifier("iotRocketMQMessageBus")
    private IotMessageBus iotMessageBus;

    @GetMapping("/page")
    @Operation(summary = "获得设备消息分页")
    @PreAuthorize("@ss.hasPermission('iot:device:message-query')")
    public CommonResult<PageResult<IotDeviceMessageRespVO>> getDeviceMessagePage(
            @Valid IotDeviceMessagePageReqVO pageReqVO) {
        PageResult<IotDeviceMessageDO> pageResult = deviceMessageService.getDeviceMessagePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceMessageRespVO.class));
    }

    @GetMapping("/pair-page")
    @Operation(summary = "获得设备消息对分页")
    @PreAuthorize("@ss.hasPermission('iot:device:message-query')")
    public CommonResult<PageResult<IotDeviceMessageRespPairVO>> getDeviceMessagePairPage(
            @Valid IotDeviceMessagePageReqVO pageReqVO) {
        // 1.1 先按照条件，查询 request 的消息（非 reply）
        pageReqVO.setReply(false);
        PageResult<IotDeviceMessageDO> requestMessagePageResult = deviceMessageService.getDeviceMessagePage(pageReqVO);
        if (CollUtil.isEmpty(requestMessagePageResult.getList())) {
            return success(PageResult.empty());
        }
        // 1.2 接着按照 requestIds，批量查询 reply 消息
        List<String> requestIds = convertList(requestMessagePageResult.getList(), IotDeviceMessageDO::getRequestId);
        List<IotDeviceMessageDO> replyMessageList = deviceMessageService.getDeviceMessageListByRequestIdsAndReply(
                pageReqVO.getDeviceId(), requestIds, true);
        Map<String, IotDeviceMessageDO> replyMessages = convertMap(replyMessageList, IotDeviceMessageDO::getRequestId);

        // 2. 组装结果
        List<IotDeviceMessageRespPairVO> pairMessages = convertList(requestMessagePageResult.getList(),
                requestMessage -> {
            IotDeviceMessageDO replyMessage = replyMessages.get(requestMessage.getRequestId());
            return new IotDeviceMessageRespPairVO()
                    .setRequest(BeanUtils.toBean(requestMessage, IotDeviceMessageRespVO.class))
                    .setReply(BeanUtils.toBean(replyMessage, IotDeviceMessageRespVO.class));
        });
        return success(new PageResult<>(pairMessages, requestMessagePageResult.getTotal()));
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息", description = "可用于设备模拟")
    @PreAuthorize("@ss.hasPermission('iot:device:message-end')")
    public CommonResult<Boolean> sendDeviceMessage(@Valid @RequestBody IotDeviceMessageSendReqVO sendReqVO) {
        deviceMessageService.sendDeviceMessage(BeanUtils.toBean(sendReqVO, IotDeviceMessage.class));
        return success(true);
    }

    /**
     * v21 调试端点：模拟发布 DeviceStateChangeMessage 到 DEVICE_STATE_CHANGED 主题，
     * 用于测试 DeviceStateChangeConsumer 中数据流转规则触发链路（thing.state.update 路径）。
     *
     * <p>正常生产链路：Gateway 端 DeviceLifecycleManager / GatewayMessagePublisher 在设备真实上下线时发布。
     * 此端点仅用于本机/集成测试，不接入业务流程。</p>
     *
     * @param req 状态变更请求体（deviceId/newState/reason/deviceType/productId/tenantId）
     * @return 是否发布成功
     */
    @PostMapping("/test-publish-state-change")
    @Operation(summary = "[调试] 发布设备状态变更消息", description = "v21 测试 DeviceStateChangeConsumer + 数据流转规则链路")
    @PreAuthorize("@ss.hasPermission('iot:device:message-end')")
    public CommonResult<Boolean> testPublishStateChange(@RequestBody Map<String, Object> req) {
        Long deviceId = ((Number) req.get("deviceId")).longValue();
        Integer newState = req.get("newState") != null ? ((Number) req.get("newState")).intValue()
                : IotDeviceStateEnum.ONLINE.getState();
        Integer previousState = req.get("previousState") != null ? ((Number) req.get("previousState")).intValue()
                : IotDeviceStateEnum.OFFLINE.getState();
        String reason = (String) req.getOrDefault("reason", "[v21 test] state change debug trigger");
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