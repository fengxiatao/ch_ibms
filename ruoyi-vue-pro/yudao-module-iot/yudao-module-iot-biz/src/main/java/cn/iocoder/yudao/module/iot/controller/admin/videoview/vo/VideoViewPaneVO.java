package cn.iocoder.yudao.module.iot.controller.admin.videoview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 视频监控 - 实时预览视图窗格 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 视频监控视图窗格 VO")
@Data
public class VideoViewPaneVO {

    @Schema(description = "窗格索引(0-15)", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer paneIndex;

    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "通道号", example = "1")
    private Integer channelNo;

    @Schema(description = "通道名称", example = "大厅摄像头1")
    private String channelName;

    @Schema(description = "目标IP", example = "192.168.1.202")
    private String targetIp;

    @Schema(description = "目标通道号", example = "1")
    private Integer targetChannelNo;

    @Schema(description = "主码流URL", example = "rtsp://admin:admin123@192.168.1.202:554/...")
    private String streamUrlMain;

    @Schema(description = "子码流URL", example = "rtsp://admin:admin123@192.168.1.202:554/...")
    private String streamUrlSub;

    @Schema(description = "其他配置(JSON格式)")
    private String config;

}
