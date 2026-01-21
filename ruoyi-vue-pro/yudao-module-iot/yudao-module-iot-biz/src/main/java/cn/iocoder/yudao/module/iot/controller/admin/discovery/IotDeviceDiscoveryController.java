package cn.iocoder.yudao.module.iot.controller.admin.discovery;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.discovery.vo.DeviceDiscoveryRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.discovery.vo.DeviceDiscoveryScanReqVO;
import cn.iocoder.yudao.module.iot.service.device.discovery.dto.DiscoveredDeviceDTO;
import cn.iocoder.yudao.module.iot.service.device.discovery.DiscoveredDeviceService;
import cn.iocoder.yudao.module.iot.service.discovery.IotDeviceDiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 设备发现控制器
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - IoT 设备发现")
@RestController
@RequestMapping("/iot/device/discovery")
@Validated
public class IotDeviceDiscoveryController {
    
    @Resource
    private IotDeviceDiscoveryService deviceDiscoveryService;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private DiscoveredDeviceService discoveredDeviceService;
    
    @PostMapping("/scan")
    @Operation(summary = "启动设备扫描")
    @SuppressWarnings("deprecation") // 兼容旧版本 API
    public CommonResult<Map<String, String>> startScan(@Valid @RequestBody DeviceDiscoveryScanReqVO reqVO) {
        // 兼容旧版本：如果使用了 scanType，转换为 protocols
        List<String> protocols = reqVO.getProtocols();
        if ((protocols == null || protocols.isEmpty()) && reqVO.getScanType() != null) {
            protocols = List.of(reqVO.getScanType());
        }
        
        // 默认使用 ONVIF
        if (protocols == null || protocols.isEmpty()) {
            protocols = List.of("onvif");
        }
        
        // 调用服务层（传递完整参数）
        String scanId = deviceDiscoveryService.startScan(reqVO);
        
        Map<String, String> result = new HashMap<>();
        result.put("scanId", scanId);
        result.put("status", "scanning");
        result.put("protocols", String.join(",", protocols));
        
        return success(result);
    }
    
    @GetMapping("/result/{scanId}")
    @Operation(summary = "获取扫描结果")
    @Parameter(name = "scanId", description = "扫描请求ID", required = true)
    public CommonResult<DeviceDiscoveryRespVO> getScanResult(@PathVariable("scanId") String scanId) {
        String status = deviceDiscoveryService.getScanStatus(scanId);
        List<DiscoveredDeviceDTO> devices = null;
        
        // 只有完成或失败状态才返回结果
        if ("completed".equals(status) || "failed".equals(status)) {
            devices = deviceDiscoveryService.getScanResult(scanId);
        } else if (discoveredDeviceService != null) {
            // ⚠️ 临时方案：如果状态不是 completed，尝试从数据库获取最近发现的设备
            // 这样即使没有收到 DeviceScanResult 消息，也能显示设备
            devices = discoveredDeviceService.getRecentDiscoveredDevices(1); // 最近1小时
        }
        
        DeviceDiscoveryRespVO respVO = new DeviceDiscoveryRespVO();
        respVO.setScanId(scanId);
        respVO.setStatus(status);
        respVO.setDevices(devices);
        
        return success(respVO);
    }
    
    @GetMapping("/status/{scanId}")
    @Operation(summary = "获取扫描状态")
    @Parameter(name = "scanId", description = "扫描请求ID", required = true)
    public CommonResult<Map<String, String>> getScanStatus(@PathVariable("scanId") String scanId) {
        String status = deviceDiscoveryService.getScanStatus(scanId);
        
        Map<String, String> result = new HashMap<>();
        result.put("scanId", scanId);
        result.put("status", status);
        
        return success(result);
    }
    
    @PostMapping("/ignore/{id}")
    @Operation(summary = "忽略发现的设备")
    @Parameter(name = "id", description = "设备记录ID", required = true)
    public CommonResult<Boolean> ignoreDevice(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Integer ignoreDays,
            @RequestParam(required = false) String reason) {
        if (discoveredDeviceService == null) {
            return success(false);
        }
        discoveredDeviceService.ignoreDevice(id, ignoreDays, reason);
        return success(true);
    }
    
    @PostMapping("/unignore/{id}")
    @Operation(summary = "取消忽略设备")
    @Parameter(name = "id", description = "设备记录ID", required = true)
    public CommonResult<Boolean> unignoreDevice(@PathVariable("id") Long id) {
        if (discoveredDeviceService == null) {
            return success(false);
        }
        discoveredDeviceService.unignoreDevice(id);
        return success(true);
    }
    
    @PostMapping("/pending/{id}")
    @Operation(summary = "标记为待处理")
    @Parameter(name = "id", description = "设备记录ID", required = true)
    public CommonResult<Boolean> markAsPending(@PathVariable("id") Long id) {
        if (discoveredDeviceService == null) {
            return success(false);
        }
        discoveredDeviceService.markAsPending(id);
        return success(true);
    }
    
    @GetMapping("/recent")
    @Operation(summary = "获取最近发现的设备")
    @Parameter(name = "hours", description = "时间范围（小时），默认24小时", required = false)
    public CommonResult<List<DiscoveredDeviceDTO>> getRecentDiscoveredDevices(
            @RequestParam(defaultValue = "24") Integer hours) {
        if (discoveredDeviceService == null) {
            return success(java.util.Collections.emptyList());
        }
        List<DiscoveredDeviceDTO> devices = discoveredDeviceService.getRecentDiscoveredDevices(hours);
        return success(devices);
    }
    
    @GetMapping("/unadded")
    @Operation(summary = "获取未添加的发现设备")
    public CommonResult<List<DiscoveredDeviceDTO>> getUnaddedDevices() {
        if (discoveredDeviceService == null) {
            return success(java.util.Collections.emptyList());
        }
        List<DiscoveredDeviceDTO> devices = discoveredDeviceService.getUnaddedDevices();
        return success(devices);
    }
}















