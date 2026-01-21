package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.DeviceRecognitionConfigVO;
import cn.iocoder.yudao.module.iot.controller.admin.spatial.vo.DxfInfoRespVO;
import cn.iocoder.yudao.module.iot.service.gis.FloorDxfService;
import cn.iocoder.yudao.module.iot.service.gis.FloorSvgSyncService;
import cn.iocoder.yudao.module.iot.service.spatial.DxfCoordinateExtractor;
import cn.iocoder.yudao.module.iot.service.spatial.DxfAreaRecognizer;
import cn.iocoder.yudao.module.iot.service.spatial.DxfDeviceRecognizer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 楼层DXF文件管理控制器
 * 处理楼层平面图CAD文件的上传、查看、删除
 *
 * @author 智慧建筑管理系统
 */
@Tag(name = "管理后台 - 楼层DXF文件管理")
@RestController
@RequestMapping("/iot/floor-dxf")
@Slf4j
public class FloorDxfController {

    @Resource
    private FloorDxfService floorDxfService;

    @Resource
    private FloorSvgSyncService floorSvgSyncService;

    @Resource
    private DxfCoordinateExtractor dxfCoordinateExtractor;

    @Resource
    private DxfAreaRecognizer dxfAreaRecognizer;

    @Resource
    private DxfDeviceRecognizer dxfDeviceRecognizer;

