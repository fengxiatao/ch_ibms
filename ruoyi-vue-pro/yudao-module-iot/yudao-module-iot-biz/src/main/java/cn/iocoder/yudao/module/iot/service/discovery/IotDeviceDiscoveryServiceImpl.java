package cn.iocoder.yudao.module.iot.service.discovery;

import cn.iocoder.yudao.module.iot.controller.admin.discovery.vo.DeviceDiscoveryScanReqVO;
import cn.iocoder.yudao.module.iot.core.discovery.DeviceScanRequest;
import cn.iocoder.yudao.module.iot.service.device.discovery.dto.DiscoveredDeviceDTO;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 设备发现服务实现
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class IotDeviceDiscoveryServiceImpl implements IotDeviceDiscoveryService {
    
    /**
     * 扫描状态缓存
     * scanId -> status
     */
    private final Map<String, String> scanStatusMap = new ConcurrentHashMap<>();
    
    /**
     * 扫描结果缓存
     * scanId -> List<DiscoveredDeviceDTO>
     */
    private final Map<String, List<DiscoveredDeviceDTO>> scanResultMap = new ConcurrentHashMap<>();
    
    @Resource
    private IotMessageBus messageBus;
    
    @Override
    @SuppressWarnings("deprecation") // 兼容旧版本 API
    public String startScan(DeviceDiscoveryScanReqVO reqVO) {
        // 生成扫描ID
        String scanId = UUID.randomUUID().toString();
        
        // 处理协议列表
        List<String> protocols = reqVO.getProtocols();
        if (protocols == null || protocols.isEmpty()) {
            // 兼容旧版本
            if (reqVO.getScanType() != null) {
                protocols = List.of(reqVO.getScanType());
            } else {
                protocols = List.of("onvif");  // 默认使用 ONVIF
            }
        }
        
        // 处理 IP 范围
        String ipRange = null;
        if ("manual".equals(reqVO.getNetworkMode())) {
            if (reqVO.getIpStart() != null && reqVO.getIpEnd() != null) {
                ipRange = reqVO.getIpStart() + "-" + reqVO.getIpEnd();
            }
        } else if (reqVO.getIpRange() != null) {
            // 兼容旧版本
            ipRange = reqVO.getIpRange();
        }
        
        log.info("[startScan] 开始设备扫描: scanId={}, protocols={}, timeout={}, ipRange={}, ports={}, concurrency={}", 
                scanId, protocols, reqVO.getTimeout(), ipRange, reqVO.getPorts(), reqVO.getConcurrency());
        
        // 设置初始状态
        scanStatusMap.put(scanId, "scanning");
        
        // 构建扫描请求
        DeviceScanRequest request = DeviceScanRequest.builder()
            .scanId(scanId)
            .requestId(scanId)
            .tenantId(cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.getTenantId())
            .scanType(protocols.get(0))  // 主要协议
            .timeout(reqVO.getTimeout() != null ? reqVO.getTimeout() : 5)
            .ipRange(ipRange)
            .build();

        
        // TODO: 将完整参数传递给 Gateway（需要扩展 DeviceScanRequest）
        // request.setProtocols(protocols);
        // request.setPorts(reqVO.getPorts());
        // request.setConcurrency(reqVO.getConcurrency());
        // request.setSkipAdded(reqVO.getSkipAdded());
        
        // 发布扫描请求到Gateway (使用统一通道)
        messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, request);
        
        log.info("[startScan] 扫描请求已发布: scanId={}", scanId);
        
        return scanId;
    }
    
    @Override
    public List<DiscoveredDeviceDTO> getScanResult(String scanId) {
        String status = scanStatusMap.get(scanId);
        
        if (status == null) {
            log.warn("[getScanResult] 扫描请求不存在: scanId={}", scanId);
            return null;
        }
        
        if ("scanning".equals(status)) {
            log.debug("[getScanResult] 扫描进行中: scanId={}", scanId);
            return null;
        }
        
        List<DiscoveredDeviceDTO> devices = scanResultMap.get(scanId);
        log.info("[getScanResult] 获取扫描结果: scanId={}, 设备数={}", 
                scanId, devices != null ? devices.size() : 0);
        
        return devices;
    }
    
    @Override
    public String getScanStatus(String scanId) {
        String status = scanStatusMap.getOrDefault(scanId, "not_found");
        log.debug("[getScanStatus] 查询扫描状态: scanId={}, status={}", scanId, status);
        return status;
    }
    
    /**
     * 处理扫描结果（由DeviceScanResultConsumer调用）
     * 
     * @param scanId 扫描ID
     * @param devices 发现的设备列表
     * @param success 是否成功
     */
    public void handleScanResult(String scanId, List<DiscoveredDeviceDTO> devices, Boolean success) {
        if (success) {
            scanStatusMap.put(scanId, "completed");
            scanResultMap.put(scanId, devices);
            log.info("[handleScanResult] 扫描完成: scanId={}, 发现{}个设备", scanId, devices.size());
        } else {
            scanStatusMap.put(scanId, "failed");
            log.error("[handleScanResult] 扫描失败: scanId={}", scanId);
        }
    }
}

















