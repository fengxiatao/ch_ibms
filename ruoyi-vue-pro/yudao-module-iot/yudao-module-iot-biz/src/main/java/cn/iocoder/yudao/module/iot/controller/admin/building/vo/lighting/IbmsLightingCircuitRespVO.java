package cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 照明回路 Response VO")
@Data
public class IbmsLightingCircuitRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "回路编码")
    private String circuitCode;

    @Schema(description = "回路名称")
    private String circuitName;

    @Schema(description = "回路类型 1-普通照明 2-应急照明 3-景观照明 4-调光照明")
    private Integer circuitType;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "控制器ID")
    private Long controllerId;

    @Schema(description = "控制器名称")
    private String controllerName;

    @Schema(description = "额定功率")
    private BigDecimal ratedPower;

    @Schema(description = "当前功率")
    private BigDecimal currentPower;

    @Schema(description = "当前亮度 0-100")
    private Integer brightness;

    @Schema(description = "灯具数量")
    private Integer lightCount;

    @Schema(description = "状态 0-关闭 1-开启 2-故障")
    private Integer status;

    @Schema(description = "最后操作时间")
    private LocalDateTime lastOperateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