    /**
     * 为楼层上传DXF平面图文件
     *
     * @param floorId 楼层ID
     * @param file DXF文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    @Operation(summary = "为楼层上传DXF平面图")
    @PreAuthorize("@ss.hasPermission('iot:floor:update')")
    public CommonResult<Map<String, Object>> uploadFloorDxf(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId,
            @Parameter(description = "DXF文件", required = true) @RequestParam("file") MultipartFile file
    ) {
        log.info("【楼层DXF】开始上传，楼层ID: {}, 文件名: {}", floorId, file.getOriginalFilename());

        // 验证文件
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".dxf")) {
            return CommonResult.error(400, "只支持DXF格式文件");
        }

        try {
            Map<String, Object> result = floorDxfService.uploadDxfForFloor(floorId, file);
            log.info("【楼层DXF】上传成功，楼层ID: {}", floorId);
            return success(result);

        } catch (Exception e) {
            log.error("【楼层DXF】上传失败，楼层ID: " + floorId, e);
            return CommonResult.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除楼层的DXF平面图文件
     *
     * @param floorId 楼层ID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除楼层DXF平面图")
    @PreAuthorize("@ss.hasPermission('iot:floor:update')")
    public CommonResult<Boolean> deleteFloorDxf(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId
    ) {
        log.info("【楼层DXF】开始删除，楼层ID: {}", floorId);

        try {
            floorDxfService.deleteDxfForFloor(floorId);
            log.info("【楼层DXF】删除成功，楼层ID: {}", floorId);
            return success(true);

        } catch (Exception e) {
            log.error("【楼层DXF】删除失败，楼层ID: " + floorId, e);
            return CommonResult.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取楼层DXF文件信息
     *
     * @param floorId 楼层ID
     * @return DXF文件信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取楼层DXF文件信息")
    @PreAuthorize("@ss.hasPermission('iot:floor:query')")
    public CommonResult<Map<String, Object>> getFloorDxfInfo(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId
    ) {
        log.info("【楼层DXF】获取文件信息，楼层ID: {}", floorId);

        try {
            Map<String, Object> result = floorDxfService.getDxfInfoForFloor(floorId);
            return success(result);

        } catch (Exception e) {
            log.error("【楼层DXF】获取文件信息失败，楼层ID: " + floorId, e);
            return CommonResult.error(500, "获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取楼层平面图SVG（用于查看器）
     *
     * @param floorId 楼层ID
     * @param layout 布局名称（可选）
     * @param layers 图层名称，多个用逗号分隔（可选）
     * @return SVG内容
     */
    @GetMapping("/svg")
    @Operation(summary = "获取楼层平面图SVG")
    @PreAuthorize("@ss.hasPermission('iot:floor:query')")
    public CommonResult<Map<String, Object>> getFloorPlanSvg(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId,
            @Parameter(description = "布局名称") @RequestParam(value = "layout", required = false) String layout,
            @Parameter(description = "图层名称，多个用逗号分隔") @RequestParam(value = "layers", required = false) String layers
    ) {
        log.info("【楼层DXF】获取平面图SVG，楼层ID: {}, 布局: {}, 图层: {}", floorId, layout, layers);

        try {
            Map<String, Object> result = floorDxfService.getFloorPlanSvg(floorId, layout, layers);
            return success(result);

        } catch (Exception e) {
            log.error("【楼层DXF】获取SVG失败，楼层ID: " + floorId, e);
            return CommonResult.error(500, "获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取楼层DXF文件的布局列表
     *
     * @param floorId 楼层ID
     * @return 布局列表
     */
    @GetMapping("/layouts")
    @Operation(summary = "获取楼层DXF文件的布局列表")
    @PreAuthorize("@ss.hasPermission('iot:floor:query')")
    public CommonResult<Map<String, Object>> getFloorDxfLayouts(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId
    ) {
        log.info("【楼层DXF】获取布局列表，楼层ID: {}", floorId);

        try {
            Map<String, Object> result = floorDxfService.getLayoutsForFloor(floorId);
            return success(result);

        } catch (Exception e) {
            log.error("【楼层DXF】获取布局列表失败，楼层ID: " + floorId, e);
            return CommonResult.error(500, "获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取楼层DXF文件的图层列表
     *
     * @param floorId 楼层ID
     * @return 图层列表
     */
    @GetMapping("/layers")
    @Operation(summary = "获取楼层DXF文件的图层列表")
    @PreAuthorize("@ss.hasPermission('iot:floor:query')")
    public CommonResult<Map<String, Object>> getFloorDxfLayers(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId
    ) {
        log.info("【楼层DXF】获取图层列表，楼层ID: {}", floorId);

        try {
            Map<String, Object> result = floorDxfService.getLayersForFloor(floorId);
            return success(result);

        } catch (Exception e) {
            log.error("【楼层DXF】获取图层列表失败，楼层ID: " + floorId, e);
            return CommonResult.error(500, "获取失败: " + e.getMessage());
        }
    }

    /**
     * 提取DXF文件的坐标和尺寸信息（上传时调用）
     * 
     * @param file DXF文件
     * @return DXF文件信息（建筑宽度、长度、边界等）
     */
    @PostMapping("/extract-info")
    @Operation(summary = "提取DXF文件的坐标和尺寸信息")
    @PreAuthorize("@ss.hasPermission('iot:floor:update')")
    public CommonResult<DxfInfoRespVO> extractDxfInfo(
            @Parameter(description = "DXF文件", required = true) @RequestParam("file") MultipartFile file
    ) {
        log.info("【Aspose.CAD】开始提取DXF信息，文件名: {}", file.getOriginalFilename());

        // 验证文件
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".dxf")) {
            return CommonResult.error(400, "只支持DXF格式文件");
        }

        try {
            // 提取DXF信息
            DxfCoordinateExtractor.DxfFileInfo dxfInfo = 
                    dxfCoordinateExtractor.extractDxfInfo(file.getInputStream());

            // 转换为VO
            DxfInfoRespVO respVO = new DxfInfoRespVO();
            respVO.setBuildingWidth(dxfInfo.getBuildingWidth());
            respVO.setBuildingHeight(dxfInfo.getBuildingHeight());
            respVO.setBuildingDepth(dxfInfo.getBuildingDepth());
            respVO.setMinX(dxfInfo.getMinX());
            respVO.setMinY(dxfInfo.getMinY());
            respVO.setMinZ(dxfInfo.getMinZ());
            respVO.setMaxX(dxfInfo.getMaxX());
            respVO.setMaxY(dxfInfo.getMaxY());
            respVO.setMaxZ(dxfInfo.getMaxZ());
            respVO.setEntityCount(dxfInfo.getEntityCount());
            respVO.setLayerCount(dxfInfo.getLayerCount());
            respVO.setUnit(dxfInfo.getUnit());
            respVO.setScaleFactor(dxfInfo.getScaleFactor());

            log.info("【Aspose.CAD】DXF信息提取成功: {}m × {}m", 
                    dxfInfo.getBuildingWidth(), dxfInfo.getBuildingHeight());

            return success(respVO);

        } catch (Exception e) {
            log.error("【Aspose.CAD】提取DXF信息失败", e);
            return CommonResult.error(500, "提取失败: " + e.getMessage());
        }
    }

    /**
     * 识别DXF文件中的区域（上传时调用）
     * 
     * @param file DXF文件
     * @return 识别到的区域列表
     */
    @PostMapping("/recognize-areas")
    @Operation(summary = "识别DXF文件中的区域（房间）")
    @PreAuthorize("@ss.hasPermission('iot:floor:update')")
    public CommonResult<List<DxfAreaRecognizer.RecognizedArea>> recognizeAreas(
            @Parameter(description = "DXF文件", required = true) @RequestParam("file") MultipartFile file
    ) {
        log.info("【Aspose.CAD】开始识别DXF区域，文件名: {}", file.getOriginalFilename());

        // 验证文件
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".dxf")) {
            return CommonResult.error(400, "只支持DXF格式文件");
        }

        try {
            // 识别区域
            List<DxfAreaRecognizer.RecognizedArea> areas = 
                    dxfAreaRecognizer.recognizeAreas(file.getInputStream());

            log.info("【Aspose.CAD】区域识别完成，共找到 {} 个区域", areas.size());

            return success(areas);

        } catch (Exception e) {
            log.error("【Aspose.CAD】识别DXF区域失败", e);
            return CommonResult.error(500, "识别失败: " + e.getMessage());
        }
    }

    /**
     * 识别DXF文件中的设备符号（上传时调用）
     * 
     * @param file DXF文件
     * @return 识别到的设备列表
     */
    @PostMapping("/recognize-devices")
    @Operation(summary = "识别DXF文件中的设备符号（摄像头、传感器等）")
    @PreAuthorize("@ss.hasPermission('iot:floor:update')")
    public CommonResult<Map<String, Object>> recognizeDevices(
            @Parameter(description = "DXF文件", required = true) @RequestParam("file") MultipartFile file
    ) {
        log.info("【Aspose.CAD】开始识别DXF设备符号，文件名: {}", file.getOriginalFilename());

        // 验证文件
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".dxf")) {
            return CommonResult.error(400, "只支持DXF格式文件");
        }

        try {
            // 识别设备
            List<DxfDeviceRecognizer.RecognizedDevice> devices = 
                    dxfDeviceRecognizer.recognizeDevices(file.getInputStream());

            // 统计设备类型
            Map<String, Integer> statistics = dxfDeviceRecognizer.getDeviceStatistics(devices);

            log.info("【Aspose.CAD】设备识别完成，共找到 {} 个设备", devices.size());

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("devices", devices);
            result.put("statistics", statistics);
            result.put("totalCount", devices.size());

            return success(result);

        } catch (Exception e) {
            log.error("【Aspose.CAD】识别DXF设备失败", e);
            return CommonResult.error(500, "识别失败: " + e.getMessage());
        }
    }

