package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 路径规划请求 VO
 *
 * @author ruoyi
 */
@Schema(description = "管理后台 - 路径规划请求 VO")
@Data
public class PathRequestVO {

    @Schema(description = "起点区域ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "起点区域ID不能为空")
    private Long fromAreaId;

    @Schema(description = "终点区域ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "256")
    @NotNull(message = "终点区域ID不能为空")
    private Long toAreaId;

    @Schema(description = "是否仅无障碍通道", example = "false")
    private Boolean accessibleOnly = false;

}

