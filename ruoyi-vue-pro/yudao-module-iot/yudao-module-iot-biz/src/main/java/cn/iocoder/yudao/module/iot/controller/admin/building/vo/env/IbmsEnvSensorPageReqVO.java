package cn.iocoder.yudao.module.iot.controller.admin.building.vo.env;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 环境传感器分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsEnvSensorPageReqVO extends PageParam {

    @Schema(description = "传感器编码", example = "ENV-001")
    private String sensorCode;

    @Schema(description = "传感器名称", example = "温湿度传感器")
    private String sensorName;

    @Schema(description = "传感器类型 1-温湿度 2-PM2.5 3-CO2 4-噪音 5-光照 6-气压")
    private Integer sensorType;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "状态 0-离线 1-在线 2-故障")
    private Integer status;

}
