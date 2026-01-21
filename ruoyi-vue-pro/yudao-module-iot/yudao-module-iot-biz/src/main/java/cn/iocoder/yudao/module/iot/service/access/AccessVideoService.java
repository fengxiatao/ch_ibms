package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.video.AccessVideoPlayParamsRespVO;

/**
 * 门禁设备视频服务接口
 * <p>
 * 提供门禁设备（如人脸一体机）的视频预览功能。
 * 通过调用 Gateway HTTP 接口获取设备的 RTSP 连接参数。
 * <p>
 * Requirements:
 * - 2.1: 通过 Gateway 获取设备的 RTSP 流 URL
 * - 2.2: 返回包含 wsURL、rtspURL、username、password 的完整连接信息
 * - 2.3: 设备离线时返回设备不在线错误
 *
 * @author 长辉信息科技有限公司
 */
public interface AccessVideoService {

    /**
     * 获取门禁设备视频播放参数
     * <p>
     * 调用 Gateway HTTP 接口获取设备的 RTSP 连接信息，
     * 并构建前端 DHPlayer 播放器所需的参数。
     *
     * @param deviceId  设备 ID
     * @param channelNo 通道号（默认 0 表示设备内置摄像头）
     * @return 视频播放参数
     */
    AccessVideoPlayParamsRespVO getPlayParams(Long deviceId, Integer channelNo);

    /**
     * 检查设备是否支持视频预览
     * <p>
     * 目前大华人脸一体机都支持视频预览，后续可以扩展为查询设备能力集。
     *
     * @param deviceId 设备 ID
     * @return 是否支持视频预览
     */
    Boolean checkVideoSupport(Long deviceId);
}
