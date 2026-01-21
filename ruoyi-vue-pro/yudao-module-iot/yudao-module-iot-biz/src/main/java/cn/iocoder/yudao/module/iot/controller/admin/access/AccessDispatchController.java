package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dispatch.*;
import cn.iocoder.yudao.module.iot.service.access.AccessDispatchService;
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
 * 门禁下发记录 Controller
 *
 * @author 智能化系统
 */
@Tag(name = "管理后台 - 门禁下发记录")
@RestController
@RequestMapping("/iot/access-dispatch")
@Validated
public class AccessDispatchController {

    @Resource
    private AccessDispatchService accessDispatchService;

    @GetMapping("/get")
    @Operation(summary = "获得门禁下发记录")
    @Parameter(name = "id", description = "记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-dispatch:query')")
    public CommonResult<AccessDispatchRespVO> getAccessDispatch(@RequestParam("id") Long id) {
        AccessDispatchRespVO dispatch = accessDispatchService.getAccessDispatch(id);
        return success(dispatch);
    }

    @GetMapping("/page")
    @Operation(summary = "获得门禁下发记录分页")
    @PreAuthorize("@ss.hasPermission('iot:access-dispatch:query')")
    public CommonResult<PageResult<AccessDispatchRespVO>> getAccessDispatchPage(@Valid AccessDispatchPageReqVO pageVO) {
        PageResult<AccessDispatchRespVO> pageResult = accessDispatchService.getAccessDispatchPage(pageVO);
        return success(pageResult);
    }

}


























