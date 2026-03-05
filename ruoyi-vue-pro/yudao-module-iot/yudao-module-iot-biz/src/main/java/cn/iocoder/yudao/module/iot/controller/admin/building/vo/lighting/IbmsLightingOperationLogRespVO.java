package cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 照明操作日志 Response VO")
@Data
public class IbmsLightingOperationLogRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "操作类型 1-开启 2-关闭 3-调光 4-场景切换 5-定时执行")
    private Integer operationType;

    @Schema(description = "操作对象类型 1-回路 2-场景 3-区域")
    private Integer targetType;

    @Schema(description = "操作对象ID")
    private Long targetId;

    @Schema(description = "操作对象名称")
    private String targetName;

    @Schema(description = "操作前状态")
    private String beforeStatus;

    @Schema(description = "操作后状态")
    private String afterStatus;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "操作方式 1-手动 2-自动 3-定时")
    private Integer operateMethod;

    @Schema(description = "操作时间")
    private LocalDateTime operateTime;

    @Schema(description = "备注")
    private String remark;

}
