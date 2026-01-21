package cn.iocoder.yudao.module.iot.controller.admin.product;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategoryPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategoryRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategorySaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategoryTreeRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategoryModuleVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductCategoryDO;
import cn.iocoder.yudao.module.iot.enums.product.IotProductCategoryModuleEnum;
import cn.iocoder.yudao.module.iot.service.product.IotProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 产品分类")
@RestController
@RequestMapping("/iot/product-category")
@Validated
public class IotProductCategoryController {

    @Resource
    private IotProductCategoryService productCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建产品分类")
    @PreAuthorize("@ss.hasPermission('iot:product-category:create')")
    public CommonResult<Long> createProductCategory(@Valid @RequestBody IotProductCategorySaveReqVO createReqVO) {
        return success(productCategoryService.createProductCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品分类")
    @PreAuthorize("@ss.hasPermission('iot:product-category:update')")
    public CommonResult<Boolean> updateProductCategory(@Valid @RequestBody IotProductCategorySaveReqVO updateReqVO) {
        productCategoryService.updateProductCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product-category:delete')")
    public CommonResult<Boolean> deleteProductCategory(@RequestParam("id") Long id) {
        productCategoryService.deleteProductCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:product-category:query')")
    public CommonResult<IotProductCategoryRespVO> getProductCategory(@RequestParam("id") Long id) {
        IotProductCategoryDO productCategory = productCategoryService.getProductCategory(id);
        return success(BeanUtils.toBean(productCategory, IotProductCategoryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品分类分页")
    @PreAuthorize("@ss.hasPermission('iot:product-category:query')")
    public CommonResult<PageResult<IotProductCategoryRespVO>> getProductCategoryPage(@Valid IotProductCategoryPageReqVO pageReqVO) {
        PageResult<IotProductCategoryDO> pageResult = productCategoryService.getProductCategoryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotProductCategoryRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得所有产品分类列表")
    @PreAuthorize("@ss.hasPermission('iot:product-category:query')")
    public CommonResult<List<IotProductCategoryRespVO>> getSimpleProductCategoryList() {
        List<IotProductCategoryDO> list = productCategoryService.getProductCategoryListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, category ->
                new IotProductCategoryRespVO().setId(category.getId()).setName(category.getName())));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取产品分类树")
    @PreAuthorize("@ss.hasPermission('iot:product-category:query')")
    public CommonResult<List<IotProductCategoryTreeRespVO>> getProductCategoryTree() {
        List<IotProductCategoryDO> list = productCategoryService.getProductCategoryTree();
        return success(buildCategoryTree(list));
    }

    @GetMapping("/tree-by-module")
    @Operation(summary = "根据模块获取分类树")
    @Parameter(name = "moduleCode", description = "模块编码", required = true, example = "building")
    @PreAuthorize("@ss.hasPermission('iot:product-category:query')")
    public CommonResult<List<IotProductCategoryTreeRespVO>> getProductCategoryTreeByModule(
            @RequestParam("moduleCode") String moduleCode) {
        List<IotProductCategoryDO> list = productCategoryService.getProductCategoryTreeByModule(moduleCode);
        return success(buildCategoryTree(list));
    }

    @GetMapping("/children")
    @Operation(summary = "获取子分类列表")
    @Parameter(name = "parentId", description = "父分类ID", required = true, example = "1000")
    @PreAuthorize("@ss.hasPermission('iot:product-category:query')")
    public CommonResult<List<IotProductCategoryTreeRespVO>> getChildCategories(
            @RequestParam("parentId") Long parentId) {
        List<IotProductCategoryDO> list = productCategoryService.getChildCategories(parentId);
        return success(BeanUtils.toBean(list, IotProductCategoryTreeRespVO.class));
    }

    @GetMapping("/modules")
    @Operation(summary = "获取 IBMS 智慧模块列表")
    @PreAuthorize("@ss.hasPermission('iot:product-category:query')")
    public CommonResult<List<IotProductCategoryModuleVO>> getIbmsModules() {
        // 从枚举中获取模块列表（也可以从菜单系统动态获取）
        List<IotProductCategoryModuleVO> modules = new ArrayList<>();
        for (IotProductCategoryModuleEnum module : IotProductCategoryModuleEnum.values()) {
            modules.add(new IotProductCategoryModuleVO(
                    module.getCode(),
                    module.getName(),
                    getModuleIcon(module.getCode()),
                    "/" + module.getCode(),
                    module.ordinal() + 1
            ));
        }
        return success(modules);
    }

    /**
     * 获取模块图标（根据模块编码）
     */
    private String getModuleIcon(String moduleCode) {
        return switch (moduleCode) {
            case "building" -> "fa:building";
            case "security" -> "fa:shield";
            case "access" -> "fa:key";
            case "fire" -> "fa:fire-extinguisher";
            case "energy" -> "fa:bolt";
            case "infrastructure" -> "fa:network-wired";
            default -> "fa:cube";
        };
    }

    /**
     * 构建分类树形结构
     *
     * @param list 分类列表
     * @return 树形结构
     */
    private List<IotProductCategoryTreeRespVO> buildCategoryTree(List<IotProductCategoryDO> list) {
        // 1. 转换为 VO
        List<IotProductCategoryTreeRespVO> voList = BeanUtils.toBean(list, IotProductCategoryTreeRespVO.class);
        
        // 2. 构建 Map，方便查找
        Map<Long, IotProductCategoryTreeRespVO> map = new HashMap<>();
        List<IotProductCategoryTreeRespVO> result = new ArrayList<>();
        
        for (IotProductCategoryTreeRespVO vo : voList) {
            map.put(vo.getId(), vo);
            vo.setChildren(new ArrayList<>());
        }
        
        // 3. 构建树形结构
        for (IotProductCategoryTreeRespVO vo : voList) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                // 顶层分类
                result.add(vo);
            } else {
                // 子分类，添加到父分类的 children 中
                IotProductCategoryTreeRespVO parent = map.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }
        
        return result;
    }

}