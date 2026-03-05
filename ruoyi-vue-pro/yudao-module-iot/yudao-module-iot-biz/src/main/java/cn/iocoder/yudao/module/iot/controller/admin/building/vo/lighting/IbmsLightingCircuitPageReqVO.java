package cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 照明回路分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsLightingCircuitPageReqVO extends PageParam {

    @Schema(description = "回路编码")
    private String circuitCode;

    @Schema(description = "回路名称")
    private String circuitName;

    @Schema(description = "回路类型 1-普通照明 2-应急照明 3-景观照明 4-调光照明")
    private Integer circuitType;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "状态 0-关闭 1-开启 2-故障")
    private Integer status;

}
