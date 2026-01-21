package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.authorization.*;
import cn.iocoder.yudao.module.iot.service.access.AccessAuthorizationService;
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
 * 门禁授权 Controller
 *
 * @author 智能化系统
 */
@Tag(name = "管理后台 - 门禁授权")
@RestController
@RequestMapping("/iot/access-authorization")
@Validated
public class AccessAuthorizationController {

    @Resource
    private AccessAuthorizationService accessAuthorizationService;

    @PostMapping("/create")
    @Operation(summary = "创建门禁授权")
    @PreAuthorize("@ss.hasPermission('iot:access-authorization:create')")
    public CommonResult<Long> createAccessAuthorization(@Valid @RequestBody AccessAuthorizationCreateReqVO createReqVO) {
        return success(accessAuthorizationService.createAccessAuthorization(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新门禁授权")
    @PreAuthorize("@ss.hasPermission('iot:access-authorization:update')")
    public CommonResult<Boolean> updateAccessAuthorization(@Valid @RequestBody AccessAuthorizationUpdateReqVO updateReqVO) {
        accessAuthorizationService.updateAccessAuthorization(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门禁授权")
    @Parameter(name = "id", description = "授权ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-authorization:delete')")
    public CommonResult<Boolean> deleteAccessAuthorization(@RequestParam("id") Long id) {
        accessAuthorizationService.deleteAccessAuthorization(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门禁授权")
    @Parameter(name = "id", description = "授权ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-authorization:query')")
    public CommonResult<AccessAuthorizationRespVO> getAccessAuthorization(@RequestParam("id") Long id) {
        AccessAuthorizationRespVO authorization = accessAuthorizationService.getAccessAuthorization(id);
        return success(authorization);
    }

    @GetMapping("/page")
    @Operation(summary = "获得门禁授权分页")
    @PreAuthorize("@ss.hasPermission('iot:access-authorization:query')")
    public CommonResult<PageResult<AccessAuthorizationRespVO>> getAccessAuthorizationPage(@Valid AccessAuthorizationPageReqVO pageVO) {
        PageResult<AccessAuthorizationRespVO> pageResult = accessAuthorizationService.getAccessAuthorizationPage(pageVO);
        return success(pageResult);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新门禁授权状态")
    @PreAuthorize("@ss.hasPermission('iot:access-authorization:update')")
    public CommonResult<Boolean> updateAccessAuthorizationStatus(@RequestParam("id") Long id,
                                                                   @RequestParam("status") Integer status) {
        accessAuthorizationService.updateAccessAuthorizationStatus(id, status);
        return success(true);
    }

}


























