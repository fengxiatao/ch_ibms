package cn.iocoder.yudao.module.iot.controller.admin.videoinspection.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 视频巡检任务 Response VO")
@Data
public class InspectionTaskRespVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一楼大厅巡检")
    private String taskName;

    @Schema(description = "分屏布局", requiredMode = Schema.RequiredMode.REQUIRED, example = "3x3")
    private String layout;

    @Schema(description = "场景配置")
    private List<InspectionSceneVO> scenes;

    @Schema(description = "任务状态", example = "draft")
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Data
    public static class InspectionSceneVO {
        @Schema(description = "格子索引")
        private Integer cellIndex;

        @Schema(description = "通道列表")
        private List<InspectionChannelVO> channels;
    }

    @Data
    public static class InspectionChannelVO {
        @Schema(description = "设备ID")
        private Long deviceId;

        @Schema(description = "通道ID")
        private Long channelId;

        @Schema(description = "通道名称")
        private String channelName;

        @Schema(description = "播放时长（秒）")
        private Integer duration;

        @Schema(description = "设备IP")
        private String ipAddress;

        @Schema(description = "产品ID")
        private Long productId;

        @Schema(description = "配置信息")
        private String config;

        @Schema(description = "流地址")
        private String streamUrl;

        @Schema(description = "NVR ID")
        private Long nvrId;

        @Schema(description = "通道号")
        private Integer channelNo;

        @Schema(description = "位置信息")
        private String location;

        @Schema(description = "快照地址")
        private String snapshot;
    }
}
