package cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 告警统计 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 告警统计 Response VO")
@Data
public class AlertStatisticsRespVO {

    @Schema(description = "告警总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    private Long totalAlerts;

    @Schema(description = "今日告警数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Long todayAlerts;

    @Schema(description = "本周告警数", requiredMode = Schema.RequiredMode.REQUIRED, example = "85")
    private Long weekAlerts;

    @Schema(description = "本月告警数", requiredMode = Schema.RequiredMode.REQUIRED, example = "350")
    private Long monthAlerts;

    @Schema(description = "未处理告警数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long unhandledAlerts;

    @Schema(description = "已处理告警数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long handledAlerts;

    @Schema(description = "各级别告警数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Long> alertsByLevel;

    @Schema(description = "各类型告警数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Long> alertsByType;

    @Schema(description = "告警趋势（最近7天）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<TrendData> alertTrend;

    @Schema(description = "告警处理率", requiredMode = Schema.RequiredMode.REQUIRED, example = "50.0")
    private Double handledRate;

    /**
     * 趋势数据
     */
    @Schema(description = "趋势数据")
    @Data
    public static class TrendData {
        @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-10-26")
        private String date;

        @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
        private Long count;
    }
}












