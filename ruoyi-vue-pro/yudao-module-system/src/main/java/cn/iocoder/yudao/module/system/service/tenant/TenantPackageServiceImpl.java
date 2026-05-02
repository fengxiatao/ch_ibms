package cn.iocoder.yudao.module.system.service.tenant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages.TenantPackagePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages.TenantPackageSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantPackageDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.mysql.tenant.TenantPackageMapper;
import cn.iocoder.yudao.module.system.enums.permission.MenuTypeEnum;
import cn.iocoder.yudao.module.system.service.permission.MenuService;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 租户套餐 Service 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
public class TenantPackageServiceImpl implements TenantPackageService {

    @Resource
    private TenantPackageMapper tenantPackageMapper;

    @Resource
    private MenuService menuService;

    @Resource
    @Lazy // 避免循环依赖的报错
    private TenantService tenantService;

    @Override
    public Long createTenantPackage(TenantPackageSaveReqVO createReqVO) {
        // 校验套餐名是否重复
        validateTenantPackageNameUnique(null, createReqVO.getName());
        // 插入
        TenantPackageDO tenantPackage = BeanUtils.toBean(createReqVO, TenantPackageDO.class);
        tenantPackage.setMenuIds(normalizeTenantPackageMenuIds(createReqVO.getMenuIds(), createReqVO.getExcludedMenuIds()));
        tenantPackageMapper.insert(tenantPackage);
        // 返回
        return tenantPackage.getId();
    }

    @Override
    @DSTransactional // 多数据源，使用 @DSTransactional 保证本地事务，以及数据源的切换
    public void updateTenantPackage(TenantPackageSaveReqVO updateReqVO) {
        // 校验存在
        TenantPackageDO tenantPackage = validateTenantPackageExists(updateReqVO.getId());
        // 校验套餐名是否重复
        validateTenantPackageNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 将套餐菜单固化为「按钮权限(type=3)且 permission 非空」+「其父级目录链」
        // 目的：权限来源仍以按钮为准，同时保证前端可构建菜单树，避免左侧菜单为空
        Set<Long> normalizedMenuIds = normalizeTenantPackageMenuIds(updateReqVO.getMenuIds(), updateReqVO.getExcludedMenuIds());
        // 更新
        TenantPackageDO updateObj = BeanUtils.toBean(updateReqVO, TenantPackageDO.class);
        updateObj.setMenuIds(normalizedMenuIds);
        tenantPackageMapper.updateById(updateObj);
        // 固化策略：每次保存套餐后，都立即重算该套餐下租户的角色菜单
        // 这样可以确保「租户管理员 = 套餐权限」及时生效，并触发权限缓存刷新
        List<TenantDO> tenants = tenantService.getTenantListByPackageId(tenantPackage.getId());
        tenants.forEach(tenant -> tenantService.updateTenantRoleMenu(tenant.getId(), normalizedMenuIds));
    }

    /**
     * 规范化套餐菜单 ID 集合。
     * <p>
     * 设计原则：所见即所得。前端勾选什么后端就保存什么，仅做两类自动补齐：
     * <ol>
     *     <li>子树展开：保证勾选了某个目录/菜单时，其下所有子菜单和按钮自动包含，
     *         避免前端只勾父节点导致按钮权限点丢失（API 403）。</li>
     *     <li>父目录链补齐：保证被勾选的菜单/按钮其祖先目录都在集合内，否则前端菜单树构建不出来。</li>
     * </ol>
     * 不再做任何"种子+反推过滤"，前端勾选的所有菜单（含 type=2+permission 非空的列表页）都会原样保留。
     */
    @VisibleForTesting
    Set<Long> normalizeTenantPackageMenuIds(Set<Long> menuIds, Set<Long> excludedMenuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            return Collections.emptySet();
        }

        // 全量菜单用于树构建
        List<MenuDO> allMenus = menuService.getMenuList();
        if (CollUtil.isEmpty(allMenus)) {
            return new HashSet<>(menuIds);
        }
        Map<Long, MenuDO> menuById = allMenus.stream()
                .collect(Collectors.toMap(MenuDO::getId, m -> m, (a, b) -> a));
        Map<Long, List<Long>> childrenIdsByParentId = new HashMap<>();
        for (MenuDO menu : allMenus) {
            childrenIdsByParentId.computeIfAbsent(menu.getParentId(), k -> new ArrayList<>()).add(menu.getId());
        }

