package cn.iocoder.yudao.module.iot.api.device;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.enums.RpcConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 设备 API 实现类
 *
 * @author haohao
 */
@RestController
@Validated
@Slf4j
@Primary // 保证优先匹配，因为 yudao-iot-gateway 也有 IotDeviceCommonApi 的实现，并且也可能会被 biz 引入
public class IoTDeviceApiImpl implements IotDeviceCommonApi {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotProductService productService;

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/auth")
    @PermitAll
    public CommonResult<Boolean> authDevice(@RequestBody IotDeviceAuthReqDTO authReqDTO) {
        return success(deviceService.authDevice(authReqDTO));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/get") // 特殊：方便调用，暂时使用 POST，实际更推荐 GET
    @PermitAll
    @TenantIgnore // Gateway 系统级调用，需要跨租户查询设备
    public CommonResult<IotDeviceRespDTO> getDevice(@RequestBody IotDeviceGetReqDTO getReqDTO) {
        IotDeviceDO device = null;
        
        // 优先级：id > deviceKey > productKey + deviceName
        // 注意：德通协议(长辉)设备的 deviceKey 必须设置为测站编码(stationCode)
        if (getReqDTO.getId() != null) {
            device = deviceService.getDeviceFromCache(getReqDTO.getId());
        } else if (getReqDTO.getDeviceKey() != null) {
            device = deviceService.getDeviceFromCacheByDeviceKey(getReqDTO.getDeviceKey());
        } else if (getReqDTO.getProductKey() != null && getReqDTO.getDeviceName() != null) {
            device = deviceService.getDeviceFromCache(getReqDTO.getProductKey(), getReqDTO.getDeviceName());
        }
        
        if (device == null) {
            log.debug("[getDevice] 未找到设备: {}", getReqDTO);
            return success(null);
        }
        
        log.debug("[getDevice] 找到设备: id={}, deviceName={}", device.getId(), device.getDeviceName());
        
        // 转换为 DTO
        IotDeviceRespDTO deviceDTO = BeanUtils.toBean(device, IotDeviceRespDTO.class);
        
        // 手动处理 config 字段：将 DeviceConfig 对象序列化为 JSON 字符串
        // 因为 BeanUtils.toBean 会调用 toString() 而不是 JSON 序列化
        DeviceConfig config = device.getConfig();
        if (config != null) {
            try {
                deviceDTO.setConfig(JSONUtil.toJsonStr(config.toMap()));
            } catch (Exception e) {
                // 如果序列化失败，尝试直接序列化对象
                deviceDTO.setConfig(JSONUtil.toJsonStr(config));
            }
        }
        
        // 设置产品编解码类型
        if (deviceDTO.getProductId() != null) {
            IotProductDO product = productService.getProductFromCache(deviceDTO.getProductId());
            if (product != null) {
                deviceDTO.setCodecType(product.getCodecType());
            }
        }
        
        return success(deviceDTO);
    }

    @Override
    @GetMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/online-devices")
    @PermitAll
    @TenantIgnore // Gateway系统级调用,需要查询所有租户的在线设备
    public CommonResult<List<IotDeviceRespDTO>> getOnlineDevices() {
        // 查询所有在线设备（state=1表示在线）
        List<IotDeviceDO> onlineDevices = deviceService.getDeviceListByState(1);
        
        // 转换为 DTO 列表
        List<IotDeviceRespDTO> deviceDTOs = onlineDevices.stream()
                .map(device -> {
                    IotDeviceRespDTO deviceDTO = BeanUtils.toBean(device, IotDeviceRespDTO.class);
                    
                    // 手动处理 config 字段：将 DeviceConfig 对象序列化为 JSON 字符串
                    // 因为 BeanUtils.toBean 会调用 toString() 而不是 JSON 序列化
                    DeviceConfig config = device.getConfig();
                    if (config != null) {
                        try {
                            deviceDTO.setConfig(JSONUtil.toJsonStr(config.toMap()));
                        } catch (Exception e) {
                            // 如果序列化失败，尝试直接序列化对象
                            deviceDTO.setConfig(JSONUtil.toJsonStr(config));
                        }
                    }
                    
                    // 设置产品编解码类型
                    if (deviceDTO.getProductId() != null) {
                        IotProductDO product = productService.getProductFromCache(deviceDTO.getProductId());
                        if (product != null) {
                            deviceDTO.setCodecType(product.getCodecType());
                        }
                    }
                    
                    return deviceDTO;
                })
                .collect(Collectors.toList());
        
        return success(deviceDTOs);
    }

    @Override
    @GetMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/access-devices")
    @PermitAll
    @TenantIgnore // Gateway系统级调用,需要查询所有租户的门禁设备
    public CommonResult<List<IotDeviceRespDTO>> getAccessDevices() {
        // 查询所有门禁设备（subsystemCode='access'），不限状态
        List<IotDeviceDO> accessDevices = deviceService.getDeviceListBySubsystemCode("access");
        
        // 转换为 DTO 列表
        List<IotDeviceRespDTO> deviceDTOs = accessDevices.stream()
                .map(device -> {
                    IotDeviceRespDTO deviceDTO = BeanUtils.toBean(device, IotDeviceRespDTO.class);
                    
                    // 手动处理 config 字段：将 DeviceConfig 对象序列化为 JSON 字符串
                    DeviceConfig config = device.getConfig();
                    if (config != null) {
                        try {
                            deviceDTO.setConfig(JSONUtil.toJsonStr(config.toMap()));
                        } catch (Exception e) {
                            // 如果序列化失败，尝试直接序列化对象
                            deviceDTO.setConfig(JSONUtil.toJsonStr(config));
                        }
                    }
                    
                    // 设置产品编解码类型
                    if (deviceDTO.getProductId() != null) {
                        IotProductDO product = productService.getProductFromCache(deviceDTO.getProductId());
                        if (product != null) {
                            deviceDTO.setCodecType(product.getCodecType());
                        }
                    }
                    
                    return deviceDTO;
                })
                .collect(Collectors.toList());
        
        return success(deviceDTOs);
    }

    @Override
    @GetMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/all-devices")
    @PermitAll
    @TenantIgnore // Gateway系统级调用,需要查询所有租户的设备
    public CommonResult<List<IotDeviceRespDTO>> getAllDevices() {
        // 查询所有设备，不限状态和类型
        List<IotDeviceDO> allDevices = deviceService.getDeviceList();
        
        // 转换为 DTO 列表
        List<IotDeviceRespDTO> deviceDTOs = allDevices.stream()
                .map(device -> {
                    IotDeviceRespDTO deviceDTO = BeanUtils.toBean(device, IotDeviceRespDTO.class);
                    
                    // 手动处理 config 字段：将 DeviceConfig 对象序列化为 JSON 字符串
                    DeviceConfig config = device.getConfig();
                    if (config != null) {
                        try {
                            deviceDTO.setConfig(JSONUtil.toJsonStr(config.toMap()));
                        } catch (Exception e) {
                            // 如果序列化失败，尝试直接序列化对象
                            deviceDTO.setConfig(JSONUtil.toJsonStr(config));
                        }
                    }
                    
                    // 设置产品编解码类型
                    if (deviceDTO.getProductId() != null) {
                        IotProductDO product = productService.getProductFromCache(deviceDTO.getProductId());
                        if (product != null) {
                            deviceDTO.setCodecType(product.getCodecType());
                        }
                    }
                    
                    return deviceDTO;
                })
                .collect(Collectors.toList());
        
        return success(deviceDTOs);
    }

    @Override
    @GetMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/devices-by-tenant")
    @PermitAll
    @TenantIgnore // 仍需忽略自动租户过滤，因为参数传入的 tenantId 会手动过滤
    public CommonResult<List<IotDeviceRespDTO>> getDevicesByTenantId(@RequestParam("tenantId") Long tenantId) {
        if (tenantId == null) {
            log.warn("[getDevicesByTenantId] tenantId 不能为空");
            return success(new ArrayList<>());
        }
        
        // 使用 TenantUtils 在指定租户上下文中执行查询
        List<IotDeviceRespDTO> result = new ArrayList<>();
        TenantUtils.execute(tenantId, () -> {
            // 在租户上下文中执行查询，会自动应用租户过滤 (WHERE tenant_id = ?)
            // 注意：必须使用 getDeviceListWithTenantFilter()，因为 getDeviceList() 有 @TenantIgnore 注解
            List<IotDeviceDO> devices = deviceService.getDeviceListWithTenantFilter();
            
            for (IotDeviceDO device : devices) {
                IotDeviceRespDTO deviceDTO = BeanUtils.toBean(device, IotDeviceRespDTO.class);
                
                // 手动处理 config 字段
                DeviceConfig config = device.getConfig();
                if (config != null) {
                    try {
                        deviceDTO.setConfig(JSONUtil.toJsonStr(config.toMap()));
                    } catch (Exception e) {
                        deviceDTO.setConfig(JSONUtil.toJsonStr(config));
                    }
                }
                
                // 设置产品编解码类型
                if (deviceDTO.getProductId() != null) {
                    IotProductDO product = productService.getProductFromCache(deviceDTO.getProductId());
                    if (product != null) {
                        deviceDTO.setCodecType(product.getCodecType());
                    }
                }
                
                result.add(deviceDTO);
            }
        });
        
        log.info("[getDevicesByTenantId] 获取租户 {} 的设备列表，共 {} 个", tenantId, result.size());
        return success(result);
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/batch-update-state")
    @PermitAll
    @TenantIgnore // Gateway系统级调用，需要跨租户更新设备状态
    public CommonResult<Integer> batchUpdateDeviceState(
            @RequestParam("deviceIds") List<Long> deviceIds,
            @RequestParam("state") Integer state,
            @RequestParam(value = "reason", required = false) String reason) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            log.warn("[batchUpdateDeviceState] deviceIds 不能为空");
            return success(0);
        }
        
        int successCount = 0;
        for (Long deviceId : deviceIds) {
            try {
                deviceService.updateDeviceState(deviceId, state);
                successCount++;
            } catch (Exception e) {
                log.warn("[batchUpdateDeviceState] 更新设备状态失败: deviceId={}, state={}, error={}", 
                        deviceId, state, e.getMessage());
            }
        }
        
        log.info("[batchUpdateDeviceState] 批量更新设备状态完成: total={}, success={}, state={}, reason={}", 
                deviceIds.size(), successCount, state, reason);
        return success(successCount);
    }

}