package cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IbmsProductPointTypeVO {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "点位类型码 VT/AI/DR 等")
    private String pointTypeCode;

    @Schema(description = "点位名称")
    private String name;

    @Schema(description = "数量")
    private Integer count;

    @Schema(description = "数据类型，如 视频流/DI/DO")
    private String dataType;
}

