package cn.iocoder.yudao.module.system.controller.admin.permission.diagnose;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.controller.admin.permission.diagnose.vo.PermissionDiagnoseRespVO;
import cn.iocoder.yudao.module.system.service.permission.diagnose.AccessDeniedDiagnoseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 权限「拒绝访问」诊断 Controller
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - 权限诊断")
@RestController
@RequestMapping("/system/permission/diagnose")
@Validated
public class PermissionDiagnoseController {

    @Resource
    private AccessDeniedDiagnoseService diagnoseService;

    @GetMapping("")
    @Operation(summary = "诊断指定 URL 被拒原因", description = "输入 HTTP 方法 + URL，返回缺失 permission、对应菜单、祖先链与修复建议")
    @Parameter(name = "method", description = "HTTP 方法", example = "GET", required = true)
    @Parameter(name = "url", description = "请求 URL（可带 contextPath）", example = "/admin-api/iot/parking/lane/list-by-lot", required = true)
    @Parameter(name = "tenantPackageId", description = "租户套餐 ID；传入则计算缺失项")
    @PreAuthorize("@ss.hasPermission('system:permission:diagnose')")
    public CommonResult<PermissionDiagnoseRespVO> diagnose(
            @RequestParam("method") @NotBlank String method,
            @RequestParam("url") @NotBlank String url,
            @RequestParam(value = "tenantPackageId", required = false) Long tenantPackageId) {
        return success(diagnoseService.diagnose(method, url, tenantPackageId));
    }

    @PostMapping("/auto-fix")
    @Operation(summary = "一键修复：把缺失菜单加进套餐并同步 role_menu",
            description = "诊断指定 URL 后，自动把缺失的菜单 ID（按钮 + 祖先链）追加到套餐的 menu_ids，并同步该套餐下所有租户的角色菜单")
    @Parameter(name = "tenantPackageId", description = "租户套餐 ID", example = "113", required = true)
    @Parameter(name = "method", description = "HTTP 方法", example = "GET", required = true)
    @Parameter(name = "url", description = "请求 URL", example = "/admin-api/iot/parking/lane/list-by-lot", required = true)
    @Parameter(name = "hideAncestorMenuPage", description = "是否同时把按钮所在的 type=2 菜单页加入 excludedMenuIds（仅放行 API、不在侧边栏显示），默认 false")
    @PreAuthorize("@ss.hasPermission('system:permission:diagnose-auto-fix')")
    public CommonResult<PermissionDiagnoseRespVO> autoFix(
            @RequestParam("tenantPackageId") @NotNull Long tenantPackageId,
            @RequestParam("method") @NotBlank String method,
            @RequestParam("url") @NotBlank String url,
            @RequestParam(value = "hideAncestorMenuPage", defaultValue = "false") boolean hideAncestorMenuPage) {
        return success(diagnoseService.autoFix(tenantPackageId, method, url, hideAncestorMenuPage));
    }

}
