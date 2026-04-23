package cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IbmsUnassignedPageReqVO extends PageParam {

    @Schema(description = "关键字（设备：名称/编码；通道：名称/编码/系统）")
    private String keyword;

    @Schema(description = "专业分组码（仅设备支持）")
    private String groupCode;

    @Schema(description = "类型：device/point（为空表示全部）")
    private String type;
}

