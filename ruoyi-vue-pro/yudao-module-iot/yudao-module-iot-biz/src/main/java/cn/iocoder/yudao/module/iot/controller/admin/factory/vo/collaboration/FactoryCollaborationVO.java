package cn.iocoder.yudao.module.iot.controller.admin.factory.vo.collaboration;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - 智慧工厂业务协同 VO 集合
 *
 * <p>说明：为减少本次四大域工作台实现中的重复样板类，
 * 将请求与响应 VO 统一收敛到单一文件中，便于维护与联调。</p>
 *
 * @author GPT-5.4
 */
public class FactoryCollaborationVO {

    /**
     * 业务协同顶部指标卡
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricCardItem {

        @Schema(description = "唯一键")
        private String key;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "展示值")
        private String value;

        @Schema(description = "单位")
        private String unit;

        @Schema(description = "补充说明")
        private String hint;

        @Schema(description = "趋势说明")
        private String trend;

        @Schema(description = "图标")
        private String icon;

        @Schema(description = "主题色")
        private String theme;
    }

    /**
     * 生产协同工作台查询参数
     */
    @Data
    public static class ProductionDashboardReqVO extends PageParam {

        @Schema(description = "二级 Tab", example = "production-plan")
        private String subTab;

        @Schema(description = "关键字")
        private String keyword;
    }

    /**
     * 生产计划新增请求
     */
    @Data
    public static class ProductionPlanCreateReqVO {

        @Schema(description = "计划编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "计划编号不能为空")
        private String planCode;

        @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "产品名称不能为空")
        private String productName;

        @Schema(description = "批次编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "批次编号不能为空")
        private String batchCode;

        @Schema(description = "产线名称", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "产线名称不能为空")
        private String lineName;

        @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "计划数量不能为空")
        private Integer plannedQuantity;

        @Schema(description = "操作员", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "操作员不能为空")
        private String operatorName;

