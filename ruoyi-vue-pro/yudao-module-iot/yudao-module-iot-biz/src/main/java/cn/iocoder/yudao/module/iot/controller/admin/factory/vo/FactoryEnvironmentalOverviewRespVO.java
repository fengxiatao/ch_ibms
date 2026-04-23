package cn.iocoder.yudao.module.iot.controller.admin.factory.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件说明：工厂环保监测工作台响应 VO
 *
 * <p>说明：独立页环保监测与合规管理中的环保监测 Tab 完全解耦，
 * 当前 VO 仅服务于 `/iot/factory/environmental/dashboard` 独立聚合接口。</p>
 *
 * @author GPT-5.4
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactoryEnvironmentalOverviewRespVO {

    /**
     * 数据更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 顶部 KPI
     */
    private List<KpiCardItem> kpiCards;

    /**
     * 废气排放监测
     */
    private AirEmissionCard airEmission;

    /**
     * 废水排放监测
     */
    private WastewaterCard wastewater;

    /**
     * 噪声监测
     */
    private NoiseCard noise;

    /**
     * 环保预警
     */
    private List<AlertItem> alerts;

    /**
     * 顶部指标卡
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiCardItem {

        private String key;

        private String title;

        private BigDecimal value;

        private String valueText;

        private String unit;

        private String icon;

        private String theme;

        private String status;
    }

    /**
     * 废气卡
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AirEmissionCard {

        private String title;

        private String overallStatusText;

        private List<AirEmissionItem> items;
    }

    /**
     * 废气明细项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AirEmissionItem {

        private String pointCode;

        private String pointName;

        private BigDecimal value;

        private String valueText;

        private String unit;

        private BigDecimal limitValue;

        private String limitValueText;

        private String status;

        private String tone;

        private BigDecimal progressPercent;

        private BigDecimal limitMarkerPercent;
    }

    /**
     * 废水卡
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WastewaterCard {

        private String title;

        private String overallStatusText;

        private List<WastewaterItem> items;
    }

    /**
     * 废水明细项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WastewaterItem {

        private String pointCode;

        private String pointName;

        private BigDecimal value;

        private String valueText;

        private String displayUnitText;

        private String status;

        private String tone;

        private String theme;

        private BigDecimal progressPercent;
    }

    /**
     * 噪声卡
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoiseCard {

        private String title;

        private NoiseGauge day;

        private NoiseGauge night;

        private NoiseLimit limit;
    }

    /**
     * 噪声仪表项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoiseGauge {

        private String label;

        private BigDecimal value;

        private String valueText;

        private String unit;

        private String tone;

        private BigDecimal percent;
    }

    /**
     * 噪声限值项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoiseLimit {

        private String label;

        private BigDecimal value;

        private String valueText;

        private String unit;
    }

    /**
     * 预警项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertItem {

        private Long id;

        private String title;

        private LocalDateTime happenedAt;

        private BigDecimal currentValue;

        private String currentValueText;

        private BigDecimal limitValue;

        private String limitValueText;

        private String unit;

        private String level;

        private String tone;

        private String description;
    }
}
