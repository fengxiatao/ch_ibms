package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.rechargerecord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 月卡充值记录 Response VO")
@Data
public class ParkingRechargeRecordRespVO {

    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "月租车ID", example = "1")
    private Long monthlyVehicleId;

    @Schema(description = "车牌号", requiredMode = Schema.RequiredMode.REQUIRED, example = "京A12345")
    private String plateNumber;

    @Schema(description = "车主姓名", example = "张三")
    private String ownerName;

    @Schema(description = "车主电话", example = "13800138000")
    private String ownerPhone;

    @Schema(description = "充值月数", example = "3")
    private Integer rechargeMonths;

    @Schema(description = "有效期开始")
    private LocalDateTime validStart;

    @Schema(description = "有效期结束")
    private LocalDateTime validEnd;

    @Schema(description = "应收金额", example = "900.00")
    private BigDecimal chargeAmount;

    @Schema(description = "实收金额", example = "900.00")
    private BigDecimal paidAmount;

    @Schema(description = "支付方式：cash-现金，wechat-微信，alipay-支付宝，card-刷卡")
    private String paymentMethod;

    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;

    @Schema(description = "支付状态：0-未支付，1-已支付", example = "1")
    private Integer paymentStatus;

    @Schema(description = "操作员")
    private String operator;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
