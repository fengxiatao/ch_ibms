package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 车道新增/修改 Request VO")
@Data
public class ParkingLaneSaveReqVO {

    @Schema(description = "车道ID", example = "1")
    private Long id;

    @Schema(description = "车道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1号车道")
    @NotBlank(message = "车道名称不能为空")
    private String laneName;

    @Schema(description = "车道编码", example = "LANE001")
    private String laneCode;

    @Schema(description = "所属车场ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "所属车场不能为空")
    private Long lotId;

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
}
