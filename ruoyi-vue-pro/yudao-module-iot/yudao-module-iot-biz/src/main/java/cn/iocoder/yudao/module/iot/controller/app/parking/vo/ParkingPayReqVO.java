package cn.iocoder.yudao.module.iot.controller.app.parking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 停车费微信支付 Request VO
 */
@Schema(description = "小程序 - 停车费微信支付 Request VO")
@Data
public class ParkingPayReqVO {

    @Schema(description = "微信授权码", requiredMode = Schema.RequiredMode.REQUIRED, example = "0b1xxxxxxxxxxx")
    @NotBlank(message = "微信授权码不能为空")
    private String code;

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "3.00")
    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;

    @Schema(description = "车牌号", requiredMode = Schema.RequiredMode.REQUIRED, example = "粤A12345")
    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;

    @Schema(description = "停车场ID", example = "1")
    private Long parkingLotId;
}
