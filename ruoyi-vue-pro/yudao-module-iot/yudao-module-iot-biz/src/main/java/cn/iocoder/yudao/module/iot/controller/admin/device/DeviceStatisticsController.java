package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceOnlineStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceTrendReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceTrendRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceTypeStatisticsRespVO;
import cn.iocoder.yudao.module.iot.service.device.statistics.DeviceStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 设备统计 Controller
 * 
 * <p>Requirements: 3.1, 3.2, 3.3, 3.4</p>
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - IoT 设备统计")
@RestController
@RequestMapping("/iot/device/statistics")
@Validated
public class DeviceStatisticsController {

    @Resource
    private DeviceStatisticsService deviceStatisticsService;

    /**
     * 获取设备在线统计
     * 
     * <p>Requirements: 3.1, 3.2, 3.3</p>
     */
    @GetMapping("/online")
    @Operation(summary = "获取设备在线统计")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<DeviceOnlineStatisticsRespVO> getOnlineStatistics() {
        return success(deviceStatisticsService.getOnlineStatistics());
    }

    /**
     * 按设备类型获取统计数据
     * 
     * <p>Requirements: 3.2</p>
     */
    @GetMapping("/by-type")
    @Operation(summary = "按设备类型获取统计数据")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<DeviceTypeStatisticsRespVO>> getStatisticsByType() {
        return success(deviceStatisticsService.getStatisticsByType());
    }

    /**
     * 获取设备离线率
     * 
     * <p>Requirements: 3.3</p>
     */
    @GetMapping("/offline-rate")
    @Operation(summary = "获取设备离线率")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<BigDecimal> getOfflineRate() {
        return success(deviceStatisticsService.getOfflineRate());
    }

    /**
     * 获取设备历史趋势数据
     * 
     * <p>Requirements: 3.4</p>
     */
    @GetMapping("/trend")
    @Operation(summary = "获取设备历史趋势数据")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<DeviceTrendRespVO>> getTrend(@Valid DeviceTrendReqVO reqVO) {
        return success(deviceStatisticsService.getTrend(reqVO));
    }

}
