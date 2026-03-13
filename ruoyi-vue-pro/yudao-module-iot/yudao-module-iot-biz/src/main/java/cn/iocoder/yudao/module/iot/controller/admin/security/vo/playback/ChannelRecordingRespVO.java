package cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ChannelRecordingRespVO {

    @Schema(description = "通道ID")
    private Long channelId;

    @Schema(description = "通道名称")
    private String channelName;

    @Schema(description = "录像片段")
    private List<RecordingSegmentRespVO> segments;
}

