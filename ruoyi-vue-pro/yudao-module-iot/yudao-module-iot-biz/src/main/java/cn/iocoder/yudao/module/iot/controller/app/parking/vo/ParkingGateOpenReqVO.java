package cn.iocoder.yudao.module.iot.controller.app.parking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 道闸放行通知 Request VO
 */
@Schema(description = "小程序 - 道闸放行通知 Request VO")
@Data
public class ParkingGateOpenReqVO {

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "车牌号", requiredMode = Schema.RequiredMode.REQUIRED, example = "粤A12345")
    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;
}
