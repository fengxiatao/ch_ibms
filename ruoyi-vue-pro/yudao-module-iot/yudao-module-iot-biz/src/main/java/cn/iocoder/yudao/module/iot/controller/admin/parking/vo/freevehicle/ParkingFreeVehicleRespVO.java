package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.freevehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 免费车 Response VO")
@Data
public class ParkingFreeVehicleRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "车牌号", requiredMode = Schema.RequiredMode.REQUIRED, example = "京A12345")
    private String plateNumber;

    @Schema(description = "车主姓名", example = "张三")
    private String ownerName;

    @Schema(description = "车主电话", example = "13800138000")
    private String ownerPhone;

    @Schema(description = "车辆类型：1-小型车，2-中型车，3-新能源车，4-大型车，5-超大型车", example = "1")
    private Integer vehicleType;

    @Schema(description = "特殊车类型：武警车、警车等", example = "警车")
    private String specialType;

    @Schema(description = "有效期开始")
    private LocalDateTime validStart;

    @Schema(description = "有效期结束")
    private LocalDateTime validEnd;

    @Schema(description = "适用车场ID列表(JSON数组)", example = "[1,2,3]")
    private String lotIds;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
