package cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RecordingSegmentRespVO {

    @Schema(description = "开始时间（yyyy-MM-dd HH:mm:ss）")
    private String startTime;

    @Schema(description = "结束时间（yyyy-MM-dd HH:mm:ss）")
    private String endTime;

    @Schema(description = "是否有录像")
    private Boolean hasRecording;
}

