package cn.iocoder.yudao.module.iot.service.video;

import cn.iocoder.yudao.module.iot.controller.admin.security.vo.PlayUrlRespVO;

/**
 * ZLMediaKit 流媒体服务接口
 * 
 * <p>提供摄像头实时预览和录像回放的流媒体服务</p>
 *
 * @author IBMS
 */
public interface ZlmStreamService {

    /**
     * 获取摄像头实时播放地址（按需拉流）- 使用主码流
     * 
     * <p>如果流不存在，会自动从摄像头 RTSP 拉流</p>
     *
     * @param channelId 通道ID
     * @return 多协议播放地址
     */
    default PlayUrlRespVO getLivePlayUrl(Long channelId) {
        return getLivePlayUrl(channelId, 0);
    }

    /**
     * 获取摄像头实时播放地址（按需拉流）
     * 
     * <p>如果流不存在，会自动从摄像头 RTSP 拉流</p>
     *
     * @param channelId 通道ID
     * @param subtype   码流类型: 0=主码流/高清, 1=子码流/标清
     * @return 多协议播放地址
     */
    PlayUrlRespVO getLivePlayUrl(Long channelId, Integer subtype);

    /**
     * 停止指定通道的流
     *
     * @param channelId 通道ID
     * @return 是否成功
     */
    boolean stopStream(Long channelId);

    /**
     * 检查流是否在线
     *
     * @param channelId 通道ID
     * @return 是否在线
     */
    boolean isStreamOnline(Long channelId);

    /**
     * 处理流未找到事件（Hook 回调）
     *
     * @param app    应用名
     * @param stream 流名
     */
    void handleStreamNotFound(String app, String stream);

    /**
     * 处理无人观看事件（Hook 回调）
     *
     * @param app    应用名
     * @param stream 流名
     */
    void handleStreamNoneReader(String app, String stream);

    /**
     * 获取录像回放播放地址
     * 
     * <p>从 NVR 拉取指定时间段的录像流</p>
     *
     * @param channelId  通道ID
     * @param startTime  开始时间（ISO格式或时间戳）
     * @param endTime    结束时间（ISO格式或时间戳）
     * @return 多协议播放地址
     */
    default PlayUrlRespVO getPlaybackUrl(Long channelId, String startTime, String endTime) {
        return getPlaybackUrl(channelId, startTime, endTime, null);
    }

    /**
     * 获取录像回放播放地址（支持窗口复用）
     * 
     * <p>从 NVR 拉取指定时间段的录像流</p>
     *
     * @param channelId  通道ID
     * @param startTime  开始时间（ISO格式或时间戳）
     * @param endTime    结束时间（ISO格式或时间戳）
     * @param playId     播放窗口标识（同一窗口使用固定标识，便于复用流）
     * @return 多协议播放地址
     */
    PlayUrlRespVO getPlaybackUrl(Long channelId, String startTime, String endTime, String playId);

    /**
     * 清除所有流代理（清空缓存的流）
     * 
     * <p>用于修复因RTSP URL错误导致的缓存问题</p>
     *
     * @return 清除的流数量
     */
    int clearAllStreams();

    /**
     * 获取通道封面快照（JPEG）。
     *
     * <p>用于列表页 / 播放器 idle/loading 状态下的占位封面，
     * 让用户在真流出帧前先看到画面，显著降低首屏体感延迟。</p>
     *
     * <p>实现策略：</p>
     * <ul>
     *     <li>复用 {@code getLivePlayUrl} 的 RTSP 拼接逻辑</li>
     *     <li>调用 ZLM {@code /index/api/getSnap}，由 ZLM 在磁盘缓存 jpeg（默认 5 分钟）</li>
     *     <li>若该流当前正在被代理（已 addStreamProxy），ZLM 会从内存帧抓取，毫秒级返回</li>
     *     <li>若流不存在，ZLM 会临时拉一次源（1-3 秒），首次调用慢，后续走缓存</li>
     * </ul>
     *
     * @param channelId 通道 ID
     * @param subtype   码流类型: 0=主码流, 1=子码流（封面建议用子码流，体积小）
     * @return JPEG 字节数组；若失败返回 null
     */
    byte[] getChannelSnapshot(Long channelId, Integer subtype);
}
