package cn.iocoder.yudao.module.iot.controller.admin.visitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.*;
import cn.iocoder.yudao.module.iot.service.visitor.IotVisitorApplyService;
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
 * 访客申请 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 访客申请")
@RestController
@RequestMapping("/iot/visitor/apply")
@Validated
public class IotVisitorApplyController {

    @Resource
    private IotVisitorApplyService visitorApplyService;

    // ========== 申请管理 ==========

    @PostMapping("/create")
    @Operation(summary = "创建访客申请")
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:create')")
    public CommonResult<Long> createApply(@Valid @RequestBody IotVisitorApplyCreateReqVO reqVO) {
        return success(visitorApplyService.createApply(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新访客申请")
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:update')")
    public CommonResult<Boolean> updateApply(@RequestParam("id") Long id,
                                              @Valid @RequestBody IotVisitorApplyCreateReqVO reqVO) {
        visitorApplyService.updateApply(id, reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除访客申请")
    @Parameter(name = "id", description = "申请ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:delete')")
    public CommonResult<Boolean> deleteApply(@RequestParam("id") Long id) {
        visitorApplyService.deleteApply(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取访客申请详情")
    @Parameter(name = "id", description = "申请ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:query')")
    public CommonResult<IotVisitorApplyRespVO> getApply(@RequestParam("id") Long id) {
        return success(visitorApplyService.getApplyDetail(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获取访客申请分页")
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:query')")
    public CommonResult<PageResult<IotVisitorApplyRespVO>> getApplyPage(@Valid IotVisitorApplyPageReqVO reqVO) {
        return success(visitorApplyService.getApplyPage(reqVO));
    }

    // ========== 审批操作 ==========

    @PostMapping("/approve")
    @Operation(summary = "审批通过")
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:approve')")
    public CommonResult<Boolean> approve(@RequestParam("id") Long id,
                                          @RequestParam(value = "remark", required = false) String remark) {
        visitorApplyService.approve(id, remark);
        return success(true);
    }

    @PostMapping("/reject")
    @Operation(summary = "审批拒绝")
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:approve')")
    public CommonResult<Boolean> reject(@RequestParam("id") Long id,
                                         @RequestParam(value = "remark", required = false) String remark) {
        visitorApplyService.reject(id, remark);
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消申请")
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:update')")
    public CommonResult<Boolean> cancel(@RequestParam("id") Long id,
                                         @RequestParam(value = "reason", required = false) String reason) {
        visitorApplyService.cancel(id, reason);
        return success(true);
    }

    // ========== 签到签离 ==========

    @PostMapping("/check-in")
    @Operation(summary = "访客签到")
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:update')")
    public CommonResult<Boolean> checkIn(@RequestParam("id") Long id) {
        visitorApplyService.checkIn(id);
        return success(true);
    }

    @PostMapping("/check-out")
    @Operation(summary = "访客签离")
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:update')")
    public CommonResult<Boolean> checkOut(@RequestParam("id") Long id) {
        visitorApplyService.checkOut(id);
        return success(true);
    }

    // ========== 权限下发/回收 ==========

    @PostMapping("/dispatch-auth")
    @Operation(summary = "下发访客权限")
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:dispatch')")
    public CommonResult<Long> dispatchAuth(@Valid @RequestBody IotVisitorAuthDispatchReqVO reqVO) {
        return success(visitorApplyService.dispatchAuth(reqVO));
    }

    @PostMapping("/revoke-auth")
    @Operation(summary = "回收访客权限")
    @Parameter(name = "applyId", description = "申请ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:dispatch')")
    public CommonResult<Long> revokeAuth(@RequestParam("applyId") Long applyId) {
        return success(visitorApplyService.revokeAuth(applyId));
    }

    // ========== 统计 ==========

    @GetMapping("/statistics")
    @Operation(summary = "获取访客统计数据")
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:query')")
    public CommonResult<IotVisitorStatisticsRespVO> getStatistics() {
        return success(visitorApplyService.getStatistics());
    }

}
