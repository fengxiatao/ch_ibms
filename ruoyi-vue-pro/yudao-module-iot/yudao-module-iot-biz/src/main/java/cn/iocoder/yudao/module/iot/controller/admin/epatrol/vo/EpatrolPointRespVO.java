package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 电子巡更点 Response VO")
@Data
public class EpatrolPointRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "点位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XGD001")
    private String pointNo;

    @Schema(description = "点位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1号楼大厅")
    private String pointName;

    @Schema(description = "点位位置", example = "A区1楼")
    private String pointLocation;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
