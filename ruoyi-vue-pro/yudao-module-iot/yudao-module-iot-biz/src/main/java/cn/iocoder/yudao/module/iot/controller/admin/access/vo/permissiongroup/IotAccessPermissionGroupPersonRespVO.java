package cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 权限组关联人员 Response VO
 */
@Schema(description = "管理后台 - 权限组关联人员 Response VO")
@Data
public class IotAccessPermissionGroupPersonRespVO {

    @Schema(description = "人员ID")
    private Long personId;

    @Schema(description = "人员编号")
    private String personCode;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

}
