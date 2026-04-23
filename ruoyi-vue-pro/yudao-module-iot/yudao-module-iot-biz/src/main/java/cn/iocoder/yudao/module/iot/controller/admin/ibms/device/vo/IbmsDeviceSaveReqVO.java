package cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IbmsDeviceSaveReqVO {

    @Schema(description = "ID，更新时必填")
    private Long id;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "专业分组码 SA/ST/SB/SE/SF/GW", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String groupCode;

    @Schema(description = "系统码 VI/AC/BA/...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String systemCode;

    @Schema(description = "设备类型码 CAM/NVR/CTR/...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String deviceTypeCode;

    @Schema(description = "品牌码 HIK/DAH/ZKT/JOH 等", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String brand;

    @Schema(description = "接入类型：IP/RS485/韦根/无线/模拟量/开关量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String accessType;

    @Schema(description = "产品型号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String productModel;

    @Schema(description = "IP 地址")
    private String ip;

    @Schema(description = "接入协议：ONVIF/GB28181/Modbus TCP/BACnet/MQTT 等")
    private String protocol;

    @Schema(description = "空间位置展示/检索用文案（不参与设备编码生成），例如 F01 大堂")
    private String space;

    @Schema(description = "设备编码序号（3 位），用于生成 deviceCode，例如 001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer seq;

    @Schema(description = "扩展配置 JSON 字符串（键值与产品属性 propName 对齐，由设备表单动态区写入）")
    private String extra;
}

