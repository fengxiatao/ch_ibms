package cn.iocoder.yudao.module.iot.newgateway.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通道同步消息（newgateway → biz）
 * <p>
 * 用于 newgateway 向 biz 层发送通道同步结果的消息格式。
 * </p>
 *
 * @author IoT Gateway Team
 * @see ChannelInfo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelSyncMessage {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备Key
     */
    private String deviceKey;

    /**
     * 服务标识
     * <p>
     * 对于通道查询，值为 "QUERY_CHANNELS"
     * </p>
     */
    private String serviceIdentifier;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 通道列表
     */
    @Builder.Default
    private List<ChannelInfo> channelList = new ArrayList<>();

    /**
     * 查询时间
     */
    private LocalDateTime queryTime;

    /**
     * 服务标识常量：查询通道
     */
    public static final String SERVICE_QUERY_CHANNELS = "QUERY_CHANNELS";

    /**
     * 创建成功的通道同步消息
     *
     * @param deviceId    设备ID
     * @param deviceKey   设备Key
     * @param requestId   请求ID
     * @param channelList 通道列表
     * @return 通道同步消息
     */
    public static ChannelSyncMessage success(Long deviceId, String deviceKey, String requestId,
                                              List<ChannelInfo> channelList) {
        return ChannelSyncMessage.builder()
                .deviceId(deviceId)
                .deviceKey(deviceKey)
                .serviceIdentifier(SERVICE_QUERY_CHANNELS)
                .requestId(requestId)
                .success(true)
                .channelList(channelList != null ? channelList : new ArrayList<>())
                .queryTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败的通道同步消息
     *
     * @param deviceId     设备ID
     * @param deviceKey    设备Key
     * @param requestId    请求ID
     * @param errorMessage 错误消息
     * @return 通道同步消息
     */
    public static ChannelSyncMessage failure(Long deviceId, String deviceKey, String requestId,
                                              String errorMessage) {
        return ChannelSyncMessage.builder()
                .deviceId(deviceId)
                .deviceKey(deviceKey)
                .serviceIdentifier(SERVICE_QUERY_CHANNELS)
                .requestId(requestId)
                .success(false)
                .errorMessage(errorMessage)
                .channelList(new ArrayList<>())
                .queryTime(LocalDateTime.now())
                .build();
    }

    /**
     * 检查消息是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return deviceId != null && deviceKey != null && serviceIdentifier != null;
    }

    /**
     * 获取通道数量
     *
     * @return 通道数量
     */
    public int getChannelCount() {
        return channelList != null ? channelList.size() : 0;
    }
}
