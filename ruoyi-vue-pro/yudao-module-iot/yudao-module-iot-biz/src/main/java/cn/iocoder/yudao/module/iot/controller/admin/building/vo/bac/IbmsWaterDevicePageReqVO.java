package cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 给排水设备分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsWaterDevicePageReqVO extends PageParam {

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型 1-生活水泵 2-消防水泵 3-污水泵 4-水箱 5-阀门")
    private Integer deviceType;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "状态 0-离线 1-在线 2-故障")
    private Integer status;

    @Schema(description = "运行状态 0-停止 1-运行 2-待机")
    private Integer runningStatus;

}
