package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.status.DeviceStatusBatchReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.status.DeviceStatusPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.status.DeviceStatusRespVO;
import cn.iocoder.yudao.module.iot.service.device.status.DeviceStatusService;
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
 * 设备状态查询 Controller
 * 
 * <p>提供设备在线状态的查询接口</p>
 * 
 * <p>Requirements: 5.1, 5.2, 5.3</p>
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - IoT 设备状态")
@RestController
@RequestMapping("/iot/device/status")
@Validated
public class DeviceStatusController {

    @Resource
    private DeviceStatusService deviceStatusService;

    /**
     * 查询单个设备状态
     * 
     * <p>Requirements: 5.1 - 返回设备的当前状态和最后活跃时间</p>
     * <p>Requirements: 5.4 - 查询的设备不存在时返回 INACTIVE 状态</p>
     */
    @GetMapping("/{deviceId}")
    @Operation(summary = "查询单个设备状态")
    @Parameter(name = "deviceId", description = "设备编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<DeviceStatusRespVO> getDeviceStatus(@PathVariable("deviceId") Long deviceId) {
        return success(deviceStatusService.getDeviceStatus(deviceId));
    }

    /**
     * 批量查询设备状态
     * 
     * <p>Requirements: 5.2 - 返回所有请求设备的状态列表</p>
     * <p>Requirements: 5.3 - 批量查询的设备数量不超过 100</p>
     */
    @PostMapping("/batch")
    @Operation(summary = "批量查询设备状态")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<DeviceStatusRespVO>> batchGetDeviceStatus(
            @RequestBody @Valid DeviceStatusBatchReqVO reqVO) {
        return success(deviceStatusService.batchGetDeviceStatus(reqVO.getDeviceIds()));
    }

    /**
     * 分页查询设备状态
     * 
     * <p>Requirements: 5.3 - 支持按设备类型、状态、产品ID筛选</p>
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询设备状态")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PageResult<DeviceStatusRespVO>> getDeviceStatusPage(
            @Valid DeviceStatusPageReqVO pageReqVO) {
        return success(deviceStatusService.getDeviceStatusPage(pageReqVO));
    }

}
