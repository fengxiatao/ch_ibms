package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * GIS 聚合 Response VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 聚合 Response VO")
@Data
public class IotGisClusterRespVO {

    @Schema(description = "聚合中心经度", example = "113.264385")
    private BigDecimal longitude;

    @Schema(description = "聚合中心纬度", example = "23.129112")
    private BigDecimal latitude;

    @Schema(description = "聚合设备数量", example = "15")
    private Integer count;

    @Schema(description = "是否为聚合", example = "true")
    private Boolean isCluster;

    @Schema(description = "聚合的设备ID列表")
    private List<Long> deviceIds;

    @Schema(description = "在线设备数", example = "12")
    private Integer onlineCount;

    @Schema(description = "离线设备数", example = "3")
    private Integer offlineCount;

    @Schema(description = "告警设备数", example = "1")
    private Integer alarmCount;
}












