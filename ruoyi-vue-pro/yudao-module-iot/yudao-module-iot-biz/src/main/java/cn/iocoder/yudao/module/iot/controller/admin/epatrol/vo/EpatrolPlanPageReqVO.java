package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 电子巡更计划分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EpatrolPlanPageReqVO extends PageParam {

    @Schema(description = "计划编号", example = "JH20240101001")
    private String planCode;

    @Schema(description = "计划名称", example = "日常巡更计划")
    private String planName;

    @Schema(description = "巡更路线ID", example = "1")
    private Long routeId;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
