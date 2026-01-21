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
 * DXF区域识别器（简化版）
 * 从DXF文件中识别封闭多边形（房间/区域）
 *
 * @author IBMS Team
 */
@Service
@Slf4j
public class DxfAreaRecognizer {

    /**
     * 识别的区域信息
     */
    @Data
    public static class RecognizedArea {
        private String name;
        private String code;
        private String areaType;
        private String layerName;
        private List<Point> vertices;
        private BoundingBox bounds;
        private BigDecimal area;
        private Point center;
        private Boolean selected = false;
    }

    @Data
    public static class Point {
        private BigDecimal x;
        private BigDecimal y;

        public Point(BigDecimal x, BigDecimal y) {
            this.x = x;
            this.y = y;
        }
    }

    @Data
    public static class BoundingBox {
        private BigDecimal minX;
        private BigDecimal minY;
        private BigDecimal maxX;
        private BigDecimal maxY;
    }

    /**
     * 从DXF文件识别区域
     */
    public List<RecognizedArea> recognizeAreas(InputStream inputStream) {
        List<RecognizedArea> areas = new ArrayList<>();

        try {
            log.info("【Aspose.CAD】开始识别DXF区域");

            Image image = Image.load(inputStream);
            if (!(image instanceof CadImage)) {
                throw new IllegalArgumentException("文件不是有效的CAD/DXF格式");
            }

            CadImage cadImage = (CadImage) image;

            // 识别圆形区域（简化版，只识别圆形）
            recognizeCircles(cadImage, areas);

            // 生成区域名称
            generateAreaNames(areas);

            log.info("【Aspose.CAD】识别完成，共找到 {} 个区域", areas.size());

            cadImage.dispose();
            return areas;

        } catch (Exception e) {
            log.error("【Aspose.CAD】识别DXF区域失败", e);
            throw new RuntimeException("识别区域失败: " + e.getMessage(), e);
        }
    }

    /**
     * 识别圆形区域
     */
    private void recognizeCircles(CadImage cadImage, List<RecognizedArea> areas) {
        try {
            Object[] entities = cadImage.getEntities();
            int areaIndex = 1;

            for (Object entity : entities) {
                if (entity instanceof CadCircle) {
                    CadCircle circle = (CadCircle) entity;
                    // 只识别大圆作为区域（半径>500mm）
                    if (circle.getRadius() > 500) {
                        RecognizedArea area = createAreaFromCircle(circle, areaIndex++);
                        if (area != null) {
                            areas.add(area);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("【Aspose.CAD】识别圆形区域失败", e);
        }
    }

    /**
     * 从Circle创建区域
     */
    private RecognizedArea createAreaFromCircle(CadCircle circle, int index) {
        try {
            RecognizedArea area = new RecognizedArea();
            area.setCode("CIRCLE_" + String.format("%03d", index));
            area.setLayerName(circle.getLayerName());
            area.setAreaType("circle");

            // 圆心和半径（转换为米）
            BigDecimal cx = BigDecimal.valueOf(circle.getCenterPoint().getX() * 0.001)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal cy = BigDecimal.valueOf(circle.getCenterPoint().getY() * 0.001)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal r = BigDecimal.valueOf(circle.getRadius() * 0.001)
                    .setScale(2, RoundingMode.HALF_UP);

            // 用多边形近似圆（36个点）
            List<Point> points = new ArrayList<>();
            for (int i = 0; i < 36; i++) {
                double angle = 2 * Math.PI * i / 36;
                BigDecimal x = cx.add(BigDecimal.valueOf(Math.cos(angle)).multiply(r))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal y = cy.add(BigDecimal.valueOf(Math.sin(angle)).multiply(r))
                        .setScale(2, RoundingMode.HALF_UP);
                points.add(new Point(x, y));
            }

            area.setVertices(points);
            area.setBounds(calculateBounds(points));
            
            // 圆的面积 = π * r²
            area.setArea(BigDecimal.valueOf(Math.PI)
                    .multiply(r.multiply(r))
                    .setScale(2, RoundingMode.HALF_UP));
            
            area.setCenter(new Point(cx, cy));

            return area;

        } catch (Exception e) {
            log.debug("从Circle创建区域失败", e);
            return null;
        }
    }

    /**
     * 生成区域名称
     */
    private void generateAreaNames(List<RecognizedArea> areas) {
        int unnamed = 1;
        for (RecognizedArea area : areas) {
            if (area.getName() == null || area.getName().isEmpty()) {
                area.setName("区域 " + unnamed++);
            }
        }
    }

    /**
     * 计算边界框
     */
    private BoundingBox calculateBounds(List<Point> points) {
        BoundingBox bounds = new BoundingBox();
        
        BigDecimal minX = points.get(0).getX();
        BigDecimal minY = points.get(0).getY();
        BigDecimal maxX = points.get(0).getX();
        BigDecimal maxY = points.get(0).getY();

        for (Point p : points) {
            if (p.getX().compareTo(minX) < 0) minX = p.getX();
            if (p.getX().compareTo(maxX) > 0) maxX = p.getX();
            if (p.getY().compareTo(minY) < 0) minY = p.getY();
            if (p.getY().compareTo(maxY) > 0) maxY = p.getY();
        }

        bounds.setMinX(minX);
        bounds.setMinY(minY);
        bounds.setMaxX(maxX);
        bounds.setMaxY(maxY);

        return bounds;
    }
}

