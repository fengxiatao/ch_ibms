package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.refund;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 停车退款记录 Response VO")
@Data
public class ParkingRefundRecordRespVO {

    @Schema(description = "退款记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "关联的停车记录ID", example = "100")
    private Long recordId;

    @Schema(description = "车牌号", example = "京A12345")
    private String plateNumber;

    @Schema(description = "原支付订单号", example = "PKG202401150001")
    private String outTradeNo;

    @Schema(description = "微信支付交易号", example = "4200001234567890")
    private String transactionId;

    @Schema(description = "退款单号", example = "REF202401150001")
    private String outRefundNo;

    @Schema(description = "微信退款单号", example = "50000123456789")
    private String refundId;

    @Schema(description = "原订单金额（元）", example = "10.00")
    private BigDecimal totalFee;

    @Schema(description = "退款金额（元）", example = "10.00")
    private BigDecimal refundFee;

    @Schema(description = "退款原因", example = "重复支付")
    private String refundReason;

    @Schema(description = "退款状态：0-申请中，1-退款成功，2-退款失败，3-退款关闭", example = "1")
    private Integer refundStatus;

    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    @Schema(description = "退款完成时间")
    private LocalDateTime refundTime;

    @Schema(description = "申请人")
    private String applyUser;

    @Schema(description = "审核人")
    private String auditUser;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
