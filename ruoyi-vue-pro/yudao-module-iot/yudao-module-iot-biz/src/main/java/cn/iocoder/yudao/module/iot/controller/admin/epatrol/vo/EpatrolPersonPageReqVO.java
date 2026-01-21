package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 电子巡更人员分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EpatrolPersonPageReqVO extends PageParam {

    @Schema(description = "人员姓名", example = "张三")
    private String name;

    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "巡更棒编号", example = "XGB001")
    private String patrolStickNo;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
