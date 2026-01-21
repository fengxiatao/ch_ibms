package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 路径步骤响应 VO
 *
 * @author ruoyi
 */
@Schema(description = "管理后台 - 路径步骤响应 VO")
@Data
public class PathStepRespVO {

    @Schema(description = "区域ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long areaId;

    @Schema(description = "区域名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "101室")
    private String areaName;

    @Schema(description = "区域类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ROOM")
    private String areaType;

    @Schema(description = "步骤序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer stepNumber;

    @Schema(description = "累计成本", requiredMode = Schema.RequiredMode.REQUIRED, example = "2.5")
    private BigDecimal totalCost;

    @Schema(description = "指令说明", requiredMode = Schema.RequiredMode.REQUIRED, example = "从 101室 出发")
    private String instruction;

}











