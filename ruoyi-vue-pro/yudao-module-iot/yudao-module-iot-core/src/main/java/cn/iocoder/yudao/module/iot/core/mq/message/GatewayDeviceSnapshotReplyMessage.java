package cn.iocoder.yudao.module.iot.core.mq.message;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Biz 对 {@link GatewayDeviceSnapshotRequestMessage} 的应答。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayDeviceSnapshotReplyMessage {

    @Builder.Default
    private String schemaVersion = "1";

    private String correlationId;

    /**
     * 与 {@link cn.iocoder.yudao.framework.common.pojo.CommonResult} 对齐的业务码
     */
    private Integer code;

    private String msg;

    private IotDeviceRespDTO device;
}
