package cn.iocoder.yudao.module.iot.newgateway.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 通道信息
 * <p>
 * 用于 newgateway 与 biz 层之间的消息传输，表示设备通道的通用数据结构。
 * </p>
 *
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1DoorInfo
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.AccessGen2DoorInfo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelInfo {

    /**
     * 通道号
     */
    private Integer channelNo;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 通道类型
     * <ul>
     *     <li>ACCESS - 门禁通道</li>
     *     <li>VIDEO - 视频通道</li>
     *     <li>ALARM - 报警通道</li>
     * </ul>
     */
    private String channelType;

    /**
     * 状态
     * <ul>
     *     <li>0 - 离线/关闭</li>
     *     <li>1 - 在线/打开</li>
     *     <li>2 - 未知</li>
     * </ul>
     */
    private Integer status;

    /**
     * 能力信息
     * <p>
     * 包含通道的能力标识，如：
     * <ul>
     *     <li>hasCard - 是否支持刷卡</li>
     *     <li>hasFace - 是否支持人脸识别</li>
     *     <li>hasFingerprint - 是否支持指纹识别</li>
     * </ul>
     * </p>
     */
    @Builder.Default
    private Map<String, Object> capabilities = new HashMap<>();

    /**
     * 通道类型常量：门禁通道
     */
    public static final String TYPE_ACCESS = "ACCESS";

    /**
     * 通道类型常量：视频通道
     */
    public static final String TYPE_VIDEO = "VIDEO";

    /**
     * 通道类型常量：报警通道
     */
    public static final String TYPE_ALARM = "ALARM";

    /**
     * 状态常量：离线/关闭
     */
    public static final int STATUS_OFFLINE = 0;

    /**
     * 状态常量：在线/打开
     */
    public static final int STATUS_ONLINE = 1;

    /**
     * 状态常量：未知
     */
    public static final int STATUS_UNKNOWN = 2;

    /**
     * 创建门禁通道信息
     *
     * @param channelNo   通道号
     * @param channelName 通道名称
     * @param status      状态
     * @param hasCard     是否支持刷卡
     * @param hasFace     是否支持人脸
     * @param hasFingerprint 是否支持指纹
     * @return 通道信息
     */
    public static ChannelInfo createAccessChannel(Integer channelNo, String channelName, Integer status,
                                                   boolean hasCard, boolean hasFace, boolean hasFingerprint) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("hasCard", hasCard);
        capabilities.put("hasFace", hasFace);
        capabilities.put("hasFingerprint", hasFingerprint);

        return ChannelInfo.builder()
                .channelNo(channelNo)
                .channelName(channelName)
                .channelType(TYPE_ACCESS)
                .status(status)
                .capabilities(capabilities)
                .build();
    }

    /**
     * 创建视频通道信息
     *
     * @param channelNo   通道号
     * @param channelName 通道名称
     * @param status      状态
     * @return 通道信息
     */
    public static ChannelInfo createVideoChannel(Integer channelNo, String channelName, Integer status) {
        return ChannelInfo.builder()
                .channelNo(channelNo)
                .channelName(channelName)
                .channelType(TYPE_VIDEO)
                .status(status)
                .capabilities(new HashMap<>())
                .build();
    }

    /**
     * 创建报警通道信息
     *
     * @param channelNo   通道号
     * @param channelName 通道名称
     * @param status      状态
     * @return 通道信息
     */
    public static ChannelInfo createAlarmChannel(Integer channelNo, String channelName, Integer status) {
        return ChannelInfo.builder()
                .channelNo(channelNo)
                .channelName(channelName)
                .channelType(TYPE_ALARM)
                .status(status)
                .capabilities(new HashMap<>())
                .build();
    }

    /**
     * 检查通道信息是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return channelNo != null && channelType != null;
    }
}
