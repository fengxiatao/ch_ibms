package cn.iocoder.yudao.module.iot.controller.admin.factory.vo.compliance;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件说明：智慧工厂合规管理 VO 集合
 *
 * <p>说明：合规管理页面包含 GMP 合规、环保监测、批次追溯三个独立 Tab，
 * 因此将其请求与响应结构聚合到独立 VO 文件，避免与业务协同追溯模型混用。</p>
 *
 * @author GPT-5.4
 */
public class FactoryComplianceVO {

    /**
     * 合规管理查询参数
     */
    @Data
    public static class ComplianceDashboardReqVO extends PageParam {

        @Schema(description = "当前 Tab", example = "gmp")
        private String tab;

        @Schema(description = "关键字")
        private String keyword;
    }

    /**
     * 合规历史查询参数
     */
    @Data
    public static class ComplianceHistoryReqVO extends PageParam {

        @Schema(description = "当前 Tab", requiredMode = Schema.RequiredMode.REQUIRED, example = "gmp")
        @NotBlank(message = "当前 Tab 不能为空")
        private String tab;
    }

    /**
     * 顶部指标卡
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceMetricItem {

        private String key;
        private String title;
        private String value;
        private String unit;
        private String icon;
        private String theme;
    }

    /**
     * 合规管理总览响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceDashboardRespVO {

        private LocalDateTime updatedAt;
        private String tab;
        private List<ComplianceMetricItem> metrics;
        private ComplianceGmpOverview gmpOverview;
        private ComplianceEnvironmentalOverview environmentalOverview;
        private ComplianceBatchOverview batchOverview;
    }

    /**
     * 历史记录响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceHistoryRespVO {

        private LocalDateTime updatedAt;
        private String tab;
        private List<ComplianceHistoryItem> historyList;
    }

    /**
     * 概览卡项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceSummaryCardItem {

        private Long id;
        private String title;
        private String subtitle;
        private BigDecimal progressRate;
        private String progressText;
        private String tone;
    }

    /**
     * GMP 合规总览
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceGmpOverview {

        private List<ComplianceSummaryCardItem> regionCards;
        private List<GmpInspectionRowItem> detailList;
    }

    /**
     * GMP 检查明细行
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GmpInspectionRowItem {

        private Long id;
        private String pointName;
        private Integer compliantCount;
        private Integer exceedCount;
        private String status;
        private LocalDateTime lastCheckTime;
        private String detailText;
    }

    /**
     * 环保监测总览
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceEnvironmentalOverview {

        private List<ComplianceSummaryCardItem> regionCards;
        private List<EnvironmentalMonitorRowItem> detailList;
    }

    /**
     * 环保监测明细行
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnvironmentalMonitorRowItem {

        private Long id;
        private String pointName;
        private BigDecimal currentValue;
        private BigDecimal standardValue;
        private Integer exceedCount;
        private String status;
        private LocalDateTime lastCheckTime;
        private String detailText;
        private String unit;
    }

    /**
     * 合规批次追溯总览
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceBatchOverview {

        private List<ComplianceSummaryCardItem> batchCards;
        private List<ComplianceBatchTraceRowItem> detailList;
    }

    /**
     * 合规批次追溯明细行
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceBatchTraceRowItem {

        private Long id;
        private String batchCode;
        private String productName;
        private Integer checkpointCount;
        private Integer issueCount;
        private String status;
        private LocalDateTime lastCheckTime;
        private String detailText;
    }

    /**
     * 合规历史项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceHistoryItem {

        private Long id;
        private String eventType;
        private String title;
        private String status;
        private String operatorName;
        private String description;
        private LocalDateTime happenedAt;
    }
}
