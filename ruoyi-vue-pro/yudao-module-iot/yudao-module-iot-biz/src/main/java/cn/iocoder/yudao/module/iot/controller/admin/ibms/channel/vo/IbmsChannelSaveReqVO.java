package cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IbmsChannelSaveReqVO {

    @Schema(description = "ID，更新时必填")
    private Long id;

    @Schema(description = "空间ID（可为空表示未分配）")
    private Long spaceId;

    @Schema(description = "设备ID（可为空）")
    private Long deviceId;

    @Schema(description = "通道编码，如 F01-LBY-VI-VT-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String code;

    @Schema(description = "通道号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer channelNo;

    @Schema(description = "通道名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "业务分类：security/access/alarm/parking/building/environment/lighting/energy", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String business;

    @Schema(description = "通道类型码（点位类型码），如 VT/DR/DI/...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String typeCode;

    @Schema(description = "通道类别（展示文案）")
    private String category;

    @Schema(description = "系统类型，如 VI/AC/AL/BA/EN...")
    private String systemType;

    @Schema(description = "数据源，如 NVR/CTR/GW/DDC/Meter")
    private String dataSource;

    @Schema(description = "IP 地址")
    private String ip;

    @Schema(description = "MAC 地址")
    private String mac;

    @Schema(description = "设备序列号（冗余）")
    private String deviceSn;

    @Schema(description = "所属设备名称（冗余）")
    private String deviceName;

    @Schema(description = "空间位置文案（冗余）")
    private String space;

    @Schema(description = "当前值")
    private String currentValue;

    @Schema(description = "状态：online/offline/warning/armed", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String status;

    @Schema(description = "扩展 JSON 字符串")
    private String extra;
}

