package cn.iocoder.yudao.module.iot.service.device.discovery;

import cn.iocoder.yudao.module.iot.service.device.discovery.dto.DiscoveredDeviceDTO;

import java.util.List;

/**
 * 发现设备服务接口
 * 
 * @author 长辉信息科技有限公司
 */
public interface DiscoveredDeviceService {
    
    /**
     * 保存发现的设备
     * 
     * @param device 发现的设备
     * @param added 是否已添加到平台
     * @return 是否成功保存（false表示24小时内已存在，未保存）
     */
    boolean saveDiscoveredDevice(DiscoveredDeviceDTO device, boolean added);
    
    /**
     * 获取最近发现的设备
     * 
     * @param hours 最近多少小时
     * @return 发现的设备列表
     */
    List<DiscoveredDeviceDTO> getRecentDiscoveredDevices(Integer hours);
    
    /**
     * 获取未添加的设备
     * 
     * @return 未添加的设备列表
     */
    List<DiscoveredDeviceDTO> getUnaddedDevices();
    
    /**
     * 忽略发现的设备
     * 
     * @param id 设备记录ID
     * @param ignoreDays 忽略天数（NULL表示永久忽略）
     * @param reason 忽略原因
     */
    void ignoreDevice(Long id, Integer ignoreDays, String reason);
    
    /**
     * 取消忽略设备
     * 
     * @param id 设备记录ID
     */
    void unignoreDevice(Long id);
    
    /**
     * 标记设备为待处理
     * 
     * @param id 设备记录ID
     */
    void markAsPending(Long id);
    
    /**
     * 标记设备为已激活
     * 
     * @param ip 设备IP地址
     * @param deviceId 激活后的设备ID
     */
    void markAsActivated(String ip, Long deviceId);
    
    /**
     * 获取未激活的设备列表（排除已激活的设备）
     * 
     * @return 未激活的设备列表
     */
    List<DiscoveredDeviceDTO> getUnactivatedDevices();
}

