package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 摄像头预设点创建/修改 Request VO")
@Data
public class CameraPresetSaveReqVO {

    @Schema(description = "预设点ID（修改时必填）", example = "1")
    private Long id;

    @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    @NotNull(message = "通道ID不能为空")
    private Long channelId;

    @Schema(description = "预设点编号（1-255）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "预设点编号不能为空")
    @Min(value = 1, message = "预设点编号必须在1-255之间")
    @Max(value = 255, message = "预设点编号必须在1-255之间")
    private Integer presetNo;

    @Schema(description = "预设点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大门入口")
    @NotBlank(message = "预设点名称不能为空")
    private String presetName;

    @Schema(description = "预设点描述", example = "监控大门入口区域")
    private String description;

    @Schema(description = "水平角度（Pan）", example = "45.5")
    private BigDecimal pan;

    @Schema(description = "垂直角度（Tilt）", example = "30.2")
    private BigDecimal tilt;

    @Schema(description = "变焦值（Zoom）", example = "2.5")
    private BigDecimal zoom;

}
