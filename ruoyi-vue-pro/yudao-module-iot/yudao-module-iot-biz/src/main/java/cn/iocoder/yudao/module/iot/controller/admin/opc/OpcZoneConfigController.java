package cn.iocoder.yudao.module.iot.controller.admin.opc;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.opc.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opc.OpcZoneConfigDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.opc.OpcZoneConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * OPC 防区配置 Controller
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - OPC防区配置")
@RestController
@RequestMapping("/iot/opc/zone-config")
@Validated
public class OpcZoneConfigController {

    @Resource
    private OpcZoneConfigService zoneConfigService;

    @Resource
    private IotDeviceService deviceService;

    @PostMapping("/create")
    @Operation(summary = "创建防区配置")
    @PermitAll
    public CommonResult<Long> createZoneConfig(@Valid @RequestBody OpcZoneConfigCreateReqVO createReqVO) {
        return success(zoneConfigService.createZoneConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新防区配置")
    @PermitAll
    public CommonResult<Boolean> updateZoneConfig(@Valid @RequestBody OpcZoneConfigUpdateReqVO updateReqVO) {
        zoneConfigService.updateZoneConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除防区配置")
    @Parameter(name = "id", description = "配置ID", required = true)
    @PermitAll
    public CommonResult<Boolean> deleteZoneConfig(@RequestParam("id") Long id) {
        zoneConfigService.deleteZoneConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得防区配置")
    @Parameter(name = "id", description = "配置ID", required = true)
    @PermitAll
    public CommonResult<OpcZoneConfigRespVO> getZoneConfig(@RequestParam("id") Long id) {
        OpcZoneConfigDO config = zoneConfigService.getZoneConfig(id);
        return success(buildZoneConfigRespVO(config));
    }

    @GetMapping("/page")
    @Operation(summary = "获得防区配置分页")
    @PermitAll
    public CommonResult<PageResult<OpcZoneConfigRespVO>> getZoneConfigPage(@Valid OpcZoneConfigPageReqVO pageReqVO) {
        PageResult<OpcZoneConfigDO> pageResult = zoneConfigService.getZoneConfigPage(pageReqVO);
        return success(buildZoneConfigRespVOPage(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获得设备的所有防区配置")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PermitAll
    public CommonResult<List<OpcZoneConfigRespVO>> getZoneConfigList(@RequestParam("deviceId") Long deviceId) {
        List<OpcZoneConfigDO> list = zoneConfigService.getZoneConfigListByDeviceId(deviceId);
        return success(BeanUtils.toBean(list, OpcZoneConfigRespVO.class));
    }

    /**
     * 构建防区配置响应VO
     */
    private OpcZoneConfigRespVO buildZoneConfigRespVO(OpcZoneConfigDO config) {
        if (config == null) {
            return null;
        }

        OpcZoneConfigRespVO respVO = BeanUtils.toBean(config, OpcZoneConfigRespVO.class);

        // 补充设备名称
        if (config.getDeviceId() != null) {
            IotDeviceDO device = deviceService.getDevice(config.getDeviceId());
            if (device != null) {
                respVO.setDeviceName(device.getDeviceName());
            }
        }

        // TODO: 补充摄像头名称（如果需要）
        // if (config.getCameraId() != null) {
        //     CameraDO camera = cameraService.getCamera(config.getCameraId());
        //     if (camera != null) {
        //         respVO.setCameraName(camera.getName());
        //     }
        // }

        return respVO;
    }

    /**
     * 构建防区配置响应VO分页
     */
    private PageResult<OpcZoneConfigRespVO> buildZoneConfigRespVOPage(PageResult<OpcZoneConfigDO> pageResult) {
        List<OpcZoneConfigRespVO> list = pageResult.getList().stream()
                .map(this::buildZoneConfigRespVO)
                .toList();
        return new PageResult<>(list, pageResult.getTotal());
    }
}
