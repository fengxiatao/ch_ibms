package cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaybackClipRespVO {

    @Schema(description = "通道ID")
    private Long cameraId;

    @Schema(description = "通道名称")
    private String cameraName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "下载地址（可为空）")
    private String fileUrl;

    @Schema(description = "文件路径（可为空）")
    private String filePath;
}

