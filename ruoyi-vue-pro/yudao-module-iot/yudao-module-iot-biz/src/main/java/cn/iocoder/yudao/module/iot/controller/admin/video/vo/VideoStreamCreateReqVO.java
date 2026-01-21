package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理后台 - IoT 视频流创建 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 视频流创建 Request VO")
@Data
public class VideoStreamCreateReqVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "流类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "live")
    @NotNull(message = "流类型不能为空")
    private String streamType; // live: 实时预览, playback: 录像回放

    @Schema(description = "开始时间（回放时使用）", example = "2025-10-26T10:00:00")
    private String startTime;

    @Schema(description = "结束时间（回放时使用）", example = "2025-10-26T12:00:00")
    private String endTime;
}












