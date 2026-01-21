package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 电子巡更人员新增/修改 Request VO")
@Data
public class EpatrolPersonSaveReqVO {

    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "人员姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "人员姓名不能为空")
    private String name;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @Schema(description = "巡更棒编号", example = "XGB001")
    private String patrolStickNo;

    @Schema(description = "人员卡编号", example = "RYK001")
    private String personCardNo;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
