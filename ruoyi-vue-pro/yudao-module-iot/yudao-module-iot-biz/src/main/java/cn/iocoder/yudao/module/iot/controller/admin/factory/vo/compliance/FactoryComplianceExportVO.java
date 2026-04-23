package cn.iocoder.yudao.module.iot.controller.admin.factory.vo.compliance;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件说明：智慧工厂合规管理导出 VO 集合
 *
 * <p>说明：用于合规管理页面“导出报告”功能，
 * 按当前 Tab 输出真实数据库记录对应的 Excel 行结构。</p>
 *
 * @author GPT-5.4
 */
public class FactoryComplianceExportVO {

    /**
     * GMP 合规导出行
     */
    @Schema(description = "管理后台 - 合规管理 GMP 导出行")
    @Data
    @ExcelIgnoreUnannotated
    public static class GmpReportRow {

        @ExcelProperty("监测点位")
        private String pointName;

        @ExcelProperty("合规点数")
        private Integer compliantCount;

        @ExcelProperty("超标次数")
        private Integer exceedCount;

        @ExcelProperty("状态")
        private String status;

        @ExcelProperty("最后检查时间")
        private LocalDateTime lastCheckTime;

        @ExcelProperty("详情说明")
        private String detailText;
    }

    /**
     * 环保监测导出行
     */
    @Schema(description = "管理后台 - 合规管理环保监测导出行")
    @Data
    @ExcelIgnoreUnannotated
    public static class EnvironmentalReportRow {

        @ExcelProperty("监测点位")
        private String pointName;

        @ExcelProperty("当前值")
        private String currentValue;

        @ExcelProperty("标准值")
        private String standardValue;

        @ExcelProperty("告警次数")
        private Integer exceedCount;

        @ExcelProperty("状态")
        private String status;

        @ExcelProperty("最后检查时间")
        private LocalDateTime lastCheckTime;

        @ExcelProperty("详情说明")
        private String detailText;
    }

    /**
     * 批次追溯导出行
     */
    @Schema(description = "管理后台 - 合规管理批次追溯导出行")
    @Data
    @ExcelIgnoreUnannotated
    public static class BatchTraceReportRow {

        @ExcelProperty("批次编号")
        private String batchCode;

        @ExcelProperty("产品名称")
        private String productName;

        @ExcelProperty("检查点数")
        private Integer checkpointCount;

        @ExcelProperty("异常数")
        private Integer issueCount;

        @ExcelProperty("状态")
        private String status;

        @ExcelProperty("最后检查时间")
        private LocalDateTime lastCheckTime;

        @ExcelProperty("详情说明")
        private String detailText;
    }
}
