package cn.iocoder.yudao.module.iot.service.spatial;

import com.aspose.cad.Image;
import com.aspose.cad.fileformats.cad.CadImage;
import com.aspose.cad.fileformats.cad.cadobjects.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * DXF坐标提取器
 * 使用Aspose.CAD从DXF文件中提取建筑尺寸、坐标信息
 *
 * @author IBMS Team
 */
@Service
@Slf4j
public class DxfCoordinateExtractor {

    /**
     * DXF文件信息
     */
    @Data
    public static class DxfFileInfo {
        /** 最小X坐标 */
        private BigDecimal minX;
        /** 最小Y坐标 */
        private BigDecimal minY;
        /** 最小Z坐标 */
        private BigDecimal minZ;
        /** 最大X坐标 */
        private BigDecimal maxX;
        /** 最大Y坐标 */
        private BigDecimal maxY;
        /** 最大Z坐标 */
        private BigDecimal maxZ;
        /** 建筑宽度（米） */
        private BigDecimal buildingWidth;
        /** 建筑长度（米） */
        private BigDecimal buildingHeight;
        /** 建筑高度（米，Z轴） */
        private BigDecimal buildingDepth;
        /** 实体数量 */
        private int entityCount;
        /** 图层数量 */
        private int layerCount;
        /** 单位（默认毫米） */
        private String unit = "mm";
        /** 缩放因子（转换为米） */
        private BigDecimal scaleFactor = BigDecimal.valueOf(0.001); // 默认mm转m
    }

    /**
     * 点击位置信息
     */
    @Data
    public static class ClickLocation {
        /** X坐标（米） */
        private BigDecimal x;
        /** Y坐标（米） */
        private BigDecimal y;
        /** Z坐标（米） */
        private BigDecimal z;
        /** 所在区域ID（如果能识别） */
        private Long areaId;
        /** 所在区域名称 */
        private String areaName;
    }

