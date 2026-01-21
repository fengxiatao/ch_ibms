package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 提交巡更结果 Request VO")
@Data
public class EpatrolTaskSubmitReqVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @Schema(description = "巡更记录列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "巡更记录不能为空")
    private List<PatrolRecordItem> records;

    @Schema(description = "是否清空巡更棒记录", example = "true")
    private Boolean clearStickData;

    @Data
    @Schema(description = "巡更记录项")
    public static class PatrolRecordItem {

        @Schema(description = "点位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XGD001")
        @NotNull(message = "点位编号不能为空")
        private String pointNo;

        @Schema(description = "人员卡编号", example = "RYK001")
        private String personCardNo;

        @Schema(description = "实际打卡时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01 08:12:00")
        @NotNull(message = "打卡时间不能为空")
        private LocalDateTime actualTime;

        @Schema(description = "实际顺序", example = "1")
        private Integer actualSort;

    }

}
