package cn.iocoder.yudao.module.iot.controller.admin.building;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.env.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvAlarmDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvDataRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvSensorDO;
import cn.iocoder.yudao.module.iot.service.building.IbmsEnvService;
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
 * 环境监测 Controller
 *
 * @author 智慧楼宇系统
 */
@Tag(name = "管理后台 - 环境监测")
@RestController
@RequestMapping("/iot/building/env")
@Validated
public class IbmsEnvController {

    @Resource
    private IbmsEnvService envService;

    // ======================= 传感器管理 =======================

    @PostMapping("/sensor/create")
    @Operation(summary = "创建环境传感器")
    @PreAuthorize("@ss.hasPermission('iot:building-env:create')")
    public CommonResult<Long> createEnvSensor(@Valid @RequestBody IbmsEnvSensorSaveReqVO createReqVO) {
        return success(envService.createEnvSensor(createReqVO));
    }

    @PutMapping("/sensor/update")
    @Operation(summary = "更新环境传感器")
    @PreAuthorize("@ss.hasPermission('iot:building-env:update')")
    public CommonResult<Boolean> updateEnvSensor(@Valid @RequestBody IbmsEnvSensorSaveReqVO updateReqVO) {
        envService.updateEnvSensor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/sensor/delete")
    @Operation(summary = "删除环境传感器")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-env:delete')")
    public CommonResult<Boolean> deleteEnvSensor(@RequestParam("id") Long id) {
        envService.deleteEnvSensor(id);
        return success(true);
    }

    @GetMapping("/sensor/get")
    @Operation(summary = "获得环境传感器")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-env:query')")
    public CommonResult<IbmsEnvSensorRespVO> getEnvSensor(@RequestParam("id") Long id) {
        IbmsEnvSensorDO sensor = envService.getEnvSensor(id);
        return success(BeanUtils.toBean(sensor, IbmsEnvSensorRespVO.class));
    }

    @GetMapping("/sensor/page")
    @Operation(summary = "获得环境传感器分页")
    @PreAuthorize("@ss.hasPermission('iot:building-env:query')")
    public CommonResult<PageResult<IbmsEnvSensorRespVO>> getEnvSensorPage(@Valid IbmsEnvSensorPageReqVO pageReqVO) {
        PageResult<IbmsEnvSensorDO> pageResult = envService.getEnvSensorPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsEnvSensorRespVO.class));
    }

    @GetMapping("/sensor/list")
    @Operation(summary = "获得环境传感器列表")
    @PreAuthorize("@ss.hasPermission('iot:building-env:query')")
    public CommonResult<List<IbmsEnvSensorRespVO>> getEnvSensorList(@Valid IbmsEnvSensorPageReqVO reqVO) {
        List<IbmsEnvSensorDO> list = envService.getEnvSensorList(reqVO);
        return success(BeanUtils.toBean(list, IbmsEnvSensorRespVO.class));
    }

    // ======================= 数据记录 =======================

    @GetMapping("/data/page")
    @Operation(summary = "获得环境数据记录分页")
    @PreAuthorize("@ss.hasPermission('iot:building-env:query')")
    public CommonResult<PageResult<IbmsEnvDataRecordRespVO>> getEnvDataRecordPage(@Valid IbmsEnvDataRecordPageReqVO pageReqVO) {
        PageResult<IbmsEnvDataRecordDO> pageResult = envService.getEnvDataRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsEnvDataRecordRespVO.class));
    }

    @GetMapping("/data/latest")
    @Operation(summary = "获得传感器最新数据")
    @Parameter(name = "sensorId", description = "传感器ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-env:query')")
    public CommonResult<IbmsEnvDataRecordRespVO> getLatestEnvDataRecord(@RequestParam("sensorId") Long sensorId) {
        IbmsEnvDataRecordDO record = envService.getLatestEnvDataRecord(sensorId);
        return success(BeanUtils.toBean(record, IbmsEnvDataRecordRespVO.class));
    }

    @GetMapping("/data/history")
    @Operation(summary = "获得传感器历史数据")
    @PreAuthorize("@ss.hasPermission('iot:building-env:query')")
    public CommonResult<List<IbmsEnvDataRecordRespVO>> getEnvDataRecordHistory(
            @RequestParam("sensorId") Long sensorId,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<IbmsEnvDataRecordDO> list = envService.getEnvDataRecordHistory(sensorId, limit);
        return success(BeanUtils.toBean(list, IbmsEnvDataRecordRespVO.class));
    }

    // ======================= 告警管理 =======================

    @GetMapping("/alarm/page")
    @Operation(summary = "获得环境告警分页")
    @PreAuthorize("@ss.hasPermission('iot:building-env:query')")
    public CommonResult<PageResult<IbmsEnvAlarmRespVO>> getEnvAlarmPage(@Valid IbmsEnvAlarmPageReqVO pageReqVO) {
        PageResult<IbmsEnvAlarmDO> pageResult = envService.getEnvAlarmPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsEnvAlarmRespVO.class));
    }

    @PutMapping("/alarm/handle")
    @Operation(summary = "处理环境告警")
    @PreAuthorize("@ss.hasPermission('iot:building-env:update')")
    public CommonResult<Boolean> handleEnvAlarm(
            @RequestParam("id") Long id,
            @RequestParam("handler") String handler,
            @RequestParam(value = "handleRemark", required = false) String handleRemark) {
        envService.handleEnvAlarm(id, handler, handleRemark);
        return success(true);
    }

    @PutMapping("/alarm/ignore")
    @Operation(summary = "忽略环境告警")
    @PreAuthorize("@ss.hasPermission('iot:building-env:update')")
    public CommonResult<Boolean> ignoreEnvAlarm(
            @RequestParam("id") Long id,
            @RequestParam("handler") String handler,
            @RequestParam(value = "handleRemark", required = false) String handleRemark) {
        envService.ignoreEnvAlarm(id, handler, handleRemark);
        return success(true);
    }

    // ======================= 统计分析 =======================

    @GetMapping("/statistics")
    @Operation(summary = "获得环境监测统计数据")
    @PreAuthorize("@ss.hasPermission('iot:building-env:query')")
    public CommonResult<IbmsEnvStatisticsVO> getEnvStatistics() {
        return success(envService.getEnvStatistics());
    }

}
