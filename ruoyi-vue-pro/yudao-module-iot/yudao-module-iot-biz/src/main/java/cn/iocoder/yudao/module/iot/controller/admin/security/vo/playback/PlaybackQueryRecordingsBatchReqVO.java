package cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 查询录像片段（批量） Request VO
 *
 * <p>为兼容前端：startTime/endTime 使用字符串（yyyy-MM-dd HH:mm:ss）。</p>
 */
@Data
public class PlaybackQueryRecordingsBatchReqVO {

    @Schema(description = "通道ID列表（前端 cameraId）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "cameraIds 不能为空")
    private List<Long> cameraIds;

    @Schema(description = "开始时间（yyyy-MM-dd HH:mm:ss）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String startTime;

    @Schema(description = "结束时间（yyyy-MM-dd HH:mm:ss）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String endTime;

    @Schema(description = "录像类型（可选）")
    private Integer recordType;
}

