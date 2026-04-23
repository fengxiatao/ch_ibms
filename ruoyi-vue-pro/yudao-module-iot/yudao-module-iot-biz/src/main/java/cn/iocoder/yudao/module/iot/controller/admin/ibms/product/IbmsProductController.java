package cn.iocoder.yudao.module.iot.controller.admin.ibms.product;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductSaveReqVO;
import cn.iocoder.yudao.module.iot.service.ibms.product.IbmsProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IBMS 产品管理
 */
@Tag(name = "管理后台 - IBMS 产品管理")
@RestController
@RequestMapping("/iot/ibms/product")
@Validated
public class IbmsProductController {

    @Resource
    private IbmsProductService ibmsProductService;

    @PostMapping("/create")
    @Operation(summary = "创建 IBMS 产品")
    @PreAuthorize("@ss.hasPermission('iot:ibms-product:create')")
    public CommonResult<Long> createProduct(@Valid @RequestBody IbmsProductSaveReqVO reqVO) {
        return success(ibmsProductService.createProduct(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 IBMS 产品")
    @PreAuthorize("@ss.hasPermission('iot:ibms-product:update')")
    public CommonResult<Boolean> updateProduct(@Valid @RequestBody IbmsProductSaveReqVO reqVO) {
        ibmsProductService.updateProduct(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 IBMS 产品")
    @PreAuthorize("@ss.hasPermission('iot:ibms-product:delete')")
    public CommonResult<Boolean> deleteProduct(@RequestParam("id") Long id) {
        ibmsProductService.deleteProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取 IBMS 产品详情")
    @PreAuthorize("@ss.hasPermission('iot:ibms-product:query')")
    public CommonResult<IbmsProductRespVO> getProduct(@RequestParam("id") Long id) {
        return success(ibmsProductService.getProduct(id));
    }

    @GetMapping("/resolve-template-for-device")
    @Operation(summary = "按设备维度解析产品模板（扩展属性定义）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-product:query')")
    public CommonResult<IbmsProductRespVO> resolveTemplateForDevice(@RequestParam("groupCode") String groupCode,
                                                                    @RequestParam("systemCode") String systemCode,
                                                                    @RequestParam("deviceTypeCode") String deviceTypeCode,
                                                                    @RequestParam("modelNumber") String modelNumber) {
        return success(ibmsProductService.getProductTemplateForDevice(groupCode, systemCode, deviceTypeCode, modelNumber));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询 IBMS 产品")
    @PreAuthorize("@ss.hasPermission('iot:ibms-product:query')")
    public CommonResult<PageResult<IbmsProductRespVO>> getProductPage(@Valid IbmsProductPageReqVO pageReqVO) {
        return success(ibmsProductService.getProductPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出 IBMS 产品 Excel（预留）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExcel() {
        // 预留出口，如后续需要导出时再实现
        throw new UnsupportedOperationException("IBMS 产品导出暂未实现");
    }
}

