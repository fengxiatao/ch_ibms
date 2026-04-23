package cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IbmsChannelRespVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "空间ID")
    private Long spaceId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "通道编码")
    private String code;

    @Schema(description = "通道号")
    private Integer channelNo;

    @Schema(description = "通道名称")
    private String name;

    @Schema(description = "业务分类")
    private String business;

    @Schema(description = "通道类型码（点位类型码）")
    private String typeCode;

    @Schema(description = "通道类别")
    private String category;

    @Schema(description = "系统类型")
    private String systemType;

    @Schema(description = "数据源")
    private String dataSource;

    @Schema(description = "IP 地址")
    private String ip;

    @Schema(description = "MAC 地址")
    private String mac;

    @Schema(description = "设备序列号")
    private String deviceSn;

    @Schema(description = "所属设备名称")
    private String deviceName;

    @Schema(description = "空间位置文案")
    private String space;

    @Schema(description = "当前值")
    private String currentValue;

    @Schema(description = "状态：online/offline/warning/armed")
    private String status;

    @Schema(description = "扩展 JSON")
    private String extra;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

