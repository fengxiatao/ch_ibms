package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 能耗费率分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IbmsEnergyRatePageReqVO extends PageParam {

    @Schema(description = "费率名称")
    private String rateName;

    @Schema(description = "能源类型：1-电力 2-水 3-燃气 4-冷 5-热")
    private Integer energyType;

    @Schema(description = "费率类型：1-单一费率 2-阶梯费率 3-峰谷电价")
    private Integer rateType;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

}
