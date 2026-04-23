package cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IbmsProductRespVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "产品编码")
    private String productCode;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "专业分组码")
    private String groupCode;

    @Schema(description = "系统码")
    private String systemCode;

    @Schema(description = "型号码")
    private String modelCode;

    @Schema(description = "设备类型码")
    private String deviceTypeCode;

    @Schema(description = "厂商品牌")
    private String manufacturer;

    @Schema(description = "产品型号")
    private String modelNumber;

    @Schema(description = "协议")
    private String protocol;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "点位类型定义")
    private List<IbmsProductPointTypeVO> pointTypes;

    @Schema(description = "设备属性定义")
    private List<IbmsProductPropertyVO> properties;

    @Schema(description = "扩展 JSON（网关/菜单/任务配置等）")
    private String extra;
}

