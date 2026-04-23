package cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IbmsProductPropertyVO {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "属性英文名 key")
    private String propName;

    @Schema(description = "显示名称")
    private String label;

    @Schema(description = "类型：text/number/select/checkbox")
    private String type;

    @Schema(description = "可选项 JSON 字符串")
    private String options;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "单位")
    private String unit;
}

