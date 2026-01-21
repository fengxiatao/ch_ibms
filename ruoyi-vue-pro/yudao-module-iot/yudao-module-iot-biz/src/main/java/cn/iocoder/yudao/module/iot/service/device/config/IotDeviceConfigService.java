package cn.iocoder.yudao.module.iot.service.device.config;

import cn.iocoder.yudao.module.iot.controller.admin.device.config.vo.*;

/**
 * IoT 设备配置服务接口
 *
 * 提供设备配置的读取、更新、同步功能，支持：
 * 1. 网络参数配置（IP、端口、DNS等）
 * 2. 事件参数配置（运动检测、视频丢失等）
 * 3. 视频参数配置（分辨率、码率、帧率等）
 * 4. ONVIF参数同步
 *
 * @author 长辉信息科技有限公司
 */
public interface IotDeviceConfigService {

    /**
     * 获取设备配置
     *
     * 从Redis缓存或ONVIF设备读取配置
     *
     * @param deviceId 设备ID
     * @return 设备配置信息
     */
    DeviceConfigRespVO getDeviceConfig(Long deviceId);

    /**
     * 更新设备网络配置
     *
     * @param deviceId 设备ID
     * @param reqVO 网络配置请求
     */
    void updateNetworkConfig(Long deviceId, DeviceNetworkConfigReqVO reqVO);

    /**
     * 更新设备事件配置
     *
     * @param deviceId 设备ID
     * @param reqVO 事件配置请求
     */
    void updateEventConfig(Long deviceId, DeviceEventConfigReqVO reqVO);

    /**
     * 更新设备视频配置
     *
     * @param deviceId 设备ID
     * @param reqVO 视频配置请求
     */
    void updateVideoConfig(Long deviceId, DeviceVideoConfigReqVO reqVO);

    /**
     * 从ONVIF设备同步配置
     *
     * 读取设备的实际配置并更新到系统中
     *
     * @param deviceId 设备ID
     * @return 同步后的配置信息
     */
    DeviceConfigRespVO syncConfigFromDevice(Long deviceId);

    /**
     * 应用配置到ONVIF设备
     *
     * 将系统中的配置写入到设备
     *
     * @param deviceId 设备ID
     */
    void applyConfigToDevice(Long deviceId);

    /**
     * 重置设备配置为默认值
     *
     * @param deviceId 设备ID
     */
    void resetDeviceConfig(Long deviceId);

    /**
     * 获取设备能力集
     *
     * 查询设备支持哪些配置项和功能
     *
     * @param deviceId 设备ID
     * @return 设备能力集信息
     */
    DeviceCapabilitiesRespVO getDeviceCapabilities(Long deviceId);
}












