package cn.iocoder.yudao.module.iot.controller.admin.factory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.compliance.FactoryComplianceVO;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.compliance.FactoryComplianceExportVO;
import cn.iocoder.yudao.module.iot.service.factory.FactoryComplianceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;

/**
 * 文件说明：管理后台 - 智慧工厂合规管理
 *
 * <p>说明：统一承接 GMP 合规、环保监测、合规批次追溯三个 Tab 的聚合接口。</p>
 *
 * @author GPT-5.4
 */
@Tag(name = "管理后台 - 智慧工厂合规管理")
@RestController
@RequestMapping("/iot/factory/collaboration/compliance")
@Validated
public class FactoryComplianceController {

    @Resource
    private FactoryComplianceService factoryComplianceService;

    /**
     * 获取合规管理工作台数据
     *
     * @param reqVO 查询参数
     * @return 工作台数据
     */
    @GetMapping("/dashboard")
    @Operation(summary = "获取合规管理工作台数据")
    @PermitAll
    public CommonResult<FactoryComplianceVO.ComplianceDashboardRespVO> getComplianceDashboard(
            @Valid FactoryComplianceVO.ComplianceDashboardReqVO reqVO
    ) {
        return success(factoryComplianceService.getComplianceDashboard(reqVO));
    }

    /**
     * 获取合规历史记录
     *
     * @param reqVO 查询参数
     * @return 历史数据
     */
    @GetMapping("/history")
    @Operation(summary = "获取合规历史记录")
    @PermitAll
    public CommonResult<FactoryComplianceVO.ComplianceHistoryRespVO> getComplianceHistory(
            @Valid FactoryComplianceVO.ComplianceHistoryReqVO reqVO
    ) {
        return success(factoryComplianceService.getComplianceHistory(reqVO));
    }

    /**
     * 导出合规管理报告
     *
     * @param reqVO 查询参数
     * @param response 响应对象
     * @throws IOException Excel 写出异常
     */
    @PostMapping("/report/export")
    @Operation(summary = "导出合规管理报告")
    @PermitAll
    @ApiAccessLog(operateType = EXPORT)
    public void exportComplianceReport(
            @Valid @RequestBody FactoryComplianceVO.ComplianceDashboardReqVO reqVO,
            HttpServletResponse response
    ) throws IOException {
        String tab = factoryComplianceService.resolveExportTab(reqVO.getTab());
        if (FactoryComplianceService.TAB_ENVIRONMENT.equals(tab)) {
            List<FactoryComplianceExportVO.EnvironmentalReportRow> rows =
                    factoryComplianceService.getEnvironmentalExportRows(reqVO);
            ExcelUtils.write(response, "合规管理-环保监测.xls", "环保监测", FactoryComplianceExportVO.EnvironmentalReportRow.class, rows);
            return;
        }
        if (FactoryComplianceService.TAB_BATCH_TRACE.equals(tab)) {
            List<FactoryComplianceExportVO.BatchTraceReportRow> rows =
                    factoryComplianceService.getBatchTraceExportRows(reqVO);
            ExcelUtils.write(response, "合规管理-批次追溯.xls", "批次追溯", FactoryComplianceExportVO.BatchTraceReportRow.class, rows);
            return;
        }
        List<FactoryComplianceExportVO.GmpReportRow> rows = factoryComplianceService.getGmpExportRows(reqVO);
        ExcelUtils.write(response, "合规管理-GMP合规.xls", "GMP合规", FactoryComplianceExportVO.GmpReportRow.class, rows);
    }
}
