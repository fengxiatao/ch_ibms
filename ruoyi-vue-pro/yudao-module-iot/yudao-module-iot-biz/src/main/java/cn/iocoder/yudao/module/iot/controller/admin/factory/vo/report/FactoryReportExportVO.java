package cn.iocoder.yudao.module.iot.controller.admin.factory.vo.report;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文件说明：智慧工厂报表中心导出 VO
 *
 * <p>说明：用于报表中心“下载”动作输出真实记录对应的 Excel 行结构。</p>
 *
 * @author GPT-5.4
 */
public class FactoryReportExportVO {

    /**
     * 报表导出行
     */
    @Schema(description = "管理后台 - 工厂报表中心导出行")
    @Data
    @ExcelIgnoreUnannotated
    public static class ReportExportRow {

        @ExcelProperty("报表名称")
        private String reportName;

        @ExcelProperty("报表类型")
        private String reportCategory;

        @ExcelProperty("模板名称")
        private String templateName;

        @ExcelProperty("模板说明")
        private String templateDesc;

        @ExcelProperty("状态")
        private String status;

        @ExcelProperty("业务日期")
        private LocalDate bizDate;

        @ExcelProperty("生成时间")
        private LocalDateTime generatedAt;

        @ExcelProperty("操作人")
        private String operatorName;
    }
}

