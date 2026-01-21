package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyvehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 月租车 Response VO")
@Data
public class ParkingMonthlyVehicleRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "车牌号", requiredMode = Schema.RequiredMode.REQUIRED, example = "京A12345")
    private String plateNumber;

    @Schema(description = "车主姓名", example = "张三")
    private String ownerName;

    @Schema(description = "车主电话", example = "13800138000")
    private String ownerPhone;

    @Schema(description = "车辆类型", example = "1")
    private Integer vehicleType;

    @Schema(description = "所属车场ID", example = "1")
    private Long lotId;

    @Schema(description = "所属车场名称", example = "地下车库A区")
    private String lotName;

    @Schema(description = "月卡有效期开始")
    private LocalDateTime validStart;

    @Schema(description = "月卡有效期结束")
    private LocalDateTime validEnd;

    @Schema(description = "月租费", example = "300.00")
    private BigDecimal monthlyFee;

    @Schema(description = "最后充值时间")
    private LocalDateTime lastChargeTime;

    @Schema(description = "最后充值月数", example = "3")
    private Integer lastChargeMonths;

    @Schema(description = "状态：0-正常，1-停用，2-过期", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
