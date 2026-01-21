package cn.iocoder.yudao.module.iot.controller.admin.access.vo.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 门禁部门创建 Request VO
 */
@Schema(description = "管理后台 - 门禁部门创建 Request VO")
@Data
public class IotAccessDepartmentCreateReqVO {

    @Schema(description = "父部门ID", example = "0")
    private Long parentId;

    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "研发部")
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称长度不能超过50个字符")
    private String deptName;

    @Schema(description = "部门编码", example = "RD001")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    private String deptCode;

    @Schema(description = "显示顺序", example = "1")
    private Integer sort;

    @Schema(description = "负责人", example = "张三")
    @Size(max = 50, message = "负责人长度不能超过50个字符")
    private String leader;

    @Schema(description = "联系电话", example = "13800138000")
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String phone;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Integer status;

}
