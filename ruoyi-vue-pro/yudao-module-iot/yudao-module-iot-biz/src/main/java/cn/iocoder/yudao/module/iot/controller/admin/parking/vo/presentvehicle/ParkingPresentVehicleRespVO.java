package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.presentvehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 在场车辆 Response VO")
@Data
public class ParkingPresentVehicleRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
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

    @Schema(description = "入场车道名称", example = "1号车道")
    private String entryLaneName;

    @Schema(description = "入场道闸ID", example = "1")
    private Long entryGateId;

    @Schema(description = "入场时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime entryTime;

    @Schema(description = "入场照片URL")
    private String entryPhotoUrl;

    @Schema(description = "入场操作员")
    private String entryOperator;

    @Schema(description = "停车时长(分钟)", example = "120")
    private Integer parkingDuration;

    @Schema(description = "长期停车标识：0-否，1-超一个月，2-超三个月", example = "0")
    private Integer longTermFlag;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
