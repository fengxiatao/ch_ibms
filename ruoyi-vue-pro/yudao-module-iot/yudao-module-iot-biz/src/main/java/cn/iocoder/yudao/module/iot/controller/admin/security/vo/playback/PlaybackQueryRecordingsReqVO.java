package cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询录像片段 Request VO
 *
 * <p>为兼容前端：startTime/endTime 使用字符串（yyyy-MM-dd HH:mm:ss）。</p>
 */
@Data
public class PlaybackQueryRecordingsReqVO {

    @Schema(description = "通道ID（前端 cameraId）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "cameraId 不能为空")
    private Long cameraId;

    @Schema(description = "开始时间（yyyy-MM-dd HH:mm:ss）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String startTime;

    @Schema(description = "结束时间（yyyy-MM-dd HH:mm:ss）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String endTime;

    @Schema(description = "录像类型（可选）")
    private Integer recordType;
}

