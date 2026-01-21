package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.event.IotDeviceEventLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.event.IotDeviceEventLogRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceEventLogDO;
import cn.iocoder.yudao.module.iot.service.device.event.IotDeviceEventLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 设备事件日志控制器
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - IoT 设备事件日志")
@RestController
@RequestMapping("/iot/device/event-log")
@Validated
public class IotDeviceEventLogController {

    @Resource
    private IotDeviceEventLogService eventLogService;

    @GetMapping("/page")
    @Operation(summary = "获取设备事件日志分页")
    public CommonResult<PageResult<IotDeviceEventLogRespVO>> getEventLogPage(@Valid IotDeviceEventLogPageReqVO pageReqVO) {
        PageResult<IotDeviceEventLogDO> pageResult = eventLogService.getEventLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceEventLogRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取设备事件日志详情")
    @Parameter(name = "id", description = "事件日志ID", required = true, example = "1")
    public CommonResult<IotDeviceEventLogRespVO> getEventLog(@RequestParam("id") Long id) {
        IotDeviceEventLogDO eventLog = eventLogService.getEventLog(id);
        return success(BeanUtils.toBean(eventLog, IotDeviceEventLogRespVO.class));
    }
}












