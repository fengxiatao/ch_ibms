package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.FloorMapper;
import cn.iocoder.yudao.module.iot.service.spatial.DxfCoordinateExtractor;
import cn.iocoder.yudao.module.iot.service.spatial.DxfToSvgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 楼层SVG数据同步服务
 * 用于在DXF识别后自动保存SVG数据到floor表
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author 智慧建筑管理系统
 */
@Service
@Slf4j
public class FloorSvgSyncService {

    @Resource
    private FloorMapper floorMapper;

    @Resource
    private DxfToSvgService dxfToSvgService;

    @Resource
    private DxfCoordinateExtractor dxfCoordinateExtractor;

    /**
     * 同步楼层的DXF数据到SVG字段
     * 在DXF识别后自动调用，保存0图层的SVG数据
     *
     * @param floorId 楼层ID
     * @return 同步结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncFloorSvgData(Long floorId) throws Exception {
        log.info("[楼层SVG同步] 开始同步，楼层ID: {}", floorId);

        // 1. 获取楼层信息
        FloorDO floor = floorMapper.selectById(floorId);
        if (floor == null) {
            throw new IllegalArgumentException("楼层不存在，ID: " + floorId);
        }

        if (floor.getDxfFilePath() == null) {
            throw new IllegalArgumentException("该楼层没有绑定DXF文件");
        }

        // 2. 读取DXF文件
        File dxfFile = new File(floor.getDxfFilePath());
        if (!dxfFile.exists()) {
            throw new IllegalArgumentException("DXF文件不存在: " + floor.getDxfFilePath());
        }

        Map<String, Object> result = new HashMap<>();

        try (InputStream inputStream = new FileInputStream(dxfFile)) {
            // 3. 提取0图层的SVG（平面图轮廓）
            log.info("[楼层SVG同步] 开始提取0图层SVG...");
            byte[] fileBytes = inputStream.readAllBytes();
            
            // 重新创建输入流
            try (InputStream svgInputStream = new java.io.ByteArrayInputStream(fileBytes)) {
                // 调用正确的方法：convertDxfToSvgByLayoutAndLayers
                String layer0Svg = dxfToSvgService.convertDxfToSvgByLayoutAndLayers(
                    svgInputStream,
                    "Model",  // 使用Model布局
                    java.util.Arrays.asList("0"),  // 只提取0图层
                    1920,  // SVG宽度
                    1080   // SVG高度
                );

                if (layer0Svg != null && !layer0Svg.isEmpty()) {
                    log.info("[楼层SVG同步] 0图层SVG提取成功，长度: {} 字符", layer0Svg.length());
                    result.put("layer0SvgLength", layer0Svg.length());
                    result.put("layer0SvgExtracted", true);

                    // 4. 提取坐标范围信息（用于计算比例）
                    try (InputStream coordInputStream = new java.io.ByteArrayInputStream(fileBytes)) {
                        // 调用正确的方法：extractDxfInfo
                        DxfCoordinateExtractor.DxfFileInfo coordInfo = dxfCoordinateExtractor.extractDxfInfo(coordInputStream);
                        
                        // 计算实际建筑尺寸（已经是米）
                        double buildingWidth = coordInfo.getBuildingWidth().doubleValue();
                        double buildingLength = coordInfo.getBuildingHeight().doubleValue();
                        
                        log.info("[楼层SVG同步] 建筑尺寸: 宽 {} 米，长 {} 米", 
                            String.format("%.2f", buildingWidth), 
                            String.format("%.2f", buildingLength));

                        // SVG视图大小（与上面convertDxfToSvgByLayoutAndLayers的参数一致）
                        int svgWidth = 1920;
                        int svgHeight = 1080;
                        
                        // 计算坐标比例（像素/米）
                        double scaleX = svgWidth / buildingWidth;
                        double scaleY = svgHeight / buildingLength;
                        double coordinateScale = Math.min(scaleX, scaleY);  // 使用较小的比例以确保全部显示

                        log.info("[楼层SVG同步] 坐标比例: {} 像素/米", String.format("%.2f", coordinateScale));

                        // 5. 更新数据库
                        FloorDO updateFloor = new FloorDO();
                        updateFloor.setId(floorId);
                        updateFloor.setDxfLayer0Svg(layer0Svg);
                        updateFloor.setBuildingWidth(java.math.BigDecimal.valueOf(buildingWidth));
                        updateFloor.setBuildingLength(java.math.BigDecimal.valueOf(buildingLength));
                        updateFloor.setCoordinateScale(java.math.BigDecimal.valueOf(coordinateScale));
                        updateFloor.setFloorPlanGeneratedAt(LocalDateTime.now());

                        floorMapper.updateById(updateFloor);

                        log.info("[楼层SVG同步] 数据库更新成功");

                        result.put("buildingWidth", buildingWidth);
                        result.put("buildingLength", buildingLength);
                        result.put("coordinateScale", coordinateScale);
                        result.put("updated", true);
                    }
                } else {
                    log.warn("[楼层SVG同步] 0图层SVG为空");
                    result.put("layer0SvgExtracted", false);
                }
            }

        } catch (Exception e) {
            log.error("[楼层SVG同步] 同步失败", e);
            throw e;
        }

        result.put("floorId", floorId);
        result.put("floorName", floor.getName());
        result.put("success", true);

        log.info("[楼层SVG同步] 同步完成: {}", result);
        return result;
    }

    /**
     * 批量同步所有有DXF但没有SVG的楼层
     *
     * @return 同步结果统计
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncAllFloorsWithDxf() {
        log.info("[楼层SVG同步] 开始批量同步所有楼层...");

        // 查询所有有DXF但没有SVG的楼层
        // TODO: 这里需要根据实际的Mapper方法实现
        // List<FloorDO> floors = floorMapper.selectFloorsWithDxfButNoSvg();

        Map<String, Object> result = new HashMap<>();
        result.put("message", "批量同步功能待实现");
        
        return result;
    }
}

