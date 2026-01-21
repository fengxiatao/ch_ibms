package cn.iocoder.yudao.module.iot.controller.admin.gis.vo.cad;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * CAD平面图完整数据结构
 * 
 * 参考ChatGPT建议的JSON结构
 * 支持完整的CAD图元展示
 * 
 * @author AI Assistant
 */
@Data
public class CADFloorPlanVO {
    
    /**
     * 楼层ID
     */
    private Long floorId;
    
    /**
     * 楼层名称
     */
    private String floorName;
    
    /**
     * 所有图层数据
     */
    private List<LayerData> layers;
    
    /**
     * 边界范围（用于前端缩放）
     */
    private BoundsData bounds;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 图层数据
     */
    @Data
    public static class LayerData {
        /**
         * 图层名称
         */
        private String name;
        
        /**
         * 图层类型（room/corridor/wall/door/text/device）
         */
        private String type;
        
        /**
         * 是否默认显示
         */
        private Boolean visible;
        
        /**
         * 图层颜色
         */
        private String color;
        
        /**
         * 图元对象列表
         */
        private List<GeometryObject> objects;
    }
    
    /**
     * 几何对象
     */
    @Data
    public static class GeometryObject {
        /**
         * 对象ID
         */
        private String id;
        
        /**
         * 对象名称
         */
        private String name;
        
        /**
         * 几何类型（polygon/line/circle/text/block）
         */
        private String geometryType;
        
        /**
         * 坐标点数组
         */
        private List<double[]> points;
        
        /**
         * 圆形数据（如果是圆）
         */
        private CircleData circle;
        
        /**
         * 文字数据（如果是文字）
         */
        private TextData text;
        
        /**
         * 块引用数据（如果是设备符号）
         */
        private BlockData block;
        
        /**
         * 扩展属性
         */
        private Map<String, Object> properties;
    }
    
    /**
     * 圆形数据
     */
    @Data
    public static class CircleData {
        private double[] center;  // [x, y]
        private double radius;
    }
    
    /**
     * 文字数据
     */
    @Data
    public static class TextData {
        private String content;
        private double[] position;  // [x, y]
        private double height;
        private double rotation;
    }
    
    /**
     * 块引用数据（设备符号）
     */
    @Data
    public static class BlockData {
        private String blockName;
        private double[] position;  // [x, y]
        private double rotation;
        private double scale;
    }
    
    /**
     * 边界数据
     */
    @Data
    public static class BoundsData {
        private double minX;
        private double minY;
        private double maxX;
        private double maxY;
        
        public double getWidth() {
            return maxX - minX;
        }
        
        public double getHeight() {
            return maxY - minY;
        }
    }
}











