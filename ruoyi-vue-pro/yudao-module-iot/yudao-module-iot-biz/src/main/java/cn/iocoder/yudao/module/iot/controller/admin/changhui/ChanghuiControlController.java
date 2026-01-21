package cn.iocoder.yudao.module.iot.controller.admin.changhui;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.control.*;
import cn.iocoder.yudao.module.iot.service.changhui.control.ChanghuiControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 长辉设备远程控制 Controller
 * 
 * <p>提供设备远程控制功能，包括模式切换、手动控制、自动控制等
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - 长辉设备远程控制")
@RestController
@RequestMapping("/iot/changhui/control")
@Validated
public class ChanghuiControlController {

    @Resource
    private ChanghuiControlService controlService;

    @PostMapping("/switch-mode")
    @Operation(summary = "模式切换")
    @PreAuthorize("@ss.hasPermission('iot:changhui-control:operate')")
    public CommonResult<String> switchMode(@Valid @RequestBody ChanghuiSwitchModeReqVO reqVO) {
        String requestId = controlService.switchMode(reqVO.getStationCode(), reqVO.getMode());
        return success(requestId);
    }

    @PostMapping("/manual")
    @Operation(summary = "手动控制")
    @PreAuthorize("@ss.hasPermission('iot:changhui-control:operate')")
    public CommonResult<String> manualControl(@Valid @RequestBody ChanghuiManualControlReqVO reqVO) {
        String requestId = controlService.manualControl(reqVO.getStationCode(), reqVO.getAction());
        return success(requestId);
    }

    @PostMapping("/auto")
    @Operation(summary = "自动控制")
    @PreAuthorize("@ss.hasPermission('iot:changhui-control:operate')")
    public CommonResult<String> autoControl(@Valid @RequestBody ChanghuiAutoControlReqVO reqVO) {
        String requestId = controlService.autoControl(
                reqVO.getStationCode(), reqVO.getControlMode(), reqVO.getTargetValue());
        return success(requestId);
    }

    @GetMapping("/logs")
    @Operation(summary = "获取控制日志分页")
    @PreAuthorize("@ss.hasPermission('iot:changhui-control:query')")
    public CommonResult<PageResult<ChanghuiControlLogRespVO>> getControlLogs(@Valid ChanghuiControlLogPageReqVO reqVO) {
        return success(controlService.getControlLogs(reqVO));
    }

    @DeleteMapping("/logs/delete-by-device")
    @Operation(summary = "删除设备的所有控制日志")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-control:delete')")
    public CommonResult<Boolean> deleteControlLogsByDeviceId(@RequestParam("deviceId") Long deviceId) {
        controlService.deleteControlLogsByDeviceId(deviceId);
        return success(true);
    }

    @DeleteMapping("/logs/delete-by-station-code")
    @Operation(summary = "删除设备的所有控制日志（根据测站编码）")
    @Parameter(name = "stationCode", description = "测站编码", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-control:delete')")
    public CommonResult<Boolean> deleteControlLogsByStationCode(@RequestParam("stationCode") String stationCode) {
        controlService.deleteControlLogsByStationCode(stationCode);
        return success(true);
    }

}