    /**
     * 一次性识别DXF中的区域和设备
     * 
     * @param file DXF文件
     * @return 识别结果（包含区域和设备）
     */
    @PostMapping("/recognize-all")
    @Operation(summary = "识别DXF文件中的区域和设备")
    @PreAuthorize("@ss.hasPermission('iot:floor:update')")
    public CommonResult<Map<String, Object>> recognizeAll(
            @Parameter(description = "DXF文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "设备识别配置（JSON字符串）", required = false) @RequestParam(value = "deviceConfig", required = false) String deviceConfigJson
    ) {
        log.info("【Aspose.CAD】开始识别DXF区域和设备，文件名: {}", file.getOriginalFilename());

        // 验证文件
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".dxf")) {
            return CommonResult.error(400, "只支持DXF格式文件");
        }

        try {
            // 解析设备识别配置
            List<DeviceRecognitionConfigVO> deviceConfig = null;
            if (deviceConfigJson != null && !deviceConfigJson.trim().isEmpty() && !"null".equals(deviceConfigJson)) {
                try {
                    deviceConfig = JSONUtil.toList(deviceConfigJson, DeviceRecognitionConfigVO.class);
                } catch (Exception e) {
                    log.warn("【Aspose.CAD】解析设备识别配置失败: {}", e.getMessage());
                }
            }

            // 识别区域
            byte[] fileBytes = file.getBytes();
            List<DxfAreaRecognizer.RecognizedArea> areas = 
                    dxfAreaRecognizer.recognizeAreas(new java.io.ByteArrayInputStream(fileBytes));

            // 识别设备（使用配置）
            List<DxfDeviceRecognizer.RecognizedDevice> devices;
            if (deviceConfig != null && !deviceConfig.isEmpty()) {
                devices = dxfDeviceRecognizer.recognizeDevicesWithConfig(
                        new java.io.ByteArrayInputStream(fileBytes), deviceConfig);
            } else {
                devices = dxfDeviceRecognizer.recognizeDevices(
                        new java.io.ByteArrayInputStream(fileBytes));
            }

            // 匹配设备到区域
            dxfDeviceRecognizer.matchDevicesToAreas(devices, areas);

            // 统计信息
            Map<String, Integer> deviceStats = dxfDeviceRecognizer.getDeviceStatistics(devices);

            log.info("【Aspose.CAD】识别完成，区域: {} 个，设备: {} 个", areas.size(), devices.size());

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("areas", areas);
            result.put("devices", devices);
            result.put("deviceStatistics", deviceStats);
            result.put("areaCount", areas.size());
            result.put("deviceCount", devices.size());

            return success(result);

        } catch (Exception e) {
            log.error("【Aspose.CAD】识别DXF失败", e);
            return CommonResult.error(500, "识别失败: " + e.getMessage());
        }
    }

    @PostMapping("/recognize-by-floor")
    @Operation(summary = "识别已上传的DXF文件（通过楼层ID）")
    @PreAuthorize("@ss.hasPermission('iot:floor:query')")
    public CommonResult<Map<String, Object>> recognizeByFloorId(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId,
            @Parameter(description = "设备识别配置", required = false) @RequestBody(required = false) List<DeviceRecognitionConfigVO> deviceConfig
    ) {
        log.info("【Aspose.CAD】开始识别楼层DXF，楼层ID: {}，配置规则数: {}", 
                floorId, deviceConfig != null ? deviceConfig.size() : 0);

        try {
            // 获取已上传的DXF文件
            java.io.InputStream dxfStream = floorDxfService.getDxfFileStream(floorId);
            if (dxfStream == null) {
                return CommonResult.error(404, "该楼层尚未上传DXF文件");
            }

            // 读取文件内容到字节数组
            byte[] fileBytes = dxfStream.readAllBytes();
            dxfStream.close();

            // 识别区域
            List<DxfAreaRecognizer.RecognizedArea> areas =
                    dxfAreaRecognizer.recognizeAreas(new java.io.ByteArrayInputStream(fileBytes));

            // 识别设备（使用配置）
            List<DxfDeviceRecognizer.RecognizedDevice> devices;
            if (deviceConfig != null && !deviceConfig.isEmpty()) {
                devices = dxfDeviceRecognizer.recognizeDevicesWithConfig(
                        new java.io.ByteArrayInputStream(fileBytes), deviceConfig);
            } else {
                devices = dxfDeviceRecognizer.recognizeDevices(
                        new java.io.ByteArrayInputStream(fileBytes));
            }

            // 匹配设备到区域
            dxfDeviceRecognizer.matchDevicesToAreas(devices, areas);

            // 统计信息
            Map<String, Integer> deviceStats = dxfDeviceRecognizer.getDeviceStatistics(devices);

            log.info("【Aspose.CAD】识别完成，区域: {} 个，设备: {} 个", areas.size(), devices.size());

            // 🔧 自动同步SVG数据到floor表（新增）
            try {
                log.info("【SVG同步】识别完成后自动同步SVG数据...");
                Map<String, Object> svgSyncResult = floorSvgSyncService.syncFloorSvgData(floorId);
                log.info("【SVG同步】同步完成: {}", svgSyncResult);
            } catch (Exception syncEx) {
                log.warn("【SVG同步】同步失败，但不影响识别结果: {}", syncEx.getMessage());
            }

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("areas", areas);
            result.put("devices", devices);
            result.put("deviceStatistics", deviceStats);
            result.put("areaCount", areas.size());
            result.put("deviceCount", devices.size());

            return success(result);

        } catch (Exception e) {
            log.error("【Aspose.CAD】识别楼层DXF失败，楼层ID: " + floorId, e);
            return CommonResult.error(500, "识别失败: " + e.getMessage());
        }
    }

    /**
     * 同步楼层SVG数据（手动触发）
     *
     * @param floorId 楼层ID
     * @return 同步结果
     */
    @PostMapping("/sync-svg")
    @Operation(summary = "同步楼层SVG数据")
    @PreAuthorize("@ss.hasPermission('iot:floor:update')")
    public CommonResult<Map<String, Object>> syncFloorSvg(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId
    ) {
        log.info("【SVG同步】手动触发SVG数据同步，楼层ID: {}", floorId);

        try {
            Map<String, Object> result = floorSvgSyncService.syncFloorSvgData(floorId);
            return success(result);
        } catch (Exception e) {
            log.error("【SVG同步】同步失败，楼层ID: " + floorId, e);
            return CommonResult.error(500, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 获取DXF文件内容（用于前端直接解析）
     */
    @GetMapping("/dxf-content")
    @Operation(summary = "获取DXF文件内容", description = "返回DXF文件原始文本内容，用于前端直接解析（无水印）")
    @PreAuthorize("@ss.hasPermission('iot:floor:query')")
    public CommonResult<String> getDxfFileContent(
            @Parameter(description = "楼层ID", required = true) @RequestParam("floorId") Long floorId
    ) {
        log.info("【DXF获取】获取DXF文件内容，楼层ID: {}", floorId);
        try {
            String dxfContent = floorDxfService.getDxfFileContent(floorId);
            if (dxfContent == null || dxfContent.isEmpty()) {
                return CommonResult.error(404, "该楼层没有上传DXF文件");
            }
            log.info("【DXF获取】成功获取DXF内容，长度: {} 字符", dxfContent.length());
            return success(dxfContent);
        } catch (Exception e) {
            log.error("【DXF获取】获取失败，楼层ID: " + floorId, e);
            return CommonResult.error(500, "获取DXF文件失败: " + e.getMessage());
        }
    }
}



















































