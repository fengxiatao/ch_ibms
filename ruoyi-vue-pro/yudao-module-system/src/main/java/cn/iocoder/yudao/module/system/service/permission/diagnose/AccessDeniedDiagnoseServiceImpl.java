package cn.iocoder.yudao.module.system.service.permission.diagnose;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.controller.admin.permission.diagnose.vo.PermissionDiagnoseRespVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.diagnose.vo.PermissionDiagnoseRespVO.MenuSimpleVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantPackageDO;
import cn.iocoder.yudao.module.system.dal.mysql.tenant.TenantPackageMapper;
import cn.iocoder.yudao.module.system.enums.permission.MenuTypeEnum;
import cn.iocoder.yudao.module.system.service.permission.MenuService;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 权限「拒绝访问」诊断 Service 实现
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class AccessDeniedDiagnoseServiceImpl implements AccessDeniedDiagnoseService {

    /** 解析 {@code @ss.hasPermission('xxx')} / {@code @ss.hasAnyPermissions('a','b')} 中的 permission 字符串 */
    private static final Pattern PERMISSION_PATTERN = Pattern.compile("['\"]([\\w:\\-*]+)['\"]");

    @Resource
    @Lazy
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Resource
    private MenuService menuService;

    @Resource
    @Lazy
    private TenantService tenantService;

    @Resource
    private TenantPackageMapper tenantPackageMapper;

    /** 缓存：HandlerMethod -> permission */
    private final Map<HandlerMethod, String> handlerPermissionCache = new HashMap<>();

    @PostConstruct
    public void initPermissionIndex() {
        try {
            Map<RequestMappingInfo, HandlerMethod> mappings = requestMappingHandlerMapping.getHandlerMethods();
            int hit = 0;
            for (HandlerMethod hm : mappings.values()) {
                String perm = extractPermission(hm);
                if (StrUtil.isNotBlank(perm)) {
                    handlerPermissionCache.put(hm, perm);
                    hit++;
                }
            }
            log.info("[AccessDeniedDiagnose] 扫描 @PreAuthorize 完成，共 {} 个 HandlerMethod，其中 {} 个含 permission",
                    mappings.size(), hit);
        } catch (Exception e) {
            log.error("[AccessDeniedDiagnose] 初始化 permission 索引失败", e);
        }
    }

    private String extractPermission(HandlerMethod hm) {
        PreAuthorize ann = hm.getMethodAnnotation(PreAuthorize.class);
        if (ann == null) {
            ann = hm.getBeanType().getAnnotation(PreAuthorize.class);
        }
        if (ann == null || StrUtil.isBlank(ann.value())) {
            return null;
        }
        Matcher m = PERMISSION_PATTERN.matcher(ann.value());
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    @Override
    public PermissionDiagnoseRespVO diagnose(HttpServletRequest request, Long tenantPackageId) {
        if (request == null) {
            return null;
        }
        HandlerMethod hm = resolveHandlerMethod(request);
        String permission = hm == null ? null : handlerPermissionCache.get(hm);
        return buildResult(request.getMethod(), request.getRequestURI(), hm, permission, tenantPackageId);
    }

    @Override
    public PermissionDiagnoseRespVO diagnose(String httpMethod, String url, Long tenantPackageId) {
        // 通过模拟 request 反查 HandlerMethod 较复杂，这里用线性扫索引兜底（按 url + method 模糊匹配）
        HandlerMethod hm = null;
        String permission = null;
        try {
            Map<RequestMappingInfo, HandlerMethod> mappings = requestMappingHandlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> e : mappings.entrySet()) {
                RequestMappingInfo info = e.getKey();
                if (info.getPathPatternsCondition() != null
                        && info.getPathPatternsCondition().getPatterns().stream()
                        .anyMatch(p -> p.matches(org.springframework.http.server.PathContainer.parsePath(stripContextPath(url))))
                        && (info.getMethodsCondition().getMethods().isEmpty()
                        || info.getMethodsCondition().getMethods().stream()
                        .anyMatch(rm -> rm.name().equalsIgnoreCase(httpMethod)))) {
                    hm = e.getValue();
                    break;
                }
            }
        } catch (Exception ex) {
            log.warn("[AccessDeniedDiagnose] 反查 HandlerMethod 失败 url={}, msg={}", url, ex.getMessage());
        }
        if (hm != null) {
            permission = handlerPermissionCache.get(hm);
        }
        return buildResult(httpMethod, url, hm, permission, tenantPackageId);
    }

    private static String stripContextPath(String url) {
        // 去掉 query string + 去掉常见 admin-api 前缀
        if (url == null) {
            return "/";
        }
        int q = url.indexOf('?');
        String path = q >= 0 ? url.substring(0, q) : url;
        // 去掉 host 部分
        int proto = path.indexOf("://");
        if (proto > 0) {
            int slash = path.indexOf('/', proto + 3);
            path = slash > 0 ? path.substring(slash) : "/";
        }
        if (path.startsWith("/admin-api")) {
            path = path.substring("/admin-api".length());
        }
        if (path.startsWith("/app-api")) {
            path = path.substring("/app-api".length());
        }
        return path.isEmpty() ? "/" : path;
    }

    private HandlerMethod resolveHandlerMethod(HttpServletRequest request) {
        try {
            HandlerExecutionChain chain = requestMappingHandlerMapping.getHandler(request);
            if (chain != null && chain.getHandler() instanceof HandlerMethod) {
                return (HandlerMethod) chain.getHandler();
            }
        } catch (Exception e) {
            log.debug("[AccessDeniedDiagnose] resolveHandlerMethod 失败 url={}", request.getRequestURI());
        }
        return null;
    }

    private PermissionDiagnoseRespVO buildResult(String httpMethod, String url, HandlerMethod hm,
                                                 String permission, Long tenantPackageId) {
        PermissionDiagnoseRespVO vo = new PermissionDiagnoseRespVO();
        vo.setHttpMethod(httpMethod);
        vo.setUrl(url);
        vo.setTenantPackageId(tenantPackageId);
        if (hm != null) {
            vo.setHandlerClass(hm.getBeanType().getName());
            vo.setHandlerMethod(hm.getMethod().getName());
        }
        if (StrUtil.isBlank(permission)) {
            vo.setSuggestion("未识别到该接口的 @PreAuthorize 权限点，可能是无鉴权接口或方法签名识别失败。");
            return vo;
        }
        vo.setRequiredPermission(permission);

        // 1) permission → menus
        List<Long> menuIds = menuService.getMenuIdListByPermissionFromCache(permission);
        if (CollUtil.isEmpty(menuIds)) {
            vo.setSuggestion(String.format("权限 %s 在 system_menu 表中不存在；请联系开发补建按钮菜单。", permission));
            return vo;
        }
        List<MenuDO> menus = menuService.getMenuList(menuIds);
        vo.setPermissionMenus(menus.stream().map(this::toSimpleVO).collect(Collectors.toList()));

        // 2) 取首个按钮的祖先链（一般 permission 唯一对应一个按钮）
        MenuDO leaf = menus.stream()
                .filter(m -> Objects.equals(m.getType(), MenuTypeEnum.BUTTON.getType()))
                .findFirst().orElse(menus.get(0));
        List<MenuDO> chain = buildAncestorChain(leaf);
        vo.setAncestorChain(chain.stream().map(this::toSimpleVO).collect(Collectors.toList()));

        // 3) 推荐运营勾选的菜单：按钮的直接父级（type=2 菜单页），找不到则按钮本身
        MenuDO recommend = chain.stream()
                .filter(m -> Objects.equals(m.getType(), MenuTypeEnum.MENU.getType()))
                .reduce((a, b) -> b)  // 取最深的 type=2
                .orElse(leaf);
        vo.setRecommendCheckMenuId(recommend.getId());

        // 4) 套餐对比
        if (tenantPackageId != null) {
            TenantPackageDO pkg = tenantPackageMapper.selectById(tenantPackageId);
            if (pkg == null) {
                vo.getMessages().add(String.format("套餐 %d 不存在", tenantPackageId));
            } else {
                Set<Long> packageMenuIds = pkg.getMenuIds() == null ? Collections.emptySet() : pkg.getMenuIds();
                List<Long> missing = new ArrayList<>();
                for (MenuDO m : chain) {
                    if (!packageMenuIds.contains(m.getId())) {
                        missing.add(m.getId());
                    }
                }
                if (!packageMenuIds.contains(leaf.getId())) {
                    missing.add(leaf.getId());
                }
                vo.setMissingInPackage(missing.stream().distinct().collect(Collectors.toList()));
            }
        }

        // 5) 修复建议
        String chainText = chain.stream()
                .map(m -> m.getId() + " " + m.getName())
                .collect(Collectors.joining(" → "));
        vo.setSuggestion(String.format(
                "缺权限 [%s] → 按钮 [%d %s]；祖先链：%s。建议在套餐里勾选 [%d %s]，并重新登录。",
                permission, leaf.getId(), leaf.getName(), chainText,
                recommend.getId(), recommend.getName()));
        return vo;
    }

    private List<MenuDO> buildAncestorChain(MenuDO leaf) {
        List<MenuDO> chain = new ArrayList<>();
        MenuDO cur = leaf;
        Map<Long, MenuDO> all = menuService.getMenuList().stream()
                .collect(Collectors.toMap(MenuDO::getId, m -> m, (a, b) -> a));
        Set<Long> visited = new HashSet<>();
        while (cur != null && cur.getParentId() != null
                && !Objects.equals(cur.getParentId(), MenuDO.ID_ROOT)
                && visited.add(cur.getParentId())) {
            MenuDO parent = all.get(cur.getParentId());
            if (parent == null) {
                break;
            }
            chain.add(0, parent);
            cur = parent;
        }
        return chain;
    }

    private MenuSimpleVO toSimpleVO(MenuDO m) {
        MenuSimpleVO vo = new MenuSimpleVO();
        vo.setId(m.getId());
        vo.setName(m.getName());
        vo.setType(m.getType());
        vo.setPermission(m.getPermission());
        vo.setParentId(m.getParentId());
        return vo;
    }

    @Override
    public PermissionDiagnoseRespVO autoFix(Long tenantPackageId, String httpMethod, String url, boolean hideAncestorMenuPage) {
        PermissionDiagnoseRespVO diag = diagnose(httpMethod, url, tenantPackageId);
        if (diag == null || StrUtil.isBlank(diag.getRequiredPermission())) {
            return diag;
        }
        if (CollUtil.isEmpty(diag.getMissingInPackage())) {
            diag.getMessages().add("无需修复：套餐已包含所有相关菜单。");
            return diag;
        }
        TenantPackageDO pkg = tenantPackageMapper.selectById(tenantPackageId);
        if (pkg == null) {
            diag.getMessages().add(String.format("套餐 %d 不存在，跳过修复。", tenantPackageId));
            return diag;
        }
        // 1) 把缺失菜单加入 menu_ids
        Set<Long> menuIds = new HashSet<>(pkg.getMenuIds() == null ? Collections.emptySet() : pkg.getMenuIds());
        menuIds.addAll(diag.getMissingInPackage());

        // 2) 可选：把按钮所在的 type=2 菜单页加入 excludedMenuIds（仅放行 API，不在前端侧边栏显示）
        Set<Long> excluded = new HashSet<>(pkg.getExcludedMenuIds() == null ? Collections.emptySet() : pkg.getExcludedMenuIds());
        if (hideAncestorMenuPage && diag.getRecommendCheckMenuId() != null) {
            excluded.add(diag.getRecommendCheckMenuId());
        }

        TenantPackageDO update = new TenantPackageDO();
        update.setId(pkg.getId());
        update.setMenuIds(menuIds);
        update.setExcludedMenuIds(excluded);
        tenantPackageMapper.updateById(update);
        diag.getMessages().add(String.format(
                "已把 %s 加入套餐 %d 的 menu_ids%s。",
                JsonUtils.toJsonString(diag.getMissingInPackage()),
                tenantPackageId,
                hideAncestorMenuPage ? String.format("，并把 %d 加入 excludedMenuIds", diag.getRecommendCheckMenuId()) : ""));

        // 3) 同步刷新该套餐下所有租户的角色菜单
        try {
            List<TenantDO> tenants = tenantService.getTenantListByPackageId(tenantPackageId);
            tenants.forEach(t -> tenantService.updateTenantRoleMenu(t.getId(), menuIds));
            diag.getMessages().add(String.format("已同步刷新 %d 个租户的角色菜单。", tenants.size()));
        } catch (Exception e) {
            log.error("[AccessDeniedDiagnose] 同步 role_menu 失败 packageId={}", tenantPackageId, e);
            diag.getMessages().add("同步 role_menu 失败：" + e.getMessage());
        }

        diag.setMissingInPackage(Collections.emptyList());
        return diag;
    }

}
