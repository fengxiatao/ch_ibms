package cn.iocoder.yudao.module.iot.controller.admin.access.vo.department;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁部门 Response VO
 */
@Schema(description = "管理后台 - 门禁部门 Response VO")
@Data
public class IotAccessDepartmentRespVO {

    @Schema(description = "部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "父部门ID", example = "0")
    private Long parentId;

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "部门编码", example = "RD001")
    private String deptCode;

    @Schema(description = "显示顺序", example = "1")
    private Integer sort;

    @Schema(description = "负责人", example = "张三")
    private String leader;

    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子部门列表")
    private List<IotAccessDepartmentRespVO> children;

    @Schema(description = "人员数量", example = "10")
    private Integer personCount;

}
