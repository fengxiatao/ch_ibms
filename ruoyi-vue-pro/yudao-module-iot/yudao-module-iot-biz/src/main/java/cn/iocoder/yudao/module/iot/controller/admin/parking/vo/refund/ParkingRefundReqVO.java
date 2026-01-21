package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.refund;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 停车退款申请 Request VO")
@Data
public class ParkingRefundReqVO {

    @Schema(description = "停车记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "停车记录ID不能为空")
    private Long recordId;

    @Schema(description = "退款金额（元），不传则全额退款", example = "10.00")
    private BigDecimal refundFee;

    @Schema(description = "退款原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "重复支付")
    @NotNull(message = "退款原因不能为空")
    private String refundReason;

    @Schema(description = "备注")
    private String remark;
}
