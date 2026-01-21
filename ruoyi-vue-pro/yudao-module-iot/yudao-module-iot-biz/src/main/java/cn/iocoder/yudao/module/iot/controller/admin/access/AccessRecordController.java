package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.record.*;
import cn.iocoder.yudao.module.iot.service.access.AccessRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁记录 Controller
 *
 * @author 智能化系统
 */
@Tag(name = "管理后台 - 门禁记录")
@RestController
@RequestMapping("/iot/access-record")
@Validated
public class AccessRecordController {

    @Resource
    private AccessRecordService accessRecordService;

    @GetMapping("/get")
    @Operation(summary = "获得门禁记录")
    @Parameter(name = "id", description = "记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-record:query')")
    public CommonResult<AccessRecordRespVO> getAccessRecord(@RequestParam("id") Long id) {
        AccessRecordRespVO record = accessRecordService.getAccessRecord(id);
        return success(record);
    }

    @GetMapping("/page")
    @Operation(summary = "获得门禁记录分页")
    @PreAuthorize("@ss.hasPermission('iot:access-record:query')")
    public CommonResult<PageResult<AccessRecordRespVO>> getAccessRecordPage(@Valid AccessRecordPageReqVO pageVO) {
        PageResult<AccessRecordRespVO> pageResult = accessRecordService.getAccessRecordPage(pageVO);
        return success(pageResult);
    }

    @GetMapping("/statistics/method")
    @Operation(summary = "获取通行方式统计")
    @PreAuthorize("@ss.hasPermission('iot:access-record:query')")
    public CommonResult<java.util.List<java.util.Map<String, Object>>> getAccessMethodStatistics(
            @RequestParam(required = false) java.time.LocalDateTime startTime,
            @RequestParam(required = false) java.time.LocalDateTime endTime) {
        java.util.List<java.util.Map<String, Object>> statistics = 
            accessRecordService.getAccessMethodStatistics(startTime, endTime);
        return success(statistics);
    }

    @GetMapping("/statistics/hourly")
    @Operation(summary = "获取24小时人员流量统计")
    @PreAuthorize("@ss.hasPermission('iot:access-record:query')")
    public CommonResult<java.util.List<java.util.Map<String, Object>>> getHourlyTrafficStatistics(
            @RequestParam(required = false) java.time.LocalDate date) {
        java.util.List<java.util.Map<String, Object>> statistics = 
            accessRecordService.getHourlyTrafficStatistics(date);
        return success(statistics);
    }

}

