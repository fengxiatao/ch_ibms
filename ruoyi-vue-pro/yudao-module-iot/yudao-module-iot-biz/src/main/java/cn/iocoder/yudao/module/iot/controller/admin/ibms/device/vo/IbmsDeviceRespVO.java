package cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IbmsDeviceRespVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "专业分组码")
    private String groupCode;

    @Schema(description = "系统码")
    private String systemCode;

    @Schema(description = "设备类型码")
    private String deviceTypeCode;

    @Schema(description = "品牌码")
    private String brand;

    @Schema(description = "产品型号")
    private String productModel;

    @Schema(description = "接入类型")
    private String accessType;

    @Schema(description = "IP 地址")
    private String ip;

    @Schema(description = "接入协议")
    private String protocol;

    @Schema(description = "设备序列号")
    private String sn;

    @Schema(description = "ProductKey")
    private String productKey;

    @Schema(description = "关联 ibms_product.id（与历史 iot_device.product_id 迁移对齐）")
    private Long ibmsProductId;

    @Schema(description = "设备运行态（ibms_device_runtime.state），无运行态行时为空")
    private Integer state;

    @Schema(description = "数值型设备类型（兼容物模型/网关）")
    private Integer deviceType;

    @Schema(description = "通道总数")
    private Integer pointCount;

    @Schema(description = "在线通道数")
    private Integer pointsOnline;

    @Schema(description = "告警通道数")
    private Integer pointsAlarm;

    @Schema(description = "空间位置展示文案")
    private String space;

    @Schema(description = "扩展 JSON 字符串")
    private String extra;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

