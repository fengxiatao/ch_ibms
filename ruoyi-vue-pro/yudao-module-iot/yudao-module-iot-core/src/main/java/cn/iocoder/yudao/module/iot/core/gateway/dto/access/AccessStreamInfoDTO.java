package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 门禁设备视频流信息 DTO
 * <p>
 * 用于 Gateway 返回给 Biz 的视频流连接信息，
 * 包含 RTSP 连接所需的 IP、端口、认证信息等。
 * <p>
 * Requirements: 2.2, 2.5 - 返回包含 IP、端口、用户名、密码的完整连接信息
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessStreamInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备 IP 地址
     */
    private String ipAddress;

    /**
     * HTTP 端口（用于 RTSP over WebSocket）
     * 大华设备默认为 80
     */
    private Integer httpPort;

    /**
     * RTSP 端口
     * 大华设备默认为 554
     */
    private Integer rtspPort;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 通道号
     * 0 表示设备内置摄像头，1-N 表示外接通道
     */
    private Integer channelNo;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备是否在线
     */
    private Boolean online;

    /**
     * 设备 ID
     */
    private Long deviceId;

    /**
     * 错误信息（如果获取失败）
     */
    private String errorMessage;
}
