package cn.iocoder.yudao.module.iot.controller.admin.alarm;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventHandleReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventStatsVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventProcessReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventExportVO;
import cn.iocoder.yudao.module.iot.service.alarm.IotAlarmEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 报警事件 Controller
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - 报警事件")
@RestController
@RequestMapping("/iot/alarm/event")
@Validated
public class IotAlarmEventController {

    @Resource
    private IotAlarmEventService alarmEventService;

    @GetMapping("/page")
    @Operation(summary = "获得报警事件分页")
    @PreAuthorize("@ss.hasPermission('iot:alarm-event:query')")
    public CommonResult<PageResult<IotAlarmEventRespVO>> getEventPage(@Valid IotAlarmEventPageReqVO pageReqVO) {
        return success(alarmEventService.getEventPageVO(pageReqVO));
    }

    @GetMapping("/stats")
    @Operation(summary = "获得报警事件统计")
    @PreAuthorize("@ss.hasPermission('iot:alarm-event:query')")
    public CommonResult<IotAlarmEventStatsVO> getEventStats() {
        return success(alarmEventService.getEventStats());
    }

    @GetMapping("/get")
    @Operation(summary = "获得报警事件详情")
    @Parameter(name = "id", description = "事件ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-event:query')")
    public CommonResult<IotAlarmEventRespVO> getEvent(@RequestParam("id") Long id) {
        return success(alarmEventService.getEvent(id));
    }

    @PutMapping("/handle")
    @Operation(summary = "处理报警事件")
    @Parameter(name = "id", description = "事件ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-event:handle')")
    public CommonResult<Boolean> handleEvent(@Valid @RequestBody IotAlarmEventHandleReqVO reqVO) {
        alarmEventService.handleEvent(reqVO);
        return success(true);
    }

    /**
     * 前端兼容：处理报警事件（旧路径 /process）
     */
    @PutMapping("/process")
    @Operation(summary = "处理报警事件（兼容前端）")
    @PreAuthorize("@ss.hasPermission('iot:alarm-event:handle')")
    public CommonResult<Boolean> processEvent(@Valid @RequestBody IotAlarmEventProcessReqVO reqVO) {
        StringBuilder sb = new StringBuilder();
        if (reqVO.getResult() != null && !reqVO.getResult().isEmpty()) {
            sb.append("result=").append(reqVO.getResult()).append("; ");
        }
        if (reqVO.getActions() != null && !reqVO.getActions().isEmpty()) {
            sb.append("actions=").append(String.join(",", reqVO.getActions())).append("; ");
        }
        if (reqVO.getRemark() != null && !reqVO.getRemark().isEmpty()) {
            sb.append(reqVO.getRemark());
        }
        IotAlarmEventHandleReqVO handle = new IotAlarmEventHandleReqVO();
        handle.setId(reqVO.getId());
        handle.setHandleRemark(sb.toString());
        alarmEventService.handleEvent(handle);
        return success(true);
    }

    @PutMapping("/ignore")
    @Operation(summary = "忽略报警事件")
    @Parameter(name = "id", description = "事件ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-event:handle')")
    public CommonResult<Boolean> ignoreEvent(@RequestParam("id") Long id,
                                             @RequestParam(value = "remark", required = false) String remark) {
        alarmEventService.ignoreEvent(id, remark);
        return success(true);
    }

    @GetMapping("/export")
    @Operation(summary = "导出报警事件")
    @PreAuthorize("@ss.hasPermission('iot:alarm-event:export')")
    public void export(@Valid IotAlarmEventPageReqVO reqVO, HttpServletResponse response) throws java.io.IOException {
        List<IotAlarmEventRespVO> list = alarmEventService.getEventListForExport(reqVO);
        List<IotAlarmEventExportVO> exportList = list.stream().map(item -> {
            IotAlarmEventExportVO vo = new IotAlarmEventExportVO();
            vo.setId(item.getId());
            vo.setHostId(item.getHostId());
            vo.setHostName(item.getHostName());
            vo.setEventCode(item.getEventCode());
            vo.setEventType(item.getEventType());
            vo.setEventLevel(item.getEventLevel());
            vo.setAreaNo(item.getAreaNo());
            vo.setZoneNo(item.getZoneNo());
            vo.setZoneName(item.getZoneName());
            vo.setEventDesc(item.getEventDesc());
            vo.setIsNewEvent(item.getIsNewEvent());
            vo.setIsHandled(item.getIsHandled());
            vo.setHandledBy(item.getHandledBy());
            vo.setHandledTime(item.getHandledTime());
            vo.setHandleRemark(item.getHandleRemark());
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).toList();
        ExcelUtils.write(response, "报警事件.xls", "数据", IotAlarmEventExportVO.class, exportList);
    }
}
