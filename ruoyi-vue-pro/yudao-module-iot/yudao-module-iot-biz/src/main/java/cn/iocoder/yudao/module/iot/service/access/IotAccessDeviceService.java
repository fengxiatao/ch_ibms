package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDeviceConfigRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;

import java.util.List;

/**
 * 门禁设备 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAccessDeviceService {

    /**
     * 激活门禁设备
     * 连接设备并更新状态为在线
     *
     * @param deviceId 设备ID
     */
    void activateDevice(Long deviceId);

    /**
     * 停用门禁设备
     * 断开设备连接并更新状态为离线
     *
     * @param deviceId 设备ID
     */
    void deactivateDevice(Long deviceId);

    /**
     * 获取门禁设备列表
     *
     * @return 门禁设备列表
     */
    List<IotDeviceDO> getAccessDevices();

    /**
     * 获取在线门禁设备列表
     *
     * @return 在线门禁设备列表
     */
    List<IotDeviceDO> getOnlineAccessDevices();

    /**
     * 检查设备是否在线
     *
     * @param deviceId 设备ID
     * @return 是否在线
     */
    boolean isDeviceOnline(Long deviceId);

    /**
     * 获取门禁设备
     *
     * @param deviceId 设备ID
     * @return 设备信息
     */
    IotDeviceDO getAccessDevice(Long deviceId);

    /**
     * 获取设备完整配置信息
     * 包含设备基本信息、连接配置、能力集、通道信息等
     *
     * @param deviceId 设备ID
     * @return 设备完整配置信息
     */
    IotAccessDeviceConfigRespVO getDeviceConfig(Long deviceId);

}
