package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 能耗计量表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsEnergyMeterPageReqVO extends PageParam {

    @Schema(description = "仪表编码")
    private String meterCode;

    @Schema(description = "仪表名称")
    private String meterName;

    @Schema(description = "仪表类型 1-电表 2-水表 3-燃气表 4-冷量表 5-热量表")
    private Integer meterType;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "状态 0-离线 1-在线 2-故障")
    private Integer status;

}
