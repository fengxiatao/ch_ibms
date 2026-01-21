package cn.iocoder.yudao.module.iot.controller.admin.changhui;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm.ChanghuiAlarmPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm.ChanghuiAlarmRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm.ChanghuiAlarmSaveReqVO;
import cn.iocoder.yudao.module.iot.service.changhui.alarm.ChanghuiAlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

/**
 * 长辉设备报警 Controller
 * 
 * <p>提供报警的保存、查询、确认等功能
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - 长辉设备报警")
@RestController
@RequestMapping("/iot/changhui/alarm")
@Validated
public class ChanghuiAlarmController {

    @Resource
    private ChanghuiAlarmService alarmService;

    @PostMapping("/save")
    @Operation(summary = "保存报警")
    @PreAuthorize("@ss.hasPermission('iot:changhui-alarm:create')")
    public CommonResult<Long> saveAlarm(@Valid @RequestBody ChanghuiAlarmSaveReqVO reqVO) {
        return success(alarmService.saveAlarm(reqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获取报警分页")
    @PreAuthorize("@ss.hasPermission('iot:changhui-alarm:query')")
    public CommonResult<PageResult<ChanghuiAlarmRespVO>> getAlarmPage(@Valid ChanghuiAlarmPageReqVO reqVO) {
        return success(alarmService.getAlarmPage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取报警详情")
    @Parameter(name = "id", description = "报警ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-alarm:query')")
    public CommonResult<ChanghuiAlarmRespVO> getAlarm(@RequestParam("id") Long id) {
        return success(alarmService.getAlarm(id));
    }

    @PutMapping("/acknowledge")
    @Operation(summary = "确认报警")
    @Parameter(name = "id", description = "报警ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-alarm:update')")
    public CommonResult<Boolean> acknowledgeAlarm(@RequestParam("id") Long id) {
        alarmService.acknowledgeAlarm(id, getLoginUserNickname());
        return success(true);
    }

    @GetMapping("/unacknowledged-count")
    @Operation(summary = "获取未确认报警数量")
    @PreAuthorize("@ss.hasPermission('iot:changhui-alarm:query')")
    public CommonResult<Long> getUnacknowledgedCount() {
        return success(alarmService.getUnacknowledgedCount());
    }

    @GetMapping("/unacknowledged-count-by-station")
    @Operation(summary = "获取指定设备的未确认报警数量")
    @Parameter(name = "stationCode", description = "测站编码", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-alarm:query')")
    public CommonResult<Long> getUnacknowledgedCountByStationCode(
            @RequestParam("stationCode") String stationCode) {
        return success(alarmService.getUnacknowledgedCountByStationCode(stationCode));
    }

    @DeleteMapping("/delete-by-device")
    @Operation(summary = "删除设备的所有报警")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-alarm:delete')")
    public CommonResult<Boolean> deleteAlarmByDeviceId(@RequestParam("deviceId") Long deviceId) {
        alarmService.deleteAlarmByDeviceId(deviceId);
        return success(true);
    }

    @DeleteMapping("/delete-by-station-code")
    @Operation(summary = "删除设备的所有报警（根据测站编码）")
    @Parameter(name = "stationCode", description = "测站编码", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-alarm:delete')")
    public CommonResult<Boolean> deleteAlarmByStationCode(@RequestParam("stationCode") String stationCode) {
        alarmService.deleteAlarmByStationCode(stationCode);
        return success(true);
    }

}
