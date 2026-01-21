package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 报警事件处理（前端兼容） Request VO
 *
 * <p>用于兼容前端历史页面的 process/ignore 行为：
 * 前端会提交 result/actions/remark，这里统一合并为 handleRemark 写入。</p>
 */
@Schema(description = "管理后台 - 报警事件处理（兼容前端） Request VO")
@Data
public class IotAlarmEventProcessReqVO {

    @Schema(description = "事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "事件ID不能为空")
    private Long id;

    @Schema(description = "处理结果（前端字段）", example = "normal")
    private String result;

    @Schema(description = "处理动作（前端字段）", example = "[\"现场检查\",\"联系安保\"]")
    private List<String> actions;

    @Schema(description = "处理备注（前端字段）", example = "已确认并处理")
    private String remark;
}


























