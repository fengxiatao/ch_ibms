package cn.iocoder.yudao.module.iot.controller.admin.factory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.CloudDefenseOverviewRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.FactoryEnvironmentalOverviewRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.FactoryOverviewRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.report.FactoryReportExportVO;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.report.FactoryReportVO;
import cn.iocoder.yudao.module.iot.service.factory.CloudDefenseService;
import cn.iocoder.yudao.module.iot.service.factory.FactoryEnvironmentalService;
import cn.iocoder.yudao.module.iot.service.factory.FactoryOverviewService;
import cn.iocoder.yudao.module.iot.service.factory.FactoryReportService;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

@Tag(name = "管理后台 - 智慧工厂总览")
@RestController
@RequestMapping("/iot/factory")
@Validated
public class FactoryOverviewController {

    @Resource
    private FactoryOverviewService factoryOverviewService;
    @Resource
    private CloudDefenseService cloudDefenseService;
    @Resource
    private FactoryEnvironmentalService factoryEnvironmentalService;
    @Resource
    private FactoryReportService factoryReportService;

    @GetMapping("/overview")
    @Operation(summary = "获取智慧工厂总览数据")
    @PermitAll
    public CommonResult<FactoryOverviewRespVO> getOverview() {
        return success(factoryOverviewService.getOverview());
    }

    @GetMapping("/cloud-defense/overview")
    @Operation(summary = "获取立体化云防总览数据")
    @PermitAll
    public CommonResult<CloudDefenseOverviewRespVO> getCloudDefenseOverview() {
        return success(cloudDefenseService.getOverview());
    }

    @GetMapping("/environmental/dashboard")
    @Operation(summary = "获取工厂环保监测工作台数据")
    @PermitAll
    public CommonResult<FactoryEnvironmentalOverviewRespVO> getEnvironmentalDashboard() {
        return success(factoryEnvironmentalService.getDashboard());
    }

    /**
     * 获取报表中心工作台数据
     *
     * @param reqVO 查询参数
     * @return 工作台数据
     */
    @GetMapping("/report/dashboard")
    @Operation(summary = "获取报表中心工作台数据")
    @PermitAll
    public CommonResult<FactoryReportVO.ReportDashboardRespVO> getReportDashboard(
            @Valid FactoryReportVO.ReportDashboardReqVO reqVO
    ) {
        return success(factoryReportService.getDashboard(reqVO));
    }

    /**
     * 手动生成报表
     *
     * @param reqVO 生成参数
     * @return 生成结果
     */
    @PostMapping("/report/generate")
    @Operation(summary = "生成报表")
    @PermitAll
    public CommonResult<FactoryReportVO.ReportGenerateRespVO> generateReport(
            @Valid @RequestBody FactoryReportVO.ReportGenerateReqVO reqVO
    ) {
        return success(factoryReportService.generateReport(reqVO));
    }

    /**
     * 获取报表预览
     *
     * @param reqVO 查询参数
     * @return 预览数据
     */
    @GetMapping("/report/preview")
    @Operation(summary = "获取报表预览")
    @PermitAll
    public CommonResult<FactoryReportVO.ReportPreviewRespVO> getReportPreview(
            @Valid FactoryReportVO.ReportPreviewReqVO reqVO
    ) {
        return success(factoryReportService.getPreview(reqVO));
    }

    /**
     * 下载报表
     *
     * @param reqVO 查询参数
     * @param response 响应对象
     * @throws IOException Excel 写出异常
     */
    @GetMapping("/report/download")
    @Operation(summary = "下载报表")
    @PermitAll
    @ApiAccessLog(operateType = EXPORT)
    public void downloadReport(
            @Valid FactoryReportVO.ReportDownloadReqVO reqVO,
            HttpServletResponse response
    ) throws IOException {
        List<FactoryReportExportVO.ReportExportRow> rows = factoryReportService.getExportRows(reqVO.getId());
        ExcelUtils.write(
                response,
                factoryReportService.getExportFileName(reqVO.getId()),
                "报表中心",
                FactoryReportExportVO.ReportExportRow.class,
                rows
        );
    }
}
