package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.alarm.*;
import cn.iocoder.yudao.module.iot.service.access.AccessAlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁告警 Controller
 *
 * @author 智能化系统
 */
@Tag(name = "管理后台 - 门禁告警")
@RestController
@RequestMapping("/iot/access-alarm")
@Validated
public class AccessAlarmController {

    @Resource
    private AccessAlarmService accessAlarmService;

    @GetMapping("/get")
    @Operation(summary = "获得门禁告警")
    @Parameter(name = "id", description = "告警ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-alarm:query')")
    public CommonResult<AccessAlarmRespVO> getAccessAlarm(@RequestParam("id") Long id) {
        AccessAlarmRespVO alarm = accessAlarmService.getAccessAlarm(id);
        return success(alarm);
    }

    @GetMapping("/page")
    @Operation(summary = "获得门禁告警分页")
    @PreAuthorize("@ss.hasPermission('iot:access-alarm:query')")
    public CommonResult<PageResult<AccessAlarmRespVO>> getAccessAlarmPage(@Valid AccessAlarmPageReqVO pageVO) {
        PageResult<AccessAlarmRespVO> pageResult = accessAlarmService.getAccessAlarmPage(pageVO);
        return success(pageResult);
    }

    @PutMapping("/handle")
    @Operation(summary = "处理门禁告警")
    @PreAuthorize("@ss.hasPermission('iot:access-alarm:update')")
    public CommonResult<Boolean> handleAccessAlarm(@Valid @RequestBody AccessAlarmHandleReqVO handleReqVO) {
        accessAlarmService.handleAccessAlarm(handleReqVO);
        return success(true);
    }

    @GetMapping("/statistics/type")
    @Operation(summary = "获取告警类型统计")
    @PreAuthorize("@ss.hasPermission('iot:access-alarm:query')")
    public CommonResult<java.util.List<java.util.Map<String, Object>>> getAlarmTypeStatistics(
            @RequestParam(required = false) java.time.LocalDateTime startTime,
            @RequestParam(required = false) java.time.LocalDateTime endTime) {
        java.util.List<java.util.Map<String, Object>> statistics = 
            accessAlarmService.getAlarmTypeStatistics(startTime, endTime);
        return success(statistics);
    }

}