    /**
     * 提取DXF文件的坐标信息和尺寸
     *
     * @param inputStream DXF文件输入流
     * @return DXF文件信息
     */
    public DxfFileInfo extractDxfInfo(InputStream inputStream) {
        try {
            log.info("【Aspose.CAD】开始提取DXF坐标信息");

            // 加载DXF文件
            Image image = Image.load(inputStream);

            if (!(image instanceof CadImage)) {
                throw new IllegalArgumentException("文件不是有效的CAD/DXF格式");
            }

            CadImage cadImage = (CadImage) image;
            DxfFileInfo info = new DxfFileInfo();

            // 1. 获取图形边界
            extractBounds(cadImage, info);

            // 2. 计算建筑尺寸
            calculateBuildingDimensions(info);

            // 3. 检测单位
            detectUnit(cadImage, info);

            // 4. 统计信息
            info.setEntityCount(cadImage.getEntities().length);
            info.setLayerCount(cadImage.getLayers().size());

            log.info("【Aspose.CAD】DXF信息提取完成: {}m × {}m × {}m",
                    info.getBuildingWidth(), info.getBuildingHeight(), info.getBuildingDepth());

            // 释放资源
            cadImage.dispose();

            return info;

        } catch (Exception e) {
            log.error("【Aspose.CAD】提取DXF信息失败", e);
            throw new RuntimeException("提取DXF信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取图形边界
     */
    private void extractBounds(CadImage cadImage, DxfFileInfo info) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxZ = Double.MIN_VALUE;

        try {
            // 方法1: 使用CadImage的边界属性（如果可用）
            // 注意：Aspose.CAD的API可能因版本而异
            try {
                // 尝试获取内置边界
                Object minPoint = cadImage.getMinPoint();
                Object maxPoint = cadImage.getMaxPoint();

                if (minPoint != null && maxPoint != null) {
                    // 解析minPoint和maxPoint
                    log.info("【Aspose.CAD】找到内置边界");
                    // 这里需要根据实际API调整
                }
            } catch (Exception e) {
                log.debug("【Aspose.CAD】无法直接获取边界，改用遍历实体方式");
            }

            // 方法2: 遍历所有实体，计算边界
            Object[] entities = cadImage.getEntities();
            log.info("【Aspose.CAD】开始遍历 {} 个实体", entities.length);

            for (Object entity : entities) {
                if (entity == null) continue;

                try {
                    // 根据实体类型提取坐标
                    if (entity instanceof CadLine) {
                        CadLine line = (CadLine) entity;
                        Cad3DPoint p1 = line.getFirstPoint();
                        Cad3DPoint p2 = line.getSecondPoint();
                        
                        minX = Math.min(minX, Math.min(p1.getX(), p2.getX()));
                        minY = Math.min(minY, Math.min(p1.getY(), p2.getY()));
                        minZ = Math.min(minZ, Math.min(p1.getZ(), p2.getZ()));
                        maxX = Math.max(maxX, Math.max(p1.getX(), p2.getX()));
                        maxY = Math.max(maxY, Math.max(p1.getY(), p2.getY()));
                        maxZ = Math.max(maxZ, Math.max(p1.getZ(), p2.getZ()));

                    } else if (entity instanceof CadCircle) {
                        CadCircle circle = (CadCircle) entity;
                        double cx = circle.getCenterPoint().getX();
                        double cy = circle.getCenterPoint().getY();
                        double cz = circle.getCenterPoint().getZ();
                        double r = circle.getRadius();

                        minX = Math.min(minX, cx - r);
                        minY = Math.min(minY, cy - r);
                        minZ = Math.min(minZ, cz);
                        maxX = Math.max(maxX, cx + r);
                        maxY = Math.max(maxY, cy + r);
                        maxZ = Math.max(maxZ, cz);

                    } else if (entity instanceof CadArc) {
                        CadArc arc = (CadArc) entity;
                        double cx = arc.getCenterPoint().getX();
                        double cy = arc.getCenterPoint().getY();
                        double cz = arc.getCenterPoint().getZ();
                        double r = arc.getRadius();

                        minX = Math.min(minX, cx - r);
                        minY = Math.min(minY, cy - r);
                        minZ = Math.min(minZ, cz);
                        maxX = Math.max(maxX, cx + r);
                        maxY = Math.max(maxY, cy + r);
                        maxZ = Math.max(maxZ, cz);
                    }
                    // 可以继续添加其他实体类型...

                } catch (Exception e) {
                    log.debug("【Aspose.CAD】跳过实体: {}", entity.getClass().getSimpleName());
                }
            }

        } catch (Exception e) {
            log.error("【Aspose.CAD】提取边界失败", e);
        }

        // 保存边界信息
        info.setMinX(BigDecimal.valueOf(minX));
        info.setMinY(BigDecimal.valueOf(minY));
        info.setMinZ(BigDecimal.valueOf(minZ));
        info.setMaxX(BigDecimal.valueOf(maxX));
        info.setMaxY(BigDecimal.valueOf(maxY));
        info.setMaxZ(BigDecimal.valueOf(maxZ));

        log.info("【Aspose.CAD】边界: X[{} ~ {}], Y[{} ~ {}], Z[{} ~ {}]",
                minX, maxX, minY, maxY, minZ, maxZ);
    }

    /**
     * 更新边界（辅助方法）
     */
    private void updateBounds(Object point, Runnable updater) {
        if (point != null) {
            updater.run();
        }
    }

    /**
     * 计算建筑尺寸
     */
    private void calculateBuildingDimensions(DxfFileInfo info) {
        // 计算原始尺寸（CAD单位）
        BigDecimal widthInCadUnits = info.getMaxX().subtract(info.getMinX());
        BigDecimal heightInCadUnits = info.getMaxY().subtract(info.getMinY());
        BigDecimal depthInCadUnits = info.getMaxZ().subtract(info.getMinZ());

        // 转换为米
        info.setBuildingWidth(widthInCadUnits.multiply(info.getScaleFactor())
                .setScale(2, RoundingMode.HALF_UP));
        info.setBuildingHeight(heightInCadUnits.multiply(info.getScaleFactor())
                .setScale(2, RoundingMode.HALF_UP));
        info.setBuildingDepth(depthInCadUnits.multiply(info.getScaleFactor())
                .setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 检测单位
     * 根据图形大小推测单位（毫米、厘米还是米）
     */
    private void detectUnit(CadImage cadImage, DxfFileInfo info) {
        try {
            // 尝试从CAD文件中读取单位设置
            // 注意：这部分API可能需要根据Aspose.CAD版本调整
            
            // 简单启发式：根据数值范围判断
            double width = info.getMaxX().subtract(info.getMinX()).doubleValue();
            double height = info.getMaxY().subtract(info.getMinY()).doubleValue();
            double maxDim = Math.max(width, height);

            if (maxDim < 100) {
                // 可能是米
                info.setUnit("m");
                info.setScaleFactor(BigDecimal.ONE);
                log.info("【Aspose.CAD】推测单位: 米 (m)");
            } else if (maxDim < 10000) {
                // 可能是厘米
                info.setUnit("cm");
                info.setScaleFactor(BigDecimal.valueOf(0.01));
                log.info("【Aspose.CAD】推测单位: 厘米 (cm)");
            } else {
                // 可能是毫米
                info.setUnit("mm");
                info.setScaleFactor(BigDecimal.valueOf(0.001));
                log.info("【Aspose.CAD】推测单位: 毫米 (mm)");
            }

        } catch (Exception e) {
            log.warn("【Aspose.CAD】无法检测单位，使用默认值（毫米）");
        }
    }

    /**
     * 将像素坐标转换为DXF坐标（米）
     *
     * @param pixelX 像素X坐标
     * @param pixelY 像素Y坐标
     * @param imageWidth SVG/图片宽度（像素）
     * @param imageHeight SVG/图片高度（像素）
     * @param dxfInfo DXF文件信息
     * @return 点击位置信息
     */
    public ClickLocation pixelToReal(int pixelX, int pixelY, 
                                     int imageWidth, int imageHeight, 
                                     DxfFileInfo dxfInfo) {
        
        // 计算比例
        BigDecimal scaleX = dxfInfo.getBuildingWidth().divide(
                BigDecimal.valueOf(imageWidth), 10, RoundingMode.HALF_UP);
        BigDecimal scaleY = dxfInfo.getBuildingHeight().divide(
                BigDecimal.valueOf(imageHeight), 10, RoundingMode.HALF_UP);

        // 转换坐标（注意Y轴反转）
        BigDecimal realX = BigDecimal.valueOf(pixelX).multiply(scaleX);
        BigDecimal realY = dxfInfo.getBuildingHeight().subtract(
                BigDecimal.valueOf(pixelY).multiply(scaleY));

        ClickLocation location = new ClickLocation();
        location.setX(realX.setScale(2, RoundingMode.HALF_UP));
        location.setY(realY.setScale(2, RoundingMode.HALF_UP));
        location.setZ(BigDecimal.ZERO); // 默认地面

        return location;
    }

    /**
     * 将DXF坐标（米）转换为像素坐标
     *
     * @param realX 真实X坐标（米）
     * @param realY 真实Y坐标（米）
     * @param imageWidth SVG/图片宽度（像素）
     * @param imageHeight SVG/图片高度（像素）
     * @param dxfInfo DXF文件信息
     * @return 像素坐标数组 [pixelX, pixelY]
     */
    public int[] realToPixel(BigDecimal realX, BigDecimal realY,
                             int imageWidth, int imageHeight,
                             DxfFileInfo dxfInfo) {
        
        // 计算比例
        BigDecimal scaleX = BigDecimal.valueOf(imageWidth).divide(
                dxfInfo.getBuildingWidth(), 10, RoundingMode.HALF_UP);
        BigDecimal scaleY = BigDecimal.valueOf(imageHeight).divide(
                dxfInfo.getBuildingHeight(), 10, RoundingMode.HALF_UP);

        // 转换坐标（注意Y轴反转）
        int pixelX = realX.multiply(scaleX).intValue();
        int pixelY = dxfInfo.getBuildingHeight().subtract(realY)
                .multiply(scaleY).intValue();

        return new int[]{pixelX, pixelY};
    }
}

