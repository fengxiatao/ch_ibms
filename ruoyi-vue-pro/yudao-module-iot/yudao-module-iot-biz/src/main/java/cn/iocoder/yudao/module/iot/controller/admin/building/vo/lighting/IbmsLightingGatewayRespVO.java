package cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 照明网关 Response VO")
@Data
public class IbmsLightingGatewayRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "网关编码")
    private String gatewayCode;

    @Schema(description = "网关名称")
    private String gatewayName;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "端口")
    private Integer port;

    @Schema(description = "协议类型")
    private String protocolType;

    @Schema(description = "安装位置")
    private String installLocation;

    @Schema(description = "状态 0-离线 1-在线 2-故障")
    private Integer status;

    @Schema(description = "最后通讯时间")
    private LocalDateTime lastCommunicateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
