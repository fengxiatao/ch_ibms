package cn.iocoder.yudao.module.system.service.permission.diagnose;

import cn.iocoder.yudao.module.system.controller.admin.permission.diagnose.vo.PermissionDiagnoseRespVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 权限「拒绝访问」诊断 Service。
 * <p>
 * 启动时扫描所有 Controller 上的 {@code @PreAuthorize} 注解，建立 URL → permission 索引；
 * 当出现 403 时反查需要的 permission、对应的菜单及祖先链，输出"修复指引"。
 *
 * @author 长辉信息科技有限公司
 */
public interface AccessDeniedDiagnoseService {

    /**
     * 根据 HTTP 请求诊断当前请求被拒原因。
     *
     * @param request 当前 HTTP 请求
     * @param tenantPackageId 期望诊断的套餐编号；为 null 时不计算"套餐是否包含该菜单"
     * @return 诊断结果；解析不到 permission 时返回 null
     */
    PermissionDiagnoseRespVO diagnose(HttpServletRequest request, Long tenantPackageId);

    /**
     * 根据 HTTP method + url 诊断。
     *
     * @param httpMethod HTTP 方法（GET/POST/...）
     * @param url 请求 URL（不含 host，可带 contextPath）
     * @param tenantPackageId 期望诊断的套餐编号；为 null 时不计算"套餐是否包含该菜单"
     * @return 诊断结果；解析不到 permission 时返回 null
     */
    PermissionDiagnoseRespVO diagnose(String httpMethod, String url, Long tenantPackageId);

    /**
     * 一键修复：把诊断出的缺失菜单（按钮 + 祖先链）加入指定套餐，并同步该套餐下所有租户的角色菜单。
     *
     * @param tenantPackageId 套餐编号
     * @param httpMethod HTTP 方法
     * @param url 请求 URL
     * @param hideAncestorMenuPage 是否把按钮所在的 type=2 菜单页加入 excludedMenuIds（仅放行 API，不在侧边栏显示）
     * @return 修复后的诊断结果（含追加的菜单列表）
     */
    PermissionDiagnoseRespVO autoFix(Long tenantPackageId, String httpMethod, String url, boolean hideAncestorMenuPage);

}
