package cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaybackClipReqVO {

    @Schema(description = "通道ID（前端 cameraId）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "cameraId 不能为空")
    private Long cameraId;

    @Schema(description = "开始时间（yyyy-MM-dd HH:mm:ss）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String startTime;

    @Schema(description = "结束时间（yyyy-MM-dd HH:mm:ss）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String endTime;
}

