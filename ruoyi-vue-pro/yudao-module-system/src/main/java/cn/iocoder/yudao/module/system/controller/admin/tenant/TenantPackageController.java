package cn.iocoder.yudao.module.system.controller.admin.tenant;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages.TenantPackagePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages.TenantPackageRespVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages.TenantPackageSaveReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages.TenantPackageSimpleRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantPackageDO;
import cn.iocoder.yudao.module.system.service.tenant.TenantPackageService;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 租户套餐")
@RestController
@RequestMapping("/system/tenant-package")
@Validated
public class TenantPackageController {

    @Resource
    private TenantPackageService tenantPackageService;

    @Resource
    private TenantService tenantService;

    @PostMapping("/create")
    @Operation(summary = "创建租户套餐")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:create')")
    public CommonResult<Long> createTenantPackage(@Valid @RequestBody TenantPackageSaveReqVO createReqVO) {
        return success(tenantPackageService.createTenantPackage(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新租户套餐")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:update')")
    public CommonResult<Boolean> updateTenantPackage(@Valid @RequestBody TenantPackageSaveReqVO updateReqVO) {
        tenantPackageService.updateTenantPackage(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除租户套餐")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:tenant-package:delete')")
    public CommonResult<Boolean> deleteTenantPackage(@RequestParam("id") Long id) {
        tenantPackageService.deleteTenantPackage(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @Operation(summary = "批量删除租户套餐")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:delete')")
    public CommonResult<Boolean> deleteTenantPackageList(@RequestParam("ids") List<Long> ids) {
        tenantPackageService.deleteTenantPackageList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得租户套餐")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:query')")
    public CommonResult<TenantPackageRespVO> getTenantPackage(@RequestParam("id") Long id) {
        TenantPackageDO tenantPackage = tenantPackageService.getTenantPackage(id);
        return success(BeanUtils.toBean(tenantPackage, TenantPackageRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得租户套餐分页")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:query')")
    public CommonResult<PageResult<TenantPackageRespVO>> getTenantPackagePage(@Valid TenantPackagePageReqVO pageVO) {
        PageResult<TenantPackageDO> pageResult = tenantPackageService.getTenantPackagePage(pageVO);
        return success(BeanUtils.toBean(pageResult, TenantPackageRespVO.class));
    }

    @GetMapping({"/get-simple-list", "simple-list"})
    @Operation(summary = "获取租户套餐精简信息列表", description = "只包含被开启的租户套餐，主要用于前端的下拉选项")
    public CommonResult<List<TenantPackageSimpleRespVO>> getTenantPackageList() {
        List<TenantPackageDO> list = tenantPackageService.getTenantPackageListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(BeanUtils.toBean(list, TenantPackageSimpleRespVO.class));
    }

    @PostMapping("/refresh-all-tenant-role-menu")
    @Operation(summary = "重新推送所有租户套餐的菜单到租户管理员角色", description = "在批量修正 system_menu 权限后，用此接口按套餐重新落库各租户的 system_role_menu 并清权限缓存。")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:refresh')")
    @TenantIgnore
    public CommonResult<Integer> refreshAllTenantRoleMenu() {
        List<TenantPackageDO> packages = tenantPackageService.getTenantPackageListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        int count = 0;
        for (TenantPackageDO pkg : packages) {
            Set<Long> menuIds = pkg.getMenuIds();
            if (menuIds == null) {
                continue;
            }
            List<TenantDO> tenants = tenantService.getTenantListByPackageId(pkg.getId());
            for (TenantDO tenant : tenants) {
                TenantUtils.execute(
                        tenant.getId(), () -> tenantService.updateTenantRoleMenu(tenant.getId(), menuIds));
                count++;
            }
        }
        return success(count);
    }

    @PostMapping("/resync-role-menu")
    @Operation(summary = "按指定套餐重新同步该套餐下所有租户的租户管理员菜单",
            description = "用于修正历史租户 system_role_menu 与套餐当前 menuIds 不一致的数据漂移；复用 system:tenant-package:update 权限，避免新增权限串。")
    @Parameter(name = "id", description = "租户套餐编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:update')")
    @TenantIgnore
    public CommonResult<Integer> resyncTenantRoleMenu(@RequestParam("id") Long id) {
        TenantPackageDO pkg = tenantPackageService.getTenantPackage(id);
        if (pkg == null || pkg.getMenuIds() == null) {
            return success(0);
        }
        Set<Long> menuIds = pkg.getMenuIds();
        List<TenantDO> tenants = tenantService.getTenantListByPackageId(pkg.getId());
        int count = 0;
        for (TenantDO tenant : tenants) {
            TenantUtils.execute(
                    tenant.getId(), () -> tenantService.updateTenantRoleMenu(tenant.getId(), menuIds));
            count++;
        }
        return success(count);
    }

}
