package cn.iocoder.yudao.module.iot.api.device;



import cn.iocoder.yudao.framework.common.pojo.CommonResult;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;

import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;

import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceGatewaySupportService;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Primary;

import org.springframework.stereotype.Service;

import org.springframework.validation.annotation.Validated;



import java.util.ArrayList;

import java.util.List;



import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;



/**

 * IoT 设备通用 API 的进程内实现：台账与运行态以 {@code ibms_*} 为准。

 */

@Service

@Validated

@Slf4j

@Primary

public class IotDeviceCommonApiLocalImpl implements IotDeviceCommonApi {



    @Resource

    private IbmsDeviceGatewaySupportService ibmsDeviceGatewaySupportService;



    @Override

    @TenantIgnore

    public CommonResult<Boolean> authDevice(IotDeviceAuthReqDTO authReqDTO) {

        return success(ibmsDeviceGatewaySupportService.authDevice(authReqDTO));

    }



    @Override

    @TenantIgnore

    public CommonResult<IotDeviceRespDTO> getDevice(IotDeviceGetReqDTO getReqDTO) {

        IotDeviceRespDTO dto = ibmsDeviceGatewaySupportService.getGatewayDevice(getReqDTO);

        if (dto == null) {

            log.debug("[getDevice] 未找到设备: {}", getReqDTO);

        }

        return success(dto);

    }



    @Override

    @TenantIgnore

    public CommonResult<List<IotDeviceRespDTO>> getOnlineDevices() {

        return success(ibmsDeviceGatewaySupportService.listOnlineDevices());

    }



    @Override

    @TenantIgnore

    public CommonResult<List<IotDeviceRespDTO>> getAccessDevices() {

        return success(ibmsDeviceGatewaySupportService.listAccessDevices());

    }



    @Override

    @TenantIgnore

    public CommonResult<List<IotDeviceRespDTO>> getAllDevices() {

        return success(ibmsDeviceGatewaySupportService.listAllDevices());

    }



    @Override

    @TenantIgnore

    public CommonResult<List<IotDeviceRespDTO>> getDevicesByTenantId(Long tenantId) {

        if (tenantId == null) {

            log.warn("[getDevicesByTenantId] tenantId 不能为空");

            return success(new ArrayList<>());

        }

        List<IotDeviceRespDTO> result = ibmsDeviceGatewaySupportService.listDevicesForTenant(tenantId);

        log.info("[getDevicesByTenantId] 获取租户 {} 的设备列表，共 {} 个", tenantId, result.size());

        return success(result);

    }



    @Override

    @TenantIgnore

    public CommonResult<Integer> batchUpdateDeviceState(List<Long> deviceIds, Integer state, String reason) {

        if (deviceIds == null || deviceIds.isEmpty()) {

            log.warn("[batchUpdateDeviceState] deviceIds 不能为空");

            return success(0);

        }

        int n = ibmsDeviceGatewaySupportService.batchUpdateGatewayRuntimeState(deviceIds, state, reason);

        log.info("[batchUpdateDeviceState] 批量更新设备网关运行态完成: total={}, success={}, state={}, reason={}",

                deviceIds.size(), n, state, reason);

        return success(n);

    }

}