        // 1) 子树展开：从前端勾选的节点出发，把所有子菜单/按钮都纳入集合
        //    （兼容前端只勾父节点的情况，避免按钮权限点丢失导致 API 403）
        Set<Long> normalizedIds = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>(menuIds);
        while (!queue.isEmpty()) {
            Long id = queue.pollFirst();
            if (id == null || !normalizedIds.add(id)) {
                continue;
            }
            List<Long> children = childrenIdsByParentId.get(id);
            if (CollUtil.isNotEmpty(children)) {
                children.forEach(queue::addLast);
            }
        }

        // 2) 父目录链补齐：避免被勾选的菜单/按钮的祖先目录缺失导致前端菜单树构建失败
        for (Long id : new ArrayList<>(normalizedIds)) {
            MenuDO current = menuById.get(id);
            Set<Long> visited = new HashSet<>();
            while (current != null
                    && current.getParentId() != null
                    && !Objects.equals(current.getParentId(), MenuDO.ID_ROOT)
                    && visited.add(current.getParentId())) {
                normalizedIds.add(current.getParentId());
                current = menuById.get(current.getParentId());
            }
        }

        // 3) 排除机制：移除 excludedMenuIds 指定的根菜单及其下的非按钮菜单
        //    保留按钮(type=3)以确保 API 权限不丢失，移除目录(type=1)和页面(type=2)以隐藏侧边栏
        if (CollUtil.isNotEmpty(excludedMenuIds)) {
            normalizedIds.removeIf(id -> {
                MenuDO menu = menuById.get(id);
                if (menu == null) {
                    return false;
                }
                if (Objects.equals(menu.getType(), MenuTypeEnum.BUTTON.getType())) {
                    return false;
                }
                return isAncestorExcluded(menu, excludedMenuIds, menuById);
            });
        }
        return normalizedIds;
    }

    /**
     * 判断菜单的祖先链中是否包含被排除的根菜单ID
     */
    private boolean isAncestorExcluded(MenuDO menu, Set<Long> excludedMenuIds, Map<Long, MenuDO> menuById) {
        // 如果菜单本身就是被排除的根菜单
        if (excludedMenuIds.contains(menu.getId())) {
            return true;
        }
        // 向上遍历祖先链
        MenuDO current = menu;
        Set<Long> visited = new HashSet<>();
        while (current != null
                && current.getParentId() != null
                && !Objects.equals(current.getParentId(), MenuDO.ID_ROOT)
                && visited.add(current.getParentId())) {
            if (excludedMenuIds.contains(current.getParentId())) {
                return true;
            }
            current = menuById.get(current.getParentId());
        }
        return false;
    }

    @Override
    public void deleteTenantPackage(Long id) {
        // 校验存在
        validateTenantPackageExists(id);
        // 校验正在使用
        validateTenantUsed(id);
        // 删除
        tenantPackageMapper.deleteById(id);
    }

    @Override
    public void deleteTenantPackageList(List<Long> ids) {
        // 1. 校验是否有租户正在使用该套餐
        for (Long id : ids) {
            if (tenantService.getTenantCountByPackageId(id) > 0) {
                throw exception(TENANT_PACKAGE_USED);
            }
        }

        // 2. 批量删除
        tenantPackageMapper.deleteByIds(ids);
    }

    private TenantPackageDO validateTenantPackageExists(Long id) {
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(id);
        if (tenantPackage == null) {
            throw exception(TENANT_PACKAGE_NOT_EXISTS);
        }
        return tenantPackage;
    }

    private void validateTenantUsed(Long id) {
        if (tenantService.getTenantCountByPackageId(id) > 0) {
            throw exception(TENANT_PACKAGE_USED);
        }
    }

    @Override
    public TenantPackageDO getTenantPackage(Long id) {
        return tenantPackageMapper.selectById(id);
    }

    @Override
    public PageResult<TenantPackageDO> getTenantPackagePage(TenantPackagePageReqVO pageReqVO) {
        return tenantPackageMapper.selectPage(pageReqVO);
    }

    @Override
    public TenantPackageDO validTenantPackage(Long id) {
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(id);
        if (tenantPackage == null) {
            throw exception(TENANT_PACKAGE_NOT_EXISTS);
        }
        if (tenantPackage.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(TENANT_PACKAGE_DISABLE, tenantPackage.getName());
        }
        return tenantPackage;
    }

    @Override
    public List<TenantPackageDO> getTenantPackageListByStatus(Integer status) {
        return tenantPackageMapper.selectListByStatus(status);
    }


    @VisibleForTesting
    void validateTenantPackageNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        TenantPackageDO tenantPackage = tenantPackageMapper.selectByName(name);
        if (tenantPackage == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(TENANT_PACKAGE_NAME_DUPLICATE);
        }
        if (!tenantPackage.getId().equals(id)) {
            throw exception(TENANT_PACKAGE_NAME_DUPLICATE);
        }
    }

}
