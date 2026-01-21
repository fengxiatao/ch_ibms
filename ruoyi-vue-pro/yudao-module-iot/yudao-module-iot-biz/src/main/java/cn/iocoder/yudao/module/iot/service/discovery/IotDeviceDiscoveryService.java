package cn.iocoder.yudao.module.iot.service.discovery;

import cn.iocoder.yudao.module.iot.controller.admin.discovery.vo.DeviceDiscoveryScanReqVO;
import cn.iocoder.yudao.module.iot.service.device.discovery.dto.DiscoveredDeviceDTO;

import java.util.List;

/**
 * IoT 设备发现服务接口
 *
 * @author 长辉信息科技有限公司
 */
public interface IotDeviceDiscoveryService {
    
    /**
     * 启动设备扫描（新版本，支持完整参数）
     * 
     * @param reqVO 扫描请求参数
     * @return 扫描请求ID
     */
    String startScan(DeviceDiscoveryScanReqVO reqVO);
    
    /**
     * 启动设备扫描（旧版本，兼容性方法）
     * 
     * @param scanType 扫描类型（onvif, upnp等）
     * @param timeout 超时时间（秒）
     * @return 扫描请求ID
     * @deprecated 请使用 {@link #startScan(DeviceDiscoveryScanReqVO)}
     */
    @Deprecated
    default String startScan(String scanType, Integer timeout) {
        DeviceDiscoveryScanReqVO reqVO = new DeviceDiscoveryScanReqVO();
        reqVO.setScanType(scanType);
        reqVO.setTimeout(timeout);
        return startScan(reqVO);
    }
    
    /**
     * 获取扫描结果
     * 
     * @param scanId 扫描请求ID
     * @return 发现的设备列表，如果扫描未完成返回null
     */
    List<DiscoveredDeviceDTO> getScanResult(String scanId);
    
    /**
     * 获取扫描状态
     * 
     * @param scanId 扫描请求ID
     * @return 扫描状态（scanning, completed, failed, not_found）
     */
    String getScanStatus(String scanId);
}

















