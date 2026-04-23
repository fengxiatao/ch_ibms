package cn.iocoder.yudao.module.iot.controller.admin.factory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.collaboration.FactoryCollaborationVO;
import cn.iocoder.yudao.module.iot.service.factory.FactoryCollaborationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 智慧工厂业务协同
 *
 * <p>统一承接生产协同、能源管理、设备管理、碳资产四大域工作台接口。</p>
 *
 * @author GPT-5.4
 */
@Tag(name = "管理后台 - 智慧工厂业务协同")
@RestController
@RequestMapping("/iot/factory/collaboration")
@Validated
public class FactoryCollaborationController {

    @Resource
    private FactoryCollaborationService factoryCollaborationService;

    /**
     * 获取生产协同工作台数据
     *
     * @param reqVO 查询参数
     * @return 工作台数据
     */
    @GetMapping("/production/dashboard")
    @Operation(summary = "获取生产协同工作台数据")
    @PermitAll
    public CommonResult<FactoryCollaborationVO.ProductionDashboardRespVO> getProductionDashboard(
            @Valid FactoryCollaborationVO.ProductionDashboardReqVO reqVO
    ) {
        return success(factoryCollaborationService.getProductionDashboard(reqVO));
    }

    /**
     * 获取批次追溯详情
     *
     * @param reqVO 查询参数
     * @return 批次详情
     */
    @GetMapping("/production/batch-trace/detail")
    @Operation(summary = "获取批次追溯详情")
    @PermitAll
    public CommonResult<FactoryCollaborationVO.ProductionBatchTraceDetailRespVO> getProductionBatchTraceDetail(
            @Valid FactoryCollaborationVO.ProductionBatchTraceDetailReqVO reqVO
    ) {
        return success(factoryCollaborationService.getProductionBatchTraceDetail(reqVO));
    }

    /**
     * 创建生产计划
     *
     * @param reqVO 创建请求
     * @return 计划主键
     */
    @PostMapping("/production/plan/create")
    @Operation(summary = "创建生产计划")
    @PermitAll
    public CommonResult<Long> createProductionPlan(
            @Valid @RequestBody FactoryCollaborationVO.ProductionPlanCreateReqVO reqVO
    ) {
        return success(factoryCollaborationService.createProductionPlan(reqVO));
    }

    /**
     * 更新生产计划状态
     *
     * @param reqVO 更新请求
     * @return 是否成功
     */
    @PutMapping("/production/plan/update-status")
    @Operation(summary = "更新生产计划状态")
    @PermitAll
    public CommonResult<Boolean> updateProductionPlanStatus(
            @Valid @RequestBody FactoryCollaborationVO.ProductionPlanStatusUpdateReqVO reqVO
    ) {
        factoryCollaborationService.updateProductionPlanStatus(reqVO);
        return success(true);
    }

    /**
     * 获取能源工作台数据
     *
     * @param reqVO 查询参数
     * @return 工作台数据
     */
    @GetMapping("/energy/dashboard")
    @Operation(summary = "获取能源工作台数据")
    @PermitAll
    public CommonResult<FactoryCollaborationVO.EnergyDashboardRespVO> getEnergyDashboard(
            @Valid FactoryCollaborationVO.EnergyDashboardReqVO reqVO
    ) {
        return success(factoryCollaborationService.getEnergyDashboard(reqVO));
    }

    /**
     * 更新节能建议处理状态
     *
     * @param reqVO 更新请求
     * @return 是否成功
     */
    @PutMapping("/energy/suggestion/handle")
    @Operation(summary = "更新节能建议处理状态")
    @PermitAll
    public CommonResult<Boolean> handleEnergySuggestion(
            @Valid @RequestBody FactoryCollaborationVO.EnergySuggestionHandleReqVO reqVO
    ) {
        factoryCollaborationService.handleEnergySuggestion(reqVO);
        return success(true);
    }

    /**
     * 获取设备工作台数据
     *
     * @param reqVO 查询参数
     * @return 工作台数据
     */
    @GetMapping("/device/dashboard")
    @Operation(summary = "获取设备工作台数据")
    @PermitAll
    public CommonResult<FactoryCollaborationVO.DeviceDashboardRespVO> getDeviceDashboard(
            @Valid FactoryCollaborationVO.DeviceDashboardReqVO reqVO
    ) {
        return success(factoryCollaborationService.getDeviceDashboard(reqVO));
    }

    /**
     * 创建设备
     *
     * @param reqVO 创建请求
     * @return 设备主键
     */
    @PostMapping("/device/create")
    @Operation(summary = "创建设备")
    @PermitAll
    public CommonResult<Long> createDevice(
            @Valid @RequestBody FactoryCollaborationVO.DeviceCreateReqVO reqVO
    ) {
        return success(factoryCollaborationService.createDevice(reqVO));
    }

    /**
     * 创建维保计划
     *
     * @param reqVO 创建请求
     * @return 计划主键
     */
    @PostMapping("/device/maintenance-plan/create")
    @Operation(summary = "创建维保计划")
    @PermitAll
    public CommonResult<Long> createMaintenancePlan(
            @Valid @RequestBody FactoryCollaborationVO.MaintenancePlanCreateReqVO reqVO
    ) {
        return success(factoryCollaborationService.createMaintenancePlan(reqVO));
    }

    /**
     * 完成维保工单
     *
     * @param reqVO 完成请求
     * @return 是否成功
     */
    @PutMapping("/device/maintenance-order/complete")
    @Operation(summary = "完成维保工单")
    @PermitAll
    public CommonResult<Boolean> completeMaintenanceOrder(
            @Valid @RequestBody FactoryCollaborationVO.MaintenanceOrderCompleteReqVO reqVO
    ) {
        factoryCollaborationService.completeMaintenanceOrder(reqVO);
        return success(true);
    }

    /**
     * 获取碳资产工作台数据
     *
     * @param reqVO 查询参数
     * @return 工作台数据
     */
    @GetMapping("/carbon/dashboard")
    @Operation(summary = "获取碳资产工作台数据")
    @PermitAll
    public CommonResult<FactoryCollaborationVO.CarbonDashboardRespVO> getCarbonDashboard(
            @Valid FactoryCollaborationVO.CarbonDashboardReqVO reqVO
    ) {
        return success(factoryCollaborationService.getCarbonDashboard(reqVO));
    }

    /**
     * 创建碳交易登记
     *
     * @param reqVO 创建请求
     * @return 交易主键
     */
    @PostMapping("/carbon/trade/create")
    @Operation(summary = "创建碳交易登记")
    @PermitAll
    public CommonResult<Long> createCarbonTrade(
            @Valid @RequestBody FactoryCollaborationVO.CarbonTradeCreateReqVO reqVO
    ) {
        return success(factoryCollaborationService.createCarbonTrade(reqVO));
    }

}
