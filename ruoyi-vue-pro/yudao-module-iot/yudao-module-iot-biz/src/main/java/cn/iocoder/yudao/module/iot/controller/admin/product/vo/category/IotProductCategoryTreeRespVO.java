package cn.iocoder.yudao.module.iot.controller.admin.product.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * IoT 产品分类树形 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 产品分类树形 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotProductCategoryTreeRespVO {

    @Schema(description = "分类 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "智慧建筑")
    private String name;

    @Schema(description = "父分类 ID", example = "0")
    private Long parentId;

    @Schema(description = "分类层级", example = "1")
    private Integer level;

    @Schema(description = "模块编码", example = "building")
    private String moduleCode;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "描述", example = "智慧建筑相关设备分类")
    private String description;

    @Schema(description = "子分类列表")
    private List<IotProductCategoryTreeRespVO> children;

}
















