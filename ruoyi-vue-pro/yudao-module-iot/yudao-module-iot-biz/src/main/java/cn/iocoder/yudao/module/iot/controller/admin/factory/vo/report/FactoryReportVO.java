package cn.iocoder.yudao.module.iot.controller.admin.factory.vo.report;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件说明：智慧工厂报表中心 VO 集合
 *
 * <p>说明：报表中心页面要求“严格原型克隆 + 真实数据库驱动”，
 * 因此将首屏聚合、预览与生成等请求/响应结构统一收敛于该 VO 文件。</p>
 *
 * @author GPT-5.4
 */
public class FactoryReportVO {

    /**
     * 报表中心查询参数
     */
    @Data
    public static class ReportDashboardReqVO extends PageParam {

        @Schema(description = "分类（原型固定 Tab）", example = "日报表")
        private String category;

        @Schema(description = "关键字（模板名/报表名模糊匹配）")
        private String keyword;
    }

    /**
     * 报表中心响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportDashboardRespVO {

        @Schema(description = "数据更新时间")
        private LocalDateTime updatedAt;

        @Schema(description = "顶部统计卡")
        private List<ReportMetricItem> metrics;

        @Schema(description = "原型分类 Tab 列表")
        private List<ReportCategoryOption> categories;

        @Schema(description = "报表模板卡片列表")
        private List<ReportTemplateCardItem> templates;

        @Schema(description = "最近生成记录")
        private List<ReportRecordRowItem> recentRecords;
    }

    /**
     * 顶部统计卡
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportMetricItem {

        private String key;
        private String title;
        private String value;
        private String unit;
        private String icon;
        private String theme;
    }

    /**
     * 分类 Tab 选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportCategoryOption {

        private String value;
        private String label;
    }

    /**
     * 模板卡片项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportTemplateCardItem {

        private Long id;
        private String templateName;
        private String category;
        private String description;
        private String status;
        private Long latestSuccessRecordId;

        private LocalDateTime lastGeneratedAt;
        private String lastStatus;

        private Boolean previewAvailable;
        private Boolean downloadAvailable;
        private Boolean generateAvailable;
    }

    /**
     * 最近生成记录行
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportRecordRowItem {

        private Long id;
        private String reportName;
        private String category;
        private LocalDateTime generatedAt;
        private String status;
        private String operatorName;
    }

    /**
     * 报表生成请求
     */
    @Data
    public static class ReportGenerateReqVO {

        @Schema(description = "模板主键", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "模板主键不能为空")
        private Long templateId;

        @Schema(description = "业务日期（可选，不传默认今天）", example = "2026-04-15")
        private LocalDate bizDate;
    }

    /**
     * 报表生成响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportGenerateRespVO {

        private Long recordId;
        private String status;
        private String reportName;
        private LocalDateTime generatedAt;
    }

    /**
     * 报表预览请求
     */
    @Data
    public static class ReportPreviewReqVO {

        @Schema(description = "记录主键", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "记录主键不能为空")
        private Long id;
    }

    /**
     * 报表预览响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportPreviewRespVO {

        private Long recordId;
        private String reportName;
        private String category;
        private LocalDate bizDate;
        private LocalDateTime generatedAt;
        private String status;
        private String operatorName;

        private Long templateId;
        private String templateName;
        private String templateDesc;
    }

    /**
     * 下载请求
     */
    @Data
    public static class ReportDownloadReqVO {

        @Schema(description = "记录主键", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "记录主键不能为空")
        private Long id;
    }
}
