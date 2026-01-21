package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 摄像头预设点 Response VO")
@Data
public class CameraPresetRespVO {

    @Schema(description = "预设点ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long channelId;

    @Schema(description = "预设点编号（1-255）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer presetNo;

    @Schema(description = "预设点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大门入口")
    private String presetName;

    @Schema(description = "预设点描述", example = "监控大门入口区域")
    private String description;

    @Schema(description = "水平角度（Pan）", example = "45.5")
    private BigDecimal pan;

    @Schema(description = "垂直角度（Tilt）", example = "30.2")
    private BigDecimal tilt;

    @Schema(description = "变焦值（Zoom）", example = "2.5")
    private BigDecimal zoom;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
