package cn.iocoder.yudao.module.iot.controller.admin.access.vo.video;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁设备视频播放参数响应 VO
 * <p>
 * 返回给前端的视频播放参数，包含 WebSocket URL、RTSP URL 和认证信息。
 * 前端使用 DHPlayer 组件播放视频时需要这些参数。
 * <p>
 * Requirements: 2.2, 2.5 - 返回包含 wsURL、rtspURL、username、password 的完整连接信息
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "门禁设备视频播放参数响应 VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessVideoPlayParamsRespVO {

    @Schema(description = "WebSocket URL", example = "ws://192.168.1.207:80/rtspoverwebsocket")
    private String wsURL;

    @Schema(description = "RTSP URL", example = "rtsp://192.168.1.207:80/cam/realmonitor?channel=1&subtype=0")
    private String rtspURL;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "目标地址（IP:端口）", example = "192.168.1.207:80")
    private String target;

    @Schema(description = "设备名称", example = "1号门人脸一体机")
    private String deviceName;

    @Schema(description = "通道名称", example = "内置摄像头")
    private String channelName;

    @Schema(description = "设备是否在线")
    private Boolean online;

    @Schema(description = "设备 ID")
    private Long deviceId;

    @Schema(description = "通道号")
    private Integer channelNo;

    @Schema(description = "错误信息（如果有）")
    private String errorMessage;
}
