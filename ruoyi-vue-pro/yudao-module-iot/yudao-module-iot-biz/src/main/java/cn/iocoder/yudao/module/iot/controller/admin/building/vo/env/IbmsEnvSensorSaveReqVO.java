package cn.iocoder.yudao.module.iot.controller.admin.building.vo.env;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 环境传感器新增/修改 Request VO")
@Data
public class IbmsEnvSensorSaveReqVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "传感器编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "传感器编码不能为空")
    private String sensorCode;

    @Schema(description = "传感器名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "传感器名称不能为空")
    private String sensorName;

    @Schema(description = "传感器类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "传感器类型不能为空")
    private Integer sensorType;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "安装位置")
    private String installLocation;

    @Schema(description = "安装时间")
    private LocalDateTime installTime;

    @Schema(description = "采集周期（秒）")
    private Integer collectInterval;

}
