package cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 照明场景 Response VO")
@Data
public class IbmsLightingSceneRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "场景编码")
    private String sceneCode;

    @Schema(description = "场景名称")
    private String sceneName;

    @Schema(description = "场景图标")
    private String sceneIcon;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "回路配置JSON")
    private String circuitConfig;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
