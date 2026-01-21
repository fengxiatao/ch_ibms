package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 电子巡更路线分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EpatrolRoutePageReqVO extends PageParam {

    @Schema(description = "路线名称", example = "白班巡更路线")
    private String routeName;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
