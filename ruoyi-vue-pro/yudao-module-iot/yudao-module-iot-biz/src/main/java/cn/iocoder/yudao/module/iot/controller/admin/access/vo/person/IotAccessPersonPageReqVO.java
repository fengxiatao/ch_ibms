package cn.iocoder.yudao.module.iot.controller.admin.access.vo.person;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门禁人员分页 Request VO
 */
@Schema(description = "管理后台 - 门禁人员分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotAccessPersonPageReqVO extends PageParam {

    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    @Schema(description = "人员类型（1员工 2访客 3临时）", example = "1")
    private Integer personType;

    @Schema(description = "部门ID", example = "1")
    private Long deptId;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Integer status;

}
