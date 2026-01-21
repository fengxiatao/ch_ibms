package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 长辉批量升级进度 VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉批量升级进度 VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChanghuiBatchUpgradeProgressVO {

    @Schema(description = "总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer total;

    @Schema(description = "待执行数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer pendingCount;

    @Schema(description = "进行中数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer inProgressCount;

    @Schema(description = "成功数", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Integer successCount;

    @Schema(description = "失败数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer failedCount;

    @Schema(description = "已取消数", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer cancelledCount;

    @Schema(description = "各任务进度详情")
    private List<TaskProgress> taskProgresses;

    /**
     * 任务进度
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskProgress {

        @Schema(description = "任务ID", example = "1")
        private Long taskId;

        @Schema(description = "测站编码", example = "1234567890")
        private String stationCode;

        @Schema(description = "状态", example = "1")
        private Integer status;

        @Schema(description = "状态名称", example = "进行中")
        private String statusName;

        @Schema(description = "进度(0-100)", example = "50")
        private Integer progress;

        @Schema(description = "错误信息", example = "")
        private String errorMessage;

    }

}
