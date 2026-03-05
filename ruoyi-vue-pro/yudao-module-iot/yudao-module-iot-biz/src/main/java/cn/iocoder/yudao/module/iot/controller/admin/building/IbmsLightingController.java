package cn.iocoder.yudao.module.iot.controller.admin.building;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.*;
import cn.iocoder.yudao.module.iot.service.building.IbmsLightingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 智能照明 Controller
 *
 * @author 智慧楼宇系统
 */
@Tag(name = "管理后台 - 智能照明")
@RestController
@RequestMapping("/iot/building/lighting")
@Validated
public class IbmsLightingController {

    @Resource
    private IbmsLightingService lightingService;

    // ======================= 回路管理 =======================

    @GetMapping("/circuit/page")
    @Operation(summary = "获得照明回路分页")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:query')")
    public CommonResult<PageResult<IbmsLightingCircuitRespVO>> getCircuitPage(@Valid IbmsLightingCircuitPageReqVO pageReqVO) {
        PageResult<IbmsLightingCircuitDO> pageResult = lightingService.getCircuitPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsLightingCircuitRespVO.class));
    }

    @GetMapping("/circuit/get")
    @Operation(summary = "获得照明回路")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:query')")
    public CommonResult<IbmsLightingCircuitRespVO> getCircuit(@RequestParam("id") Long id) {
        IbmsLightingCircuitDO circuit = lightingService.getCircuit(id);
        return success(BeanUtils.toBean(circuit, IbmsLightingCircuitRespVO.class));
    }

    @PutMapping("/circuit/control")
    @Operation(summary = "控制回路开关")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:control')")
    public CommonResult<Boolean> controlCircuit(
            @RequestParam("id") Long id,
            @RequestParam("status") Integer status,
            @RequestParam("operator") String operator) {
        lightingService.controlCircuit(id, status, operator);
        return success(true);
    }

    @PutMapping("/circuit/dim")
    @Operation(summary = "调节回路亮度")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:control')")
    public CommonResult<Boolean> dimCircuit(
            @RequestParam("id") Long id,
            @RequestParam("brightness") Integer brightness,
            @RequestParam("operator") String operator) {
        lightingService.dimCircuit(id, brightness, operator);
        return success(true);
    }

    // ======================= 场景管理 =======================

    @GetMapping("/scene/page")
    @Operation(summary = "获得照明场景分页")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:query')")
    public CommonResult<PageResult<IbmsLightingSceneRespVO>> getScenePage(@Valid IbmsLightingScenePageReqVO pageReqVO) {
        PageResult<IbmsLightingSceneDO> pageResult = lightingService.getScenePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsLightingSceneRespVO.class));
    }

    @GetMapping("/scene/list")
    @Operation(summary = "获得照明场景列表")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:query')")
    public CommonResult<List<IbmsLightingSceneRespVO>> getSceneSimpleList() {
        List<IbmsLightingSceneDO> list = lightingService.getSceneSimpleList();
        return success(BeanUtils.toBean(list, IbmsLightingSceneRespVO.class));
    }

    @PostMapping("/scene/execute")
    @Operation(summary = "执行场景")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:control')")
    public CommonResult<Boolean> executeScene(
            @RequestParam("id") Long id,
            @RequestParam("operator") String operator) {
        lightingService.executeScene(id, operator);
        return success(true);
    }

    // ======================= 定时任务 =======================

    @GetMapping("/schedule/page")
    @Operation(summary = "获得定时任务分页")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:query')")
    public CommonResult<PageResult<IbmsLightingScheduleRespVO>> getSchedulePage(@Valid IbmsLightingSchedulePageReqVO pageReqVO) {
        PageResult<IbmsLightingScheduleDO> pageResult = lightingService.getSchedulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsLightingScheduleRespVO.class));
    }

    @PutMapping("/schedule/enable")
    @Operation(summary = "启用/禁用定时任务")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:update')")
    public CommonResult<Boolean> updateScheduleEnabled(
            @RequestParam("id") Long id,
            @RequestParam("enabled") Boolean enabled) {
        lightingService.updateScheduleEnabled(id, enabled);
        return success(true);
    }

    // ======================= 设备管理 =======================

    @GetMapping("/gateway/page")
    @Operation(summary = "获得照明网关分页")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:query')")
    public CommonResult<PageResult<IbmsLightingGatewayRespVO>> getGatewayPage(@Valid IbmsLightingDevicePageReqVO pageReqVO) {
        PageResult<IbmsLightingGatewayDO> pageResult = lightingService.getGatewayPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsLightingGatewayRespVO.class));
    }

    @GetMapping("/controller/page")
    @Operation(summary = "获得照明控制器分页")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:query')")
    public CommonResult<PageResult<IbmsLightingControllerRespVO>> getControllerPage(@Valid IbmsLightingDevicePageReqVO pageReqVO) {
        PageResult<IbmsLightingControllerDO> pageResult = lightingService.getControllerPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsLightingControllerRespVO.class));
    }

    // ======================= 操作日志 =======================

    @GetMapping("/operation-log/page")
    @Operation(summary = "获得操作日志分页")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:query')")
    public CommonResult<PageResult<IbmsLightingOperationLogRespVO>> getOperationLogPage(@Valid IbmsLightingOperationLogPageReqVO pageReqVO) {
        PageResult<IbmsLightingOperationLogDO> pageResult = lightingService.getOperationLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsLightingOperationLogRespVO.class));
    }

    // ======================= 告警管理 =======================

    @GetMapping("/alarm/page")
    @Operation(summary = "获得照明告警分页")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:query')")
    public CommonResult<PageResult<IbmsLightingAlarmRespVO>> getAlarmPage(@Valid IbmsLightingAlarmPageReqVO pageReqVO) {
        PageResult<IbmsLightingAlarmDO> pageResult = lightingService.getAlarmPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsLightingAlarmRespVO.class));
    }

    @PutMapping("/alarm/handle")
    @Operation(summary = "处理告警")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:update')")
    public CommonResult<Boolean> handleAlarm(
            @RequestParam("id") Long id,
            @RequestParam("handler") String handler,
            @RequestParam(value = "handleRemark", required = false) String handleRemark) {
        lightingService.handleAlarm(id, handler, handleRemark);
        return success(true);
    }

    // ======================= 统计分析 =======================

    @GetMapping("/statistics")
    @Operation(summary = "获得智能照明统计数据")
    @PreAuthorize("@ss.hasPermission('iot:building-lighting:query')")
    public CommonResult<IbmsLightingStatisticsVO> getStatistics() {
        return success(lightingService.getStatistics());
    }

}
