package cn.iocoder.yudao.module.iot.controller.admin.product.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 产品分类模块 VO
 * 用于前端下拉选择 IBMS 模块
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 产品分类模块 VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotProductCategoryModuleVO {

    @Schema(description = "模块编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "building")
    private String code;

    @Schema(description = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "智慧建筑")
    private String name;

    @Schema(description = "模块图标", example = "fa:building")
    private String icon;

    @Schema(description = "模块路径", example = "/building")
    private String path;

    @Schema(description = "排序", example = "1")
    private Integer sort;

}
















