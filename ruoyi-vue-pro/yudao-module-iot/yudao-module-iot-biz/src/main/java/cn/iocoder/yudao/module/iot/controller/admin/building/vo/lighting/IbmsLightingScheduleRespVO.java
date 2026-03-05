package cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "管理后台 - 照明定时任务 Response VO")
@Data
public class IbmsLightingScheduleRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "任务名称")
    private String scheduleName;

    @Schema(description = "场景ID")
    private Long sceneId;

    @Schema(description = "场景名称")
    private String sceneName;

    @Schema(description = "执行类型 1-每天 2-工作日 3-周末 4-自定义")
    private Integer executeType;

    @Schema(description = "执行时间")
    private LocalTime executeTime;

    @Schema(description = "自定义周期JSON")
    private String customCycle;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecuteTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