        @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "开始时间不能为空")
        private LocalDateTime plannedStartTime;
    }

    /**
     * 生产计划状态更新请求
     */
    @Data
    public static class ProductionPlanStatusUpdateReqVO {

        @Schema(description = "计划主键", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "计划主键不能为空")
        private Long id;

        @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "状态不能为空")
        private String status;

        @Schema(description = "进度百分比", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "进度不能为空")
        private Integer progress;

        @Schema(description = "备注")
        private String remark;
    }

    /**
     * 生产协同工作台响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionDashboardRespVO {

        @Schema(description = "数据更新时间")
        private LocalDateTime updatedAt;

        @Schema(description = "顶部指标卡")
        private List<MetricCardItem> metrics;

        @Schema(description = "生产计划列表")
        private List<ProductionPlanItem> planList;

        @Schema(description = "批次追踪列表")
        private List<ProductionBatchItem> batchList;

        @Schema(description = "生产关联告警")
        private List<ProductionAlertItem> alertList;
    }

    /**
     * 批次追溯详情查询参数
     */
    @Data
    public static class ProductionBatchTraceDetailReqVO {

        @Schema(description = "批次追踪主键", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "批次追踪主键不能为空")
        private Long batchId;
    }

    /**
     * 批次追溯详情响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionBatchTraceDetailRespVO {

        @Schema(description = "批次概要")
        private ProductionBatchTraceSummary summary;

        @Schema(description = "环境信息")
        private List<BatchEnvironmentRecordItem> environmentRecords;

        @Schema(description = "质量信息")
        private List<BatchQualityRecordItem> qualityRecords;

        @Schema(description = "人员记录")
        private List<BatchPersonnelRecordItem> personnelRecords;

        @Schema(description = "设备记录")
        private List<BatchDeviceRecordItem> deviceRecords;

        @Schema(description = "原料记录")
        private List<BatchMaterialRecordItem> materialRecords;
    }

    /**
     * 批次追溯概要
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionBatchTraceSummary {

        private Long id;
        private Long planId;
        private String planCode;
        private String batchCode;
        private String productName;
        private String lineName;
        private String operatorName;
        private String currentProcess;
        private String currentLocation;
        private Integer plannedQuantity;
        private Integer completedQuantity;
        private BigDecimal yieldRate;
        private String status;
        private LocalDateTime plannedStartTime;
        private LocalDateTime updatedAt;
    }

    /**
     * 生产计划行
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionPlanItem {

        private Long id;
        private String planCode;
        private String productName;
        private String batchCode;
        private Integer plannedQuantity;
        private String operatorName;
        private String lineName;
        private LocalDateTime plannedStartTime;
        private String status;
        private Integer progress;
    }

    /**
     * 批次追踪行
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionBatchItem {

        private Long id;
        private Long planId;
        private String batchCode;
        private String productName;
        private String currentProcess;
        private String currentLocation;
        private Integer completedQuantity;
        private BigDecimal yieldRate;
        private String status;
        private LocalDateTime updatedAt;
    }

    /**
     * 生产关联告警卡片
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionAlertItem {

        private Long id;
        private String alertTitle;
        private String levelLabel;
        private String lineName;
        private String status;
        private String handlerName;
        private LocalDateTime happenedAt;
    }

    /**
     * 批次环境记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchEnvironmentRecordItem {

        private Long id;
        private LocalDateTime recordTime;
        private BigDecimal temperatureValue;
        private BigDecimal humidityValue;
        private BigDecimal pressureValue;
        private BigDecimal phValue;
        private String cleanLevel;
        private String recorderName;
    }

    /**
     * 批次质量记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchQualityRecordItem {

        private Long id;
        private String sampleName;
        private String inspectionItem;
        private String standardValue;
        private String measuredValue;
        private String resultStatus;
        private LocalDateTime recordTime;
        private String inspectorName;
    }

    /**
     * 批次人员记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchPersonnelRecordItem {

        private Long id;
        private String roleName;
        private String staffName;
        private String operationName;
        private String workstationName;
        private LocalDateTime recordTime;
        private Integer durationMinutes;
        private String remark;
    }

    /**
     * 批次设备记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchDeviceRecordItem {

        private Long id;
        private String deviceCode;
        private String deviceName;
        private String operationName;
        private String runningStatus;
        private String parameterSummary;
        private LocalDateTime recordTime;
        private String operatorName;
    }

    /**
     * 批次原料记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchMaterialRecordItem {

        private Long id;
        private String materialCode;
        private String materialName;
        private String materialType;
        private String materialBatchNo;
        private BigDecimal plannedQuantity;
        private BigDecimal actualQuantity;
        private String unit;
        private String feederName;
        private LocalDateTime recordTime;
    }

    /**
     * 能源工作台查询参数
     */
    @Data
    public static class EnergyDashboardReqVO {

        @Schema(description = "二级 Tab", example = "overview")
        private String subTab;

        @Schema(description = "时间粒度 day/week/month", example = "day")
        private String dateMode;

        @Schema(description = "统计日期")
        private LocalDate statDate;
    }

    /**
     * 节能建议处理请求
     */
    @Data
    public static class EnergySuggestionHandleReqVO {

        @Schema(description = "建议主键", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "建议主键不能为空")
        private Long id;

        @Schema(description = "处理状态", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "处理状态不能为空")
        private String status;
    }

    /**
     * 能源工作台响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnergyDashboardRespVO {

        private LocalDateTime updatedAt;
        private String subTab;
        private List<MetricCardItem> metrics;
        private List<EnergyTrendItem> trendList;
        private List<EnergyRankingItem> areaRanking;
        private List<EnergyRankingItem> deviceRanking;
        private List<EnergySuggestionItem> suggestionList;
    }

    /**
     * 能源趋势项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnergyTrendItem {

        private String label;
        private BigDecimal electricityValue;
        private BigDecimal waterValue;
        private BigDecimal gasValue;
        private BigDecimal currentValue;
    }

    /**
     * 能源排行项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnergyRankingItem {

        private String name;
        private BigDecimal value;
        private String unit;
        private String extraText;
    }

    /**
     * 节能建议项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnergySuggestionItem {

        private Long id;
        private String title;
        private String content;
        private String levelLabel;
        private String status;
    }

    /**
     * 设备工作台查询参数
     */
    @Data
    public static class DeviceDashboardReqVO extends PageParam {

        @Schema(description = "二级 Tab", example = "device-list")
        private String subTab;

        @Schema(description = "关键字")
        private String keyword;

        @Schema(description = "选中设备主键")
        private Long selectedDeviceId;
    }

    /**
     * 设备新增请求
     */
    @Data
    public static class DeviceCreateReqVO {

        @Schema(description = "设备编码", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "设备编码不能为空")
        private String deviceCode;

        @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "设备名称不能为空")
        private String deviceName;

        @Schema(description = "设备类别", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "设备类别不能为空")
        private String categoryName;

        @Schema(description = "设备型号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "设备型号不能为空")
        private String modelName;

        @Schema(description = "所属区域", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "所属区域不能为空")
        private String areaName;

        @Schema(description = "在线状态")
        private Boolean online;

        @Schema(description = "运行状态")
        private String runningStatus;

        @Schema(description = "效率百分比")
        private BigDecimal efficiencyRate;
    }

    /**
     * 维保计划新增请求
     */
    @Data
    public static class MaintenancePlanCreateReqVO {

        @Schema(description = "设备主键", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "设备主键不能为空")
        private Long deviceId;

        @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "计划名称不能为空")
        private String planName;

        @Schema(description = "周期类型", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "周期类型不能为空")
        private String cycleType;

        @Schema(description = "下次执行日期", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "下次执行日期不能为空")
        private LocalDate nextExecuteDate;

        @Schema(description = "负责人", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "负责人不能为空")
        private String ownerName;
    }

    /**
     * 维保工单完成请求
     */
    @Data
    public static class MaintenanceOrderCompleteReqVO {

        @Schema(description = "工单主键", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "工单主键不能为空")
        private Long orderId;

        @Schema(description = "结果", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "处理结果不能为空")
        private String result;

        @Schema(description = "备注")
        private String remark;
    }

    /**
     * 设备工作台响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceDashboardRespVO {

        private LocalDateTime updatedAt;
        private String subTab;
        private List<MetricCardItem> metrics;
        private List<DeviceItem> deviceList;
        private List<MaintenancePlanItem> maintenancePlanList;
        private DeviceDetail detail;
    }

    /**
     * 设备列表项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceItem {

        private Long id;
        private String deviceCode;
        private String deviceName;
        private String categoryName;
        private String areaName;
        private Boolean online;
        private String runningStatus;
        private String healthStatus;
        private BigDecimal efficiencyRate;
        private String statusText;
    }

    /**
     * 设备详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceDetail {

        private Long id;
        private String deviceCode;
        private String deviceName;
        private String categoryName;
        private String modelName;
        private String areaName;
        private Boolean online;
        private String runningStatus;
        private String healthStatus;
        private BigDecimal efficiencyRate;
        private String ownerName;
        private LocalDate lastMaintenanceDate;
        private LocalDate nextMaintenanceDate;
        private String remark;
    }

    /**
     * 维保计划项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaintenancePlanItem {

        private Long id;
        private Long deviceId;
        private String planName;
        private String deviceName;
        private String cycleType;
        private LocalDate nextExecuteDate;
        private String ownerName;
        private String status;
        private Integer pendingOrderCount;
        private Long latestOrderId;
    }

    /**
     * 碳资产工作台查询参数
     */
    @Data
    public static class CarbonDashboardReqVO extends PageParam {

        @Schema(description = "二级 Tab", example = "carbon-overview")
        private String subTab;

        @Schema(description = "统计日期")
        private LocalDate statDate;
    }

    /**
     * 碳交易新增请求
     */
    @Data
    public static class CarbonTradeCreateReqVO {

        @Schema(description = "交易编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "交易编号不能为空")
        private String tradeCode;

        @Schema(description = "交易类型 BUY/SELL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "交易类型不能为空")
        private String tradeType;

        @Schema(description = "交易数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "交易数量不能为空")
        private BigDecimal quantity;

        @Schema(description = "单价", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "单价不能为空")
        private BigDecimal unitPrice;

        @Schema(description = "交易日期", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "交易日期不能为空")
        private LocalDate tradeDate;

        @Schema(description = "交易对手方", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "交易对手方不能为空")
        private String counterparty;
    }

    /**
     * 碳资产工作台响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarbonDashboardRespVO {

        private LocalDateTime updatedAt;
        private String subTab;
        private List<MetricCardItem> metrics;
        private List<CarbonTrendItem> trendList;
        private List<CarbonSourceItem> sourceList;
        private List<CarbonTradeItem> tradeList;
        private CarbonTargetCard targetCard;
    }

    /**
     * 碳排趋势项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarbonTrendItem {

        private String label;
        private BigDecimal emissionValue;
        private BigDecimal targetValue;
    }

    /**
     * 排放源项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarbonSourceItem {

        private Long id;
        private String sourceName;
        private String sourceType;
        private BigDecimal emissionValue;
        private BigDecimal proportion;
    }

    /**
     * 碳交易项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarbonTradeItem {

        private Long id;
        private String tradeCode;
        private String tradeType;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private LocalDate tradeDate;
        private String counterparty;
        private String status;
    }

    /**
     * 年度目标卡
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarbonTargetCard {

        private BigDecimal annualTargetValue;
        private BigDecimal emittedValue;
        private BigDecimal remainingValue;
        private BigDecimal completionRate;
    }

}
