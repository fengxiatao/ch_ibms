package cn.iocoder.yudao.module.iot.mq.consumer.gateway;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.GatewayDeviceSnapshotReplyMessage;
import cn.iocoder.yudao.module.iot.core.mq.message.GatewayDeviceSnapshotRequestMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 消费网关发起的设备快照请求，查询只读台账后回写 Reply Topic。
 */
@Slf4j
@Component
public class GatewayDeviceSnapshotRequestConsumer implements IotMessageSubscriber<GatewayDeviceSnapshotRequestMessage> {

    public static final String CONSUMER_GROUP = "iot-biz-gateway-snapshot-request";

    @Resource
    private IotMessageBus messageBus;

    @Resource
    private IotDeviceCommonApi iotDeviceCommonApi;

    @PostConstruct
    public void init() {
        messageBus.register(this);
    }

    @Override
    public String getTopic() {
        return IotMessageTopics.GATEWAY_DEVICE_SNAPSHOT_REQUEST;
    }

    @Override
    public String getGroup() {
        return CONSUMER_GROUP;
    }

    @Override
    public void onMessage(GatewayDeviceSnapshotRequestMessage message) {
        if (message == null || message.getCorrelationId() == null || message.getDeviceId() == null) {
            log.warn("[GatewayDeviceSnapshotRequestConsumer] 非法请求: {}", message);
            return;
        }
        try {
            IotDeviceGetReqDTO req = new IotDeviceGetReqDTO();
            req.setId(message.getDeviceId());
            CommonResult<IotDeviceRespDTO> res = iotDeviceCommonApi.getDevice(req);
            IotDeviceRespDTO data = res != null ? res.getData() : null;
            boolean ok = res != null && res.isSuccess() && data != null;
            GatewayDeviceSnapshotReplyMessage reply = GatewayDeviceSnapshotReplyMessage.builder()
                    .correlationId(message.getCorrelationId())
                    .code(ok ? GlobalErrorCodeConstants.SUCCESS.getCode() : GlobalErrorCodeConstants.NOT_FOUND.getCode())
                    .msg(ok ? GlobalErrorCodeConstants.SUCCESS.getMsg()
                            : (res != null ? res.getMsg() : "device not found"))
                    .device(data)
                    .build();
            messageBus.post(IotMessageTopics.GATEWAY_DEVICE_SNAPSHOT_REPLY, reply);
        } catch (Exception e) {
            log.error("[GatewayDeviceSnapshotRequestConsumer] 处理失败 deviceId={}", message.getDeviceId(), e);
            GatewayDeviceSnapshotReplyMessage reply = GatewayDeviceSnapshotReplyMessage.builder()
                    .correlationId(message.getCorrelationId())
                    .code(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode())
                    .msg(e.getMessage())
                    .device(null)
                    .build();
            try {
                messageBus.post(IotMessageTopics.GATEWAY_DEVICE_SNAPSHOT_REPLY, reply);
            } catch (Exception ex) {
                log.error("[GatewayDeviceSnapshotRequestConsumer] 回写 Reply 失败", ex);
            }
        }
    }
}
