package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * GIS 图层要素列表 Request VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 图层要素列表 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotGisLayerFeaturesReqVO extends PageParam {

    @Schema(description = "图层名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "device")
    @NotBlank(message = "图层名称不能为空")
    private String layer;

    @Schema(description = "搜索关键词", example = "温度传感器")
    private String keyword;

    @Schema(description = "状态", example = "online")
    private String status;
}












