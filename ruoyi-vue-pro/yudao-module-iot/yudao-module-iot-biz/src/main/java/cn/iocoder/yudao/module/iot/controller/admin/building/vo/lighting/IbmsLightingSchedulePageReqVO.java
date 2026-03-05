package cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 照明定时任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsLightingSchedulePageReqVO extends PageParam {

    @Schema(description = "任务名称")
    private String scheduleName;

    @Schema(description = "场景ID")
    private Long sceneId;

    @Schema(description = "是否启用")
    private Boolean enabled;

}
