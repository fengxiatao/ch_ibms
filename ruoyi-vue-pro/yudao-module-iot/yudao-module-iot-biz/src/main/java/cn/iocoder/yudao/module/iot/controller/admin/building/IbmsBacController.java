package cn.iocoder.yudao.module.iot.controller.admin.building;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.*;
import cn.iocoder.yudao.module.iot.service.building.IbmsBacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 楼宇自控 Controller
 *
 * @author 智慧楼宇系统
 */
@Tag(name = "管理后台 - 楼宇自控")
@RestController
@RequestMapping("/iot/building/bac")
@Validated
public class IbmsBacController {

    @Resource
    private IbmsBacService bacService;

    // ======================= 暖通设备管理 =======================

    @GetMapping("/hvac/page")
    @Operation(summary = "获得暖通设备分页")
    @PreAuthorize("@ss.hasPermission('iot:building-bac:query')")
    public CommonResult<PageResult<IbmsHvacDeviceRespVO>> getHvacDevicePage(@Valid IbmsHvacDevicePageReqVO pageReqVO) {
        PageResult<IbmsHvacDeviceDO> pageResult = bacService.getHvacDevicePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsHvacDeviceRespVO.class));
    }

    @GetMapping("/hvac/get")
    @Operation(summary = "获得暖通设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-bac:query')")
    public CommonResult<IbmsHvacDeviceRespVO> getHvacDevice(@RequestParam("id") Long id) {
        IbmsHvacDeviceDO device = bacService.getHvacDevice(id);
        return success(BeanUtils.toBean(device, IbmsHvacDeviceRespVO.class));
    }

    @PutMapping("/hvac/control")
    @Operation(summary = "控制暖通设备启停")
    @PreAuthorize("@ss.hasPermission('iot:building-bac:control')")
    public CommonResult<Boolean> controlHvacDevice(
            @RequestParam("id") Long id,
            @RequestParam("runningStatus") Integer runningStatus,
            @RequestParam("operator") String operator) {
        bacService.controlHvacDevice(id, runningStatus, operator);
        return success(true);
    }

    @PutMapping("/hvac/set-params")
    @Operation(summary = "设置暖通设备参数")
    @PreAuthorize("@ss.hasPermission('iot:building-bac:control')")
    public CommonResult<Boolean> setHvacDeviceParams(
            @RequestParam("id") Long id,
            @RequestParam(value = "runMode", required = false) Integer runMode,
            @RequestParam(value = "setTemperature", required = false) BigDecimal setTemperature,
            @RequestParam(value = "fanSpeed", required = false) Integer fanSpeed,
            @RequestParam("operator") String operator) {
        bacService.setHvacDeviceParams(id, runMode, setTemperature, fanSpeed, operator);
        return success(true);
    }

    // ======================= 给排水设备管理 =======================

    @GetMapping("/water/page")
    @Operation(summary = "获得给排水设备分页")
    @PreAuthorize("@ss.hasPermission('iot:building-bac:query')")
    public CommonResult<PageResult<IbmsWaterDeviceRespVO>> getWaterDevicePage(@Valid IbmsWaterDevicePageReqVO pageReqVO) {
        PageResult<IbmsWaterDeviceDO> pageResult = bacService.getWaterDevicePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsWaterDeviceRespVO.class));
    }

    @GetMapping("/water/get")
    @Operation(summary = "获得给排水设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-bac:query')")
    public CommonResult<IbmsWaterDeviceRespVO> getWaterDevice(@RequestParam("id") Long id) {
        IbmsWaterDeviceDO device = bacService.getWaterDevice(id);
        return success(BeanUtils.toBean(device, IbmsWaterDeviceRespVO.class));
    }

    @PutMapping("/water/control")
    @Operation(summary = "控制给排水设备启停")
    @PreAuthorize("@ss.hasPermission('iot:building-bac:control')")
    public CommonResult<Boolean> controlWaterDevice(
            @RequestParam("id") Long id,
            @RequestParam("runningStatus") Integer runningStatus,
            @RequestParam("operator") String operator) {
        bacService.controlWaterDevice(id, runningStatus, operator);
        return success(true);
    }

    // ======================= 告警管理 =======================

    @GetMapping("/alarm/page")
    @Operation(summary = "获得楼宇自控告警分页")
    @PreAuthorize("@ss.hasPermission('iot:building-bac:query')")
    public CommonResult<PageResult<IbmsBacAlarmRespVO>> getAlarmPage(@Valid IbmsBacAlarmPageReqVO pageReqVO) {
        PageResult<IbmsBacAlarmDO> pageResult = bacService.getAlarmPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsBacAlarmRespVO.class));
    }

    @PutMapping("/alarm/handle")
    @Operation(summary = "处理告警")
    @PreAuthorize("@ss.hasPermission('iot:building-bac:update')")
    public CommonResult<Boolean> handleAlarm(
            @RequestParam("id") Long id,
            @RequestParam("handler") String handler,
            @RequestParam(value = "handleRemark", required = false) String handleRemark) {
        bacService.handleAlarm(id, handler, handleRemark);
        return success(true);
    }

    // ======================= 系统日志 =======================

    @GetMapping("/system-log/page")
    @Operation(summary = "获得系统日志分页")
    @PreAuthorize("@ss.hasPermission('iot:building-bac:query')")
    public CommonResult<PageResult<IbmsBacSystemLogRespVO>> getSystemLogPage(@Valid IbmsBacSystemLogPageReqVO pageReqVO) {
        PageResult<IbmsBacSystemLogDO> pageResult = bacService.getSystemLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsBacSystemLogRespVO.class));
    }

    // ======================= 统计分析 =======================

    @GetMapping("/statistics")
    @Operation(summary = "获得楼宇自控统计数据")
    @PreAuthorize("@ss.hasPermission('iot:building-bac:query')")
    public CommonResult<IbmsBacStatisticsVO> getStatistics() {
        return success(bacService.getStatistics());
    }

}
