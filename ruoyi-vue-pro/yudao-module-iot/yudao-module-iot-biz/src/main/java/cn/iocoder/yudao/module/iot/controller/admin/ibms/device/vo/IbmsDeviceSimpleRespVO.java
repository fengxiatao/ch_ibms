package cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IbmsDeviceSimpleRespVO {

    @Schema(description = "设备 ID")
    private Long id;

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "关联 ibms_product.id（与历史 iot_device.product_id 对齐迁移）")
    private Long ibmsProductId;

    @Schema(description = "ProductKey")
    private String productKey;

    @Schema(description = "数值型设备类型（兼容物模型/网关）")
    private Integer deviceType;

    @Schema(description = "运行态状态，无运行态行时为 null（可视作未激活）")
    private Integer state;
}
