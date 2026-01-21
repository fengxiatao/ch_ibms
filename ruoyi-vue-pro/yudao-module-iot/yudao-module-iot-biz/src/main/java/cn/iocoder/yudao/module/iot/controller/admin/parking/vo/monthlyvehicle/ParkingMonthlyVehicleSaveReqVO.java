package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyvehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 月租车新增/修改 Request VO")
@Data
public class ParkingMonthlyVehicleSaveReqVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "车牌号", requiredMode = Schema.RequiredMode.REQUIRED, example = "京A12345")
    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;

    @Schema(description = "车主姓名", example = "张三")
    private String ownerName;

    @Schema(description = "车主电话", example = "13800138000")
    private String ownerPhone;

    @Schema(description = "车辆类型", example = "1")
    private Integer vehicleType;

    @Schema(description = "所属车场ID", example = "1")
    private Long lotId;

    @Schema(description = "月卡有效期开始")
    private LocalDateTime validStart;

    @Schema(description = "月卡有效期结束")
    private LocalDateTime validEnd;

    @Schema(description = "月租费", example = "300.00")
    private BigDecimal monthlyFee;

    @Schema(description = "状态：0-正常，1-停用，2-过期", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;
}
