package cn.iocoder.yudao.module.iot.controller.admin.videoinspection.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Schema(description = "管理后台 - 视频巡检任务创建/更新 Request VO")
@Data
public class InspectionTaskSaveReqVO {

    @Schema(description = "任务ID（更新时必填）", example = "1")
    private Long id;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一楼大厅巡检")
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "分屏布局", example = "3x3")
    private String layout;

    @Schema(description = "场景配置")
    private List<InspectionTaskRespVO.InspectionSceneVO> scenes;

    @Schema(description = "任务状态", example = "draft")
    private String status;
}
