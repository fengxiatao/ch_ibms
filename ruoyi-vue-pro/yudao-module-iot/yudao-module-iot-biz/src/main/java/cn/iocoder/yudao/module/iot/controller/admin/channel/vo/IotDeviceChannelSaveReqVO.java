package cn.iocoder.yudao.module.iot.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * IoT 设备通道保存 Request VO（新增/修改）
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - IoT 设备通道保存 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDeviceChannelSaveReqVO extends IotDeviceChannelBaseVO {

    @Schema(description = "通道ID（修改时必填）", example = "1001")
    private Long id;

}
