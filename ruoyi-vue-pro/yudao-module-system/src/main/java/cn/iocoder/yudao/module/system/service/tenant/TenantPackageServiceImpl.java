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
        tenantPackage.setMenuIds(normalizeTenantPackageMenuIds(createReqVO.getMenuIds()));
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
        Set<Long> normalizedMenuIds = normalizeTenantPackageMenuIds(updateReqVO.getMenuIds());
        // 更新
        TenantPackageDO updateObj = BeanUtils.toBean(updateReqVO, TenantPackageDO.class);
        updateObj.setMenuIds(normalizedMenuIds);
        tenantPackageMapper.updateById(updateObj);
        // 固化策略：每次保存套餐后，都立即重算该套餐下租户的角色菜单
        // 这样可以确保「租户管理员 = 套餐权限」及时生效，并触发权限缓存刷新
        List<TenantDO> tenants = tenantService.getTenantListByPackageId(tenantPackage.getId());
        tenants.forEach(tenant -> tenantService.updateTenantRoleMenu(tenant.getId(), normalizedMenuIds));
    }

    private Set<Long> normalizeTenantPackageMenuIds(Set<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            return Collections.emptySet();
        }

        // 全量菜单用于构建父子树
        List<MenuDO> allMenus = menuService.getMenuList();
        if (CollUtil.isEmpty(allMenus)) {
            return Collections.emptySet();
        }

        Map<Long, MenuDO> menuById = allMenus.stream()
                .collect(Collectors.toMap(MenuDO::getId, m -> m, (a, b) -> a));
        Map<Long, List<Long>> childrenIdsByParentId = new HashMap<>();
        for (MenuDO menu : allMenus) {
            childrenIdsByParentId.computeIfAbsent(menu.getParentId(), k -> new ArrayList<>()).add(menu.getId());
        }

        // 1) 从选择的节点出发，递归展开子树
        Set<Long> expandedIds = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>(menuIds);
        while (!queue.isEmpty()) {
            Long id = queue.pollFirst();
            if (id == null || !expandedIds.add(id)) {
                continue;
            }
            List<Long> children = childrenIdsByParentId.get(id);
            if (CollUtil.isNotEmpty(children)) {
                children.forEach(queue::addLast);
            }
        }

        // 2) 按钮权限作为授权来源（type=3 且 permission 非空）
        Set<Long> buttonIds = expandedIds.stream()
                .map(menuById::get)
                .filter(Objects::nonNull)
                .filter(m -> Objects.equals(m.getType(), MenuTypeEnum.BUTTON.getType()))
                .filter(m -> StrUtil.isNotBlank(m.getPermission()))
                .map(MenuDO::getId)
                .collect(Collectors.toSet());

        if (CollUtil.isEmpty(buttonIds)) {
            return Collections.emptySet();
        }

        // 3) 为每个按钮补齐父级目录链（type=1/2），否则前端菜单树无法构建
        Set<Long> normalizedIds = new HashSet<>(buttonIds);
        for (Long buttonId : buttonIds) {
            MenuDO current = menuById.get(buttonId);
            Set<Long> visited = new HashSet<>();
            while (current != null
                    && current.getParentId() != null
                    && !Objects.equals(current.getParentId(), MenuDO.ID_ROOT)
                    && visited.add(current.getParentId())) {
                Long parentId = current.getParentId();
                normalizedIds.add(parentId);
                current = menuById.get(parentId);
            }
        }
        return normalizedIds;
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
