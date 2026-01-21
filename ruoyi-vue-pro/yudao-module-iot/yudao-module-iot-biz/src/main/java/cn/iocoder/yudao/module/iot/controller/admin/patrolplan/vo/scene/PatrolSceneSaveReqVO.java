package cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.scene;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 轮巡场景创建/更新 Request VO")
@Data
public class PatrolSceneSaveReqVO {

    @Schema(description = "场景ID", example = "1")
    private Long id;

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @Schema(description = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大堂入口")
    @NotBlank(message = "场景名称不能为空")
    private String sceneName;

    @Schema(description = "场景顺序", example = "1")
    private Integer sceneOrder;

    @Schema(description = "停留时长（秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "停留时长不能为空")
    private Integer duration;

    @Schema(description = "分屏布局", requiredMode = Schema.RequiredMode.REQUIRED, example = "2x2")
    @NotBlank(message = "分屏布局不能为空")
    private String gridLayout;

    @Schema(description = "窗格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    @NotNull(message = "窗格数量不能为空")
    private Integer gridCount;

    @Schema(description = "场景描述", example = "大堂入口监控场景")
    private String description;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "场景通道列表")
    private List<PatrolSceneChannelSaveReqVO> channels;

}
