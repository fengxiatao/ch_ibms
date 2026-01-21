package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 电子巡更点新增/修改 Request VO")
@Data
public class EpatrolPointSaveReqVO {

    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "点位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XGD001")
    @NotBlank(message = "点位编号不能为空")
    private String pointNo;

    @Schema(description = "点位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1号楼大厅")
    @NotBlank(message = "点位名称不能为空")
    private String pointName;

    @Schema(description = "点位位置", example = "A区1楼")
    private String pointLocation;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
