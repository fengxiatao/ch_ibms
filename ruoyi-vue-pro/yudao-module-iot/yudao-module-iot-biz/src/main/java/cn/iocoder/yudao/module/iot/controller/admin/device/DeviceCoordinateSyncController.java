package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.service.device.DeviceCoordinateSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 设备坐标同步控制器
 * 从DXF平面图中提取设备坐标并同步到设备表
 *
 * @author IBMS Team
 */
@Tag(name = "管理后台 - 设备坐标同步")
@RestController
@RequestMapping("/iot/device/coordinate-sync")
@Slf4j
public class DeviceCoordinateSyncController {

    @Resource
    private DeviceCoordinateSyncService coordinateSyncService;

    /**
     * 从楼层DXF中提取摄像头坐标并同步
     *
     * @param floorId 楼层ID
     * @return 同步结果
     */
    @PostMapping("/sync-cameras")
    @Operation(summary = "同步楼层摄像头坐标", description = "从DXF平面图中提取监控系统图层的摄像头坐标，并更新到设备表")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<DeviceCoordinateSyncService.SyncResult> syncCameraCoordinates(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId
    ) {
        log.info("【设备坐标同步】开始同步楼层摄像头坐标，楼层ID: {}", floorId);

        try {
            DeviceCoordinateSyncService.SyncResult result = coordinateSyncService.syncCameraCoordinates(floorId);
            
            log.info("【设备坐标同步】摄像头坐标同步完成，楼层ID: {}, 识别 {} 个，更新 {} 个",
                floorId, result.getTotalInDxf(), result.getUpdated());
            
            return success(result);

        } catch (Exception e) {
            log.error("【设备坐标同步】摄像头坐标同步失败，楼层ID: " + floorId, e);
            return CommonResult.error(500, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 从楼层DXF中提取所有设备坐标并同步
     *
     * @param floorId 楼层ID
     * @return 同步结果
     */
    @PostMapping("/sync-all")
    @Operation(summary = "同步楼层所有设备坐标", description = "从DXF平面图中提取所有图层的设备坐标，并更新到设备表")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<DeviceCoordinateSyncService.SyncResult> syncAllDeviceCoordinates(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId
    ) {
        log.info("【设备坐标同步】开始同步楼层所有设备坐标，楼层ID: {}", floorId);

        try {
            DeviceCoordinateSyncService.SyncResult result = coordinateSyncService.syncAllDeviceCoordinates(floorId);
            
            log.info("【设备坐标同步】所有设备坐标同步完成，楼层ID: {}, 识别 {} 个，更新 {} 个",
                floorId, result.getTotalInDxf(), result.getUpdated());
            
            return success(result);

        } catch (Exception e) {
            log.error("【设备坐标同步】所有设备坐标同步失败，楼层ID: " + floorId, e);
            return CommonResult.error(500, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 从楼层DXF中提取指定类型的设备坐标并同步
     *
     * @param floorId 楼层ID
     * @param deviceType 设备类型（camera/sensor/access_control等）
     * @return 同步结果
     */
    @PostMapping("/sync-by-type")
    @Operation(summary = "按类型同步设备坐标", description = "从DXF平面图中提取指定类型的设备坐标，并更新到设备表")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<DeviceCoordinateSyncService.SyncResult> syncDeviceCoordinatesByType(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId,
            @Parameter(description = "设备类型", required = true) @RequestParam("deviceType") String deviceType
    ) {
        log.info("【设备坐标同步】开始同步楼层指定类型设备坐标，楼层ID: {}, 类型: {}", floorId, deviceType);

        try {
            DeviceCoordinateSyncService.SyncResult result = 
                coordinateSyncService.syncDeviceCoordinates(floorId, deviceType);
            
            log.info("【设备坐标同步】指定类型设备坐标同步完成，楼层ID: {}, 类型: {}, 识别 {} 个，更新 {} 个",
                floorId, deviceType, result.getTotalInDxf(), result.getUpdated());
            
            return success(result);

        } catch (Exception e) {
            log.error("【设备坐标同步】指定类型设备坐标同步失败，楼层ID: " + floorId + ", 类型: " + deviceType, e);
            return CommonResult.error(500, "同步失败: " + e.getMessage());
        }
    }
}



