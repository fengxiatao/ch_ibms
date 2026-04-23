package cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IbmsUnassignedItemRespVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "类型：device/point")
    private String type;

    @Schema(description = "编码（设备编码/通道编码）")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "专业分组码（通道固定为 '-'）")
    private String group;

    @Schema(description = "系统码（设备 systemCode / 通道 systemType）")
    private String system;
}

