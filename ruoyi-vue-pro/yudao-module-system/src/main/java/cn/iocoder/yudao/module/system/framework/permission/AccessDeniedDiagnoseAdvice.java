package cn.iocoder.yudao.module.system.framework.permission;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.system.controller.admin.permission.diagnose.vo.PermissionDiagnoseRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.service.permission.diagnose.AccessDeniedDiagnoseService;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;

/**
 * 权限「拒绝访问」诊断 Advice。
 * <p>
 * 优先级高于 {@code GlobalExceptionHandler}，抓到 {@link AccessDeniedException} 时先输出
 * 详细的「修复指引」日志，再返回标准 403。日志包含：URL、缺失 permission、对应菜单、祖先链、
 * 套餐缺失项、修复建议，运维不再需要翻数据库手工排查。
 *
 * @author 长辉信息科技有限公司
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class AccessDeniedDiagnoseAdvice {

    @Resource
    private AccessDeniedDiagnoseService diagnoseService;

    @Resource
    @Lazy
    private TenantService tenantService;

    @ExceptionHandler(AccessDeniedException.class)
    public CommonResult<?> handleAccessDenied(HttpServletRequest req, AccessDeniedException ex) {
        Long userId = WebFrameworkUtils.getLoginUserId(req);
        Long tenantId = safeTenantId();
        Long packageId = resolvePackageId(tenantId);

        try {
            PermissionDiagnoseRespVO diag = diagnoseService.diagnose(req, packageId);
            if (diag != null && diag.getRequiredPermission() != null) {
                log.warn("[accessDeniedExceptionHandler] userId={} tenantId={} url={} 缺权限={} 缺失菜单={} 建议={}",
                        userId, tenantId, req.getRequestURI(),
                        diag.getRequiredPermission(),
                        diag.getMissingInPackage(),
                        diag.getSuggestion());
            } else {
                log.warn("[accessDeniedExceptionHandler] userId={} tenantId={} url={} 未识别到 @PreAuthorize 权限点",
                        userId, tenantId, req.getRequestURI());
            }
        } catch (Exception diagEx) {
            log.warn("[accessDeniedExceptionHandler] userId={} url={} 诊断失败：{}",
                    userId, req.getRequestURI(), diagEx.getMessage());
        }
        return CommonResult.error(FORBIDDEN);
    }

    private Long safeTenantId() {
        try {
            return TenantContextHolder.getTenantId();
        } catch (Exception e) {
            return null;
        }
    }

    private Long resolvePackageId(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        try {
            TenantDO tenant = tenantService.getTenant(tenantId);
            return tenant == null ? null : tenant.getPackageId();
        } catch (Exception e) {
            return null;
        }
    }

}
