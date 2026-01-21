package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 车道 Response VO")
@Data
public class ParkingLaneRespVO {

    @Schema(description = "车道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "车道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1号车道")
    private String laneName;

    @Schema(description = "车道编码", example = "LANE001")
    private String laneCode;

    @Schema(description = "所属车场ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long lotId;

    @Schema(description = "所属车场名称", example = "地下车库A区")
    private String lotName;

    @Schema(description = "出入口配置：1-入口，2-出口，3-出入口", example = "1")
    private Integer direction;

    @Schema(description = "主相机设备ID", example = "1")
    private Long mainCameraId;

    @Schema(description = "主显屏幕设备ID", example = "1")
    private Long mainScreenId;

    @Schema(description = "辅助相机设备ID", example = "1")
    private Long auxCameraId;

    @Schema(description = "辅显屏幕设备ID", example = "1")
    private Long auxScreenId;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
