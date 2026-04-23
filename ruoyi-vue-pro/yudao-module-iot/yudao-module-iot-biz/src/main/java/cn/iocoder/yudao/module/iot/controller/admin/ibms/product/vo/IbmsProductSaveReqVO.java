package cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class IbmsProductSaveReqVO {

    @Schema(description = "ID，更新时必填")
    private Long id;

    @Schema(description = "专业分组码 SA/ST/SB/SE/SF", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String groupCode;

    @Schema(description = "系统码 VI/AC/BA/...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String systemCode;

    @Schema(description = "型号码 DS/DP/...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String modelCode;

    @Schema(description = "设备类型码 CAM/READER/...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String deviceTypeCode;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String productName;

    @Schema(description = "厂商品牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String manufacturer;

    @Schema(description = "产品型号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String modelNumber;

    @Schema(description = "通信协议")
    private String protocol;

    @Schema(description = "图标样式")
    private String icon;

    @Schema(description = "产品描述")
    private String description;

    @Schema(description = "点位类型定义", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private List<@NotNull IbmsProductPointTypeVO> pointTypes;

    @Schema(description = "设备属性定义")
    private List<IbmsProductPropertyVO> properties;

    @Schema(description = "扩展 JSON（codecType、jobConfig、menuIds 等），见 IbmsProductExtra 约定")
    private String extra;
}

