package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 访客来访事由 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 访客来访事由 Response VO")
@Data
public class IotVisitorReasonRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "来访事由名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "参观")
    private String reasonName;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态：0-正常 1-停用", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "默认事由")
    private String remark;

}
