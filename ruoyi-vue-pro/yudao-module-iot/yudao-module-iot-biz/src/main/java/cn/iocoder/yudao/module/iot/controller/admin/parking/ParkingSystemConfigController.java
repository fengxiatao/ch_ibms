package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.system.ParkingSystemConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.system.ParkingSystemConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingSystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 停车系统配置")
@RestController
@RequestMapping("/iot/parking/system")
@Validated
public class ParkingSystemConfigController {

    @Resource
    private ParkingSystemConfigService parkingSystemConfigService;

    @GetMapping("/get")
    @Operation(summary = "获得停车系统配置")
    @Parameter(name = "lotId", description = "停车场ID", required = false)
    @PreAuthorize("@ss.hasPermission('iot:parking:system:query')")
    public CommonResult<ParkingSystemConfigRespVO> getConfig(
            @RequestParam(value = "lotId", required = false) Long lotId) {
        return success(BeanUtils.toBean(parkingSystemConfigService.getConfig(lotId),
                ParkingSystemConfigRespVO.class));
    }

    @PutMapping("/save")
    @Operation(summary = "保存停车系统配置")
    @PreAuthorize("@ss.hasPermission('iot:parking:system:update')")
    public CommonResult<Boolean> saveConfig(@Valid @RequestBody ParkingSystemConfigSaveReqVO reqVO) {
        parkingSystemConfigService.saveConfig(reqVO);
        return success(true);
    }
}

