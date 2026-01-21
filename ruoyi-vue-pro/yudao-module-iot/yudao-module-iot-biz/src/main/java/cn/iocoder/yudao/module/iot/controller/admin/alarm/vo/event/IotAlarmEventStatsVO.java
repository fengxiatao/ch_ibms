package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 报警事件统计 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 报警事件统计 Response VO")
@Data
public class IotAlarmEventStatsVO {

    @Schema(description = "紧急报警数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long urgentCount;

    @Schema(description = "今日报警数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long todayCount;

    @Schema(description = "活跃主机数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long activeHosts;

    @Schema(description = "处理率（百分比）", requiredMode = Schema.RequiredMode.REQUIRED, example = "85.5")
    private Double processedRate;

    // ========== 顶部统计条字段 ==========

    @Schema(description = "今日事件总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Long total;

    @Schema(description = "报警事件数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Long alarm;

    @Schema(description = "恢复事件数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "18")
    private Long restore;

    @Schema(description = "其他事件数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long other;
}
