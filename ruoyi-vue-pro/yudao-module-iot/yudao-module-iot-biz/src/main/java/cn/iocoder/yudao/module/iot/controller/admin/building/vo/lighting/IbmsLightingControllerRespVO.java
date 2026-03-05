package cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 照明控制器 Response VO")
@Data
public class IbmsLightingControllerRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "控制器编码")
    private String controllerCode;

    @Schema(description = "控制器名称")
    private String controllerName;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "网关ID")
    private Long gatewayId;

    @Schema(description = "网关名称")
    private String gatewayName;

    @Schema(description = "通道数量")
    private Integer channelCount;

    @Schema(description = "是否支持调光")
    private Boolean dimmable;

    @Schema(description = "安装位置")
    private String installLocation;

    @Schema(description = "状态 0-离线 1-在线 2-故障")
    private Integer status;

    @Schema(description = "最后通讯时间")
    private LocalDateTime lastCommunicateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
