package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 停车进出记录 Response VO")
@Data
public class ParkingRecordRespVO {

    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "车牌号", requiredMode = Schema.RequiredMode.REQUIRED, example = "京A12345")
    private String plateNumber;

    @Schema(description = "车辆类型", example = "1")
    private Integer vehicleType;

    @Schema(description = "车辆类别：free-免费车，monthly-月租车，temporary-临时车", example = "temporary")
    private String vehicleCategory;

    @Schema(description = "车场ID", example = "1")
    private Long lotId;

    @Schema(description = "车场名称", example = "地下车库A区")
    private String lotName;

    @Schema(description = "入场车道ID", example = "1")
    private Long entryLaneId;

    @Schema(description = "入场时间")
    private LocalDateTime entryTime;

    @Schema(description = "入场照片URL")
    private String entryPhotoUrl;

    @Schema(description = "入场操作员")
    private String entryOperator;

    @Schema(description = "出场车道ID", example = "1")
    private Long exitLaneId;

    @Schema(description = "出场时间")
    private LocalDateTime exitTime;

    @Schema(description = "出场照片URL")
    private String exitPhotoUrl;

    @Schema(description = "出场操作员")
    private String exitOperator;

    @Schema(description = "停车时长(分钟)", example = "120")
    private Integer parkingDuration;

    @Schema(description = "应收金额", example = "10.00")
    private BigDecimal chargeAmount;

    @Schema(description = "实收金额", example = "10.00")
    private BigDecimal paidAmount;

    @Schema(description = "支付方式：cash-现金，wechat-微信，alipay-支付宝，card-刷卡")
    private String paymentMethod;

    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;

    @Schema(description = "支付状态：0-未支付，1-已支付，2-免费", example = "1")
    private Integer paymentStatus;

    @Schema(description = "放行方式：normal-正常，force-强制出场，free-免费放行")
    private String exitType;

    @Schema(description = "记录状态：1-在场，2-已出场", example = "2")
    private Integer recordStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
