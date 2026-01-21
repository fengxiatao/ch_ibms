package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessChannelDetailRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;

import java.util.List;

/**
 * 门禁通道 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAccessChannelService {

    /**
     * 发现设备通道
     * 从设备获取通道信息并保存到数据库
     *
     * @param deviceId 设备ID
     * @return 发现的通道数量
     */
    int discoverChannels(Long deviceId);

    /**
     * 远程开门
     *
     * @param channelId    通道ID
     * @param operatorId   操作人ID
     * @param operatorName 操作人姓名
     */
    void openDoor(Long channelId, Long operatorId, String operatorName);

    /**
     * 远程关门
     *
     * @param channelId    通道ID
     * @param operatorId   操作人ID
     * @param operatorName 操作人姓名
     */
    void closeDoor(Long channelId, Long operatorId, String operatorName);

    /**
     * 设置常开
     *
     * @param channelId    通道ID
     * @param operatorId   操作人ID
     * @param operatorName 操作人姓名
     */
    void setAlwaysOpen(Long channelId, Long operatorId, String operatorName);

    /**
     * 设置常闭
     *
     * @param channelId    通道ID
     * @param operatorId   操作人ID
     * @param operatorName 操作人姓名
     */
    void setAlwaysClosed(Long channelId, Long operatorId, String operatorName);

    /**
     * 取消常开/常闭
     *
     * @param channelId    通道ID
     * @param operatorId   操作人ID
     * @param operatorName 操作人姓名
     */
    void cancelAlwaysState(Long channelId, Long operatorId, String operatorName);

    /**
     * 获取设备的通道列表
     *
     * @param deviceId 设备ID
     * @return 通道列表
     */
    List<IotDeviceChannelDO> getChannelsByDeviceId(Long deviceId);

    /**
     * 获取通道
     *
     * @param channelId 通道ID
     * @return 通道信息
     */
    IotDeviceChannelDO getChannel(Long channelId);

    /**
     * 获取通道详细信息
     * 包含通道配置、实时状态等完整信息
     *
     * @param channelId 通道ID
     * @return 通道详细信息
     */
    IotAccessChannelDetailRespVO getChannelDetail(Long channelId);

}
