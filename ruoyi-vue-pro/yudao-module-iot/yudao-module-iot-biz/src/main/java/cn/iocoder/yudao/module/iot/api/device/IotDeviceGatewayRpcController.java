package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.enums.RpcConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 专供旧版网关通过 HTTP 拉取设备的 RPC 入口。
 * <p>
 * 新建项目默认关闭（{@code yudao.iot.gateway-rpc.enabled=false}），网关仅通过 RocketMQ 协作。
 * </p>
 */
@RestController
@Validated
@Slf4j
@ConditionalOnProperty(prefix = "yudao.iot.gateway-rpc", name = "enabled", havingValue = "true")
public class IotDeviceGatewayRpcController {

    @Resource
    private IotDeviceCommonApi iotDeviceCommonApi;

    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/auth")
    @PermitAll
    public CommonResult<Boolean> authDevice(@RequestBody IotDeviceAuthReqDTO authReqDTO) {
        return iotDeviceCommonApi.authDevice(authReqDTO);
    }

    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/get")
    @PermitAll
    public CommonResult<IotDeviceRespDTO> getDevice(@RequestBody IotDeviceGetReqDTO getReqDTO) {
        return iotDeviceCommonApi.getDevice(getReqDTO);
    }

    @GetMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/online-devices")
    @PermitAll
    public CommonResult<List<IotDeviceRespDTO>> getOnlineDevices() {
        return iotDeviceCommonApi.getOnlineDevices();
    }

    @GetMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/access-devices")
    @PermitAll
    public CommonResult<List<IotDeviceRespDTO>> getAccessDevices() {
        return iotDeviceCommonApi.getAccessDevices();
    }

    @GetMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/all-devices")
    @PermitAll
    public CommonResult<List<IotDeviceRespDTO>> getAllDevices() {
        return iotDeviceCommonApi.getAllDevices();
    }

    @GetMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/devices-by-tenant")
    @PermitAll
    public CommonResult<List<IotDeviceRespDTO>> getDevicesByTenantId(@RequestParam("tenantId") Long tenantId) {
        return iotDeviceCommonApi.getDevicesByTenantId(tenantId);
    }

    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/batch-update-state")
    @PermitAll
    public CommonResult<Integer> batchUpdateDeviceState(
            @RequestParam("deviceIds") List<Long> deviceIds,
            @RequestParam("state") Integer state,
            @RequestParam(value = "reason", required = false) String reason) {
        return iotDeviceCommonApi.batchUpdateDeviceState(deviceIds, state, reason);
    }
}
