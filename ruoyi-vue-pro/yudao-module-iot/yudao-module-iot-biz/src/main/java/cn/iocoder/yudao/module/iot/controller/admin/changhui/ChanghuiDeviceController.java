package cn.iocoder.yudao.module.iot.controller.admin.changhui;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceRegisterReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceUpdateReqVO;
import cn.iocoder.yudao.module.iot.service.changhui.device.ChanghuiDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 长辉设备 Controller
 * 
 * <p>统一管理长辉、德通等使用相同TCP协议的设备
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - 长辉设备")
@RestController
@RequestMapping("/iot/changhui/device")
@Validated
public class ChanghuiDeviceController {

    @Resource
    private ChanghuiDeviceService deviceService;

    @PostMapping("/register")
    @Operation(summary = "注册设备")
    @PreAuthorize("@ss.hasPermission('iot:changhui-device:create')")
    public CommonResult<Long> registerDevice(@Valid @RequestBody ChanghuiDeviceRegisterReqVO reqVO) {
        return success(deviceService.registerDevice(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备")
    @PreAuthorize("@ss.hasPermission('iot:changhui-device:update')")
    public CommonResult<Boolean> updateDevice(@Valid @RequestBody ChanghuiDeviceUpdateReqVO reqVO) {
        deviceService.updateDevice(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备")
    @Parameter(name = "id", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-device:delete')")
    public CommonResult<Boolean> deleteDevice(@RequestParam("id") Long id) {
        deviceService.deleteDevice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取设备")
    @Parameter(name = "id", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-device:query')")
    public CommonResult<ChanghuiDeviceRespVO> getDevice(@RequestParam("id") Long id) {
        return success(deviceService.getDevice(id));
    }

    @GetMapping("/get-by-station-code")
    @Operation(summary = "根据测站编码获取设备")
    @Parameter(name = "stationCode", description = "测站编码", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-device:query')")
    public CommonResult<ChanghuiDeviceRespVO> getDeviceByStationCode(@RequestParam("stationCode") String stationCode) {
        return success(deviceService.getDeviceByStationCode(stationCode));
    }

    @GetMapping("/page")
    @Operation(summary = "获取设备分页")
    @PreAuthorize("@ss.hasPermission('iot:changhui-device:query')")
    public CommonResult<PageResult<ChanghuiDeviceRespVO>> getDevicePage(@Valid ChanghuiDevicePageReqVO reqVO) {
        return success(deviceService.getDevicePage(reqVO));
    }

    @GetMapping("/online")
    @Operation(summary = "获取在线设备列表")
    @PreAuthorize("@ss.hasPermission('iot:changhui-device:query')")
    public CommonResult<List<ChanghuiDeviceRespVO>> getOnlineDevices() {
        return success(deviceService.getOnlineDevices());
    }

}
