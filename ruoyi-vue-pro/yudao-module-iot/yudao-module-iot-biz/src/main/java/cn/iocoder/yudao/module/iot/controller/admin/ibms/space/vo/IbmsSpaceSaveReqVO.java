package cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IbmsSpaceSaveReqVO {

    @Schema(description = "ID，更新时必填")
    private Long id;

    @Schema(description = "父空间ID（0 为根）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long parentId;

    @Schema(description = "空间编码（组合）：code[-subCode]，如 F01 / F01-LBY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String spaceCode;

    @Schema(description = "区域码，如 F01/B01/PK/LB/OUT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String code;

    @Schema(description = "子区域码，如 LBY/FM（可选）")
    private String subCode;

    @Schema(description = "空间名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "空间类型：floor/area/room", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String type;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "扩展 JSON 字符串")
    private String extra;
}

