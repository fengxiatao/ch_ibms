package cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.scene;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "管理后台 - 轮巡场景通道创建/更新 Request VO")
@Data
public class PatrolSceneChannelSaveReqVO {

    @Schema(description = "通道ID", example = "1")
    private Long id;

    @Schema(description = "场景ID", example = "1")
    private Long sceneId;

    @Schema(description = "窗格位置", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "窗格位置不能为空")
    private Integer gridPosition;

    @Schema(description = "通道播放时长（秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "通道播放时长不能为空")
    private Integer duration;

    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    @Schema(description = "设备ID", example = "DEV001")
    private String deviceId;

    @Schema(description = "通道号", example = "1")
    private Integer channelNo;

    @Schema(description = "通道名称", example = "大堂入口摄像头")
    private String channelName;

    @Schema(description = "目标IP", example = "192.168.1.100")
    private String targetIp;

    @Schema(description = "目标通道号", example = "1")
    private Integer targetChannelNo;

    @Schema(description = "主码流地址", example = "rtsp://192.168.1.100/main")
    private String streamUrlMain;

    @Schema(description = "子码流地址", example = "rtsp://192.168.1.100/sub")
    private String streamUrlSub;

    @Schema(description = "配置信息（JSON）", example = "{\"preset\":1}")
    private Map<String, Object> config;

}
