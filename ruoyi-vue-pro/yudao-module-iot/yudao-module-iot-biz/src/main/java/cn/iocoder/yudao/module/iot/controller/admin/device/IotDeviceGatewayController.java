package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceRespVO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceGatewaySupportService;
import cn.iocoder.yudao.module.iot.util.VendorExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Gateway 初始化用接口（保留旧路径，仅用于网关拉取在线设备列表）。
 *
 * <p>注意：此接口是网关启动拉取在线设备配置的入口，不属于管理后台台账 CRUD；
 * 台账 CRUD 已统一收口到 {@code /iot/ibms/device}。</p>
 */
@Tag(name = "管理后台 - IoT 设备（Gateway 初始化）")
@RestController
@RequestMapping("/iot/device")
@Validated
public class IotDeviceGatewayController {

    @Resource
    private IbmsDeviceGatewaySupportService ibmsDeviceGatewaySupportService;

    /**
     * Gateway 专用接口：获取所有在线设备
     *
     * <p>此接口供 Gateway 启动时初始化设备连接使用，无需认证。</p>
     */
    @GetMapping("/list-all-online")
    @Operation(summary = "获取所有在线设备（Gateway专用）", description = "供Gateway初始化使用，返回设备基本信息和连接配置")
    @PermitAll
    @TenantIgnore
    public CommonResult<List<IotDeviceRespVO>> getAllOnlineDevices() {
        List<IotDeviceRespVO> voList = new ArrayList<>();
        for (IotDeviceRespDTO dto : ibmsDeviceGatewaySupportService.listOnlineDevices()) {
            voList.add(ibmsOnlineGatewayDtoToVo(dto));
        }
        return success(voList);
    }

    private static IotDeviceRespVO ibmsOnlineGatewayDtoToVo(IotDeviceRespDTO dto) {
        IotDeviceRespVO vo = new IotDeviceRespVO();
        vo.setId(dto.getId());
        vo.setDeviceName(dto.getDeviceName());
        vo.setProductKey(dto.getProductKey());
        vo.setProductId(dto.getProductId());
        vo.setState(IotDeviceStateEnum.ONLINE.getState());
        vo.setIpAddress(dto.getAddress());
        vo.setConfig(dto.getConfig());
        vo.setVendor(VendorExtractor.extractVendor(dto.getConfig()));
        return vo;
    }
}

