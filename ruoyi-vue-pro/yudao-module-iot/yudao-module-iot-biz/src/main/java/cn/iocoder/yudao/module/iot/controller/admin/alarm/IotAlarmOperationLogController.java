package cn.iocoder.yudao.module.iot.controller.admin.alarm;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.operationlog.IotAlarmOperationLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.operationlog.IotAlarmOperationLogRespVO;
import cn.iocoder.yudao.module.iot.service.alarm.IotAlarmOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 报警主机操作记录 Controller
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - 报警主机操作记录")
@RestController
@RequestMapping("/iot/alarm/operation-log")
@Validated
public class IotAlarmOperationLogController {

    @Resource
    private IotAlarmOperationLogService operationLogService;

    @GetMapping("/page")
    @Operation(summary = "获得报警主机操作记录分页")
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:query')")
    public CommonResult<PageResult<IotAlarmOperationLogRespVO>> getOperationLogPage(@Valid IotAlarmOperationLogPageReqVO pageReqVO) {
        PageResult<IotAlarmOperationLogRespVO> pageResult = operationLogService.getOperationLogPage(pageReqVO);
        return success(pageResult);
    }

}
