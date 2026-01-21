package cn.iocoder.yudao.module.iot.controller.admin.device.activation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.activation.vo.DeviceActivationReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.activation.vo.DeviceActivationRespVO;
import cn.iocoder.yudao.module.iot.service.device.discovery.dto.DiscoveredDeviceDTO;
import cn.iocoder.yudao.module.iot.service.device.activation.IotDeviceActivationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 设备激活控制器
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - IoT 设备激活")
@RestController
@RequestMapping("/iot/device/activation")
@Validated
public class IotDeviceActivationController {
    
    @Resource
    private IotDeviceActivationService deviceActivationService;
    
    @PostMapping("/activate")
    @Operation(summary = "激活设备")
    public CommonResult<Map<String, String>> activateDevice(@Valid @RequestBody DeviceActivationReqVO reqVO) {
        // 构建发现设备对象
        DiscoveredDeviceDTO discoveredDevice = DiscoveredDeviceDTO.builder()
            .ipAddress(reqVO.getIpAddress())
            .vendor(reqVO.getVendor())
            .model(reqVO.getModel())
            .serialNumber(reqVO.getSerialNumber())
            .firmwareVersion(reqVO.getFirmwareVersion())
            .deviceType(reqVO.getDeviceType())
            .httpPort(reqVO.getHttpPort() != null ? reqVO.getHttpPort() : 80)
            .rtspPort(reqVO.getRtspPort() != null ? reqVO.getRtspPort() : 554)
            .onvifPort(reqVO.getOnvifPort() != null ? reqVO.getOnvifPort() : 80)  // 大部分新设备使用 80 端口
            .onvifSupported(true)
            .build();
        
        String activationId = deviceActivationService.activateDevice(
            discoveredDevice,
            reqVO.getProductId(),
            reqVO.getUsername(),
            reqVO.getPassword()
        );
        
        Map<String, String> result = new HashMap<>();
        result.put("activationId", activationId);
        result.put("status", "activating");
        
        return success(result);
    }
    
    @GetMapping("/result/{activationId}")
    @Operation(summary = "获取激活结果（包含错误信息）")
    @Parameter(name = "activationId", description = "激活请求ID", required = true)
    public CommonResult<DeviceActivationRespVO> getActivationResult(@PathVariable("activationId") String activationId) {
        Map<String, Object> statusInfo = deviceActivationService.getActivationStatusDetail(activationId);
        
        if (statusInfo == null) {
            return success(null);
        }
        
        DeviceActivationRespVO respVO = new DeviceActivationRespVO();
        respVO.setActivationId(activationId);
        respVO.setStatus((String) statusInfo.get("status"));
        respVO.setDeviceId((Long) statusInfo.get("deviceId"));
        respVO.setErrorMessage((String) statusInfo.get("errorMessage"));
        
        return success(respVO);
    }
    
    @GetMapping("/status/{activationId}")
    @Operation(summary = "获取激活状态")
    @Parameter(name = "activationId", description = "激活请求ID", required = true)
    public CommonResult<Map<String, String>> getActivationStatus(@PathVariable("activationId") String activationId) {
        String status = deviceActivationService.getActivationStatus(activationId);
        
        Map<String, String> result = new HashMap<>();
        result.put("activationId", activationId);
        result.put("status", status);
        
        return success(result);
    }
    
    @PostMapping("/disconnect/{deviceId}")
    @Operation(summary = "断开设备连接")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    public CommonResult<Boolean> disconnectDevice(@PathVariable("deviceId") Long deviceId) {
        deviceActivationService.disconnectDevice(deviceId);
        return success(true);
    }
}

















