package cn.iocoder.yudao.module.iot.controller.admin.product;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 产品定时任务配置 Controller
 * 
 * <p>功能说明：
 * <ul>
 *   <li>获取产品的定时任务配置</li>
 *   <li>保存产品的定时任务配置</li>
 *   <li>删除产品的定时任务配置</li>
 * </ul>
 * 
 * @author IBMS Team
 */
@Tag(name = "管理后台 - 产品定时任务配置")
@RestController
@RequestMapping("/iot/product/job-config")
@Validated
public class ProductJobConfigController {
    
    @Resource
    private IotProductService productService;
    
    /**
     * 获取产品的定时任务配置
     * 
     * @param id 产品ID
     * @return 任务配置JSON字符串
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取产品定时任务配置")
    @Parameter(name = "id", description = "产品编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product:query')")
    public CommonResult<String> getJobConfig(@PathVariable("id") Long id) {
        IotProductDO product = productService.validateProductExists(id);
        String jobConfig = product.getJobConfig();
        
        // 如果没有配置，返回空对象JSON
        if (StrUtil.isBlank(jobConfig)) {
            jobConfig = "{}";
        }
        
        return success(jobConfig);
    }
    
    /**
     * 保存产品的定时任务配置
     * 
     * @param id 产品ID
     * @param jobConfig 任务配置JSON字符串
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "保存产品定时任务配置")
    @Parameter(name = "id", description = "产品编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product:update')")
    public CommonResult<Boolean> saveJobConfig(
            @PathVariable("id") Long id,
            @RequestBody String jobConfig) {
        
        // 验证产品存在
        productService.validateProductExists(id);
        
        // 更新配置
        productService.updateProductJobConfig(id, jobConfig);
        
        return success(true);
    }
    
    /**
     * 删除产品的定时任务配置
     * 
     * @param id 产品ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除产品定时任务配置")
    @Parameter(name = "id", description = "产品编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product:update')")
    public CommonResult<Boolean> deleteJobConfig(@PathVariable("id") Long id) {
        
        // 验证产品存在
        productService.validateProductExists(id);
        
        // 删除配置（设置为NULL）
        productService.updateProductJobConfig(id, null);
        
        return success(true);
    }
}

