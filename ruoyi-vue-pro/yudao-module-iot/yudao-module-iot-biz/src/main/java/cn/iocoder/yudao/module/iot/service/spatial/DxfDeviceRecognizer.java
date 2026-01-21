package cn.iocoder.yudao.module.iot.service.spatial;

import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.DeviceRecognitionConfigVO;
import com.aspose.cad.Image;
import com.aspose.cad.fileformats.cad.CadImage;
import com.aspose.cad.fileformats.cad.cadobjects.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DXF设备符号识别器（兼容版）
 * 从DXF文件中识别设备符号（块引用、点等），如摄像头、传感器等
 *
 * @author IBMS Team
 */
@Service
@Slf4j
public class DxfDeviceRecognizer {

    /**
     * 识别的设备信息
     */
    @Data
    public static class RecognizedDevice {
        private String name;
        private String code;
        private String deviceType;
        private String blockName;
        private String layerName;
        private BigDecimal x;
        private BigDecimal y;
        private BigDecimal z;
        private BigDecimal rotation;
        private Boolean selected = false;
        private String areaCode;
        private String roomName;  // 所属区域名称
        private String roomCode;  // 所属区域编码
        private BigDecimal scaleX; // X缩放比例
    }

    /**
     * 设备类型映射表
     */
    private static final Map<String, String> DEVICE_TYPE_MAP = new HashMap<>();
    
    static {
        DEVICE_TYPE_MAP.put("camera", "camera");
        DEVICE_TYPE_MAP.put("摄像头", "camera");
        DEVICE_TYPE_MAP.put("监控", "camera");
        DEVICE_TYPE_MAP.put("cctv", "camera");
        DEVICE_TYPE_MAP.put("ipc", "camera");
        DEVICE_TYPE_MAP.put("sensor", "sensor");
        DEVICE_TYPE_MAP.put("传感器", "sensor");
        DEVICE_TYPE_MAP.put("探测器", "sensor");
        DEVICE_TYPE_MAP.put("door", "access_control");
        DEVICE_TYPE_MAP.put("门禁", "access_control");
        DEVICE_TYPE_MAP.put("light", "light");
        DEVICE_TYPE_MAP.put("灯", "light");
        DEVICE_TYPE_MAP.put("ac", "air_conditioner");
        DEVICE_TYPE_MAP.put("空调", "air_conditioner");
        DEVICE_TYPE_MAP.put("smoke", "smoke_detector");
        DEVICE_TYPE_MAP.put("烟感", "smoke_detector");
    }

    /**
     * 从DXF文件识别设备符号
     */
    public List<RecognizedDevice> recognizeDevices(InputStream inputStream) {
        List<RecognizedDevice> devices = new ArrayList<>();

        try {
            log.info("【Aspose.CAD】开始识别DXF设备符号");

            Image image = Image.load(inputStream);
            if (!(image instanceof CadImage)) {
                throw new IllegalArgumentException("文件不是有效的CAD/DXF格式");
            }

            CadImage cadImage = (CadImage) image;
            
            // 识别各类实体
            recognizeEntities(cadImage, devices);

            // 生成设备名称
            generateDeviceNames(devices);

            log.info("【Aspose.CAD】设备识别完成，共找到 {} 个设备", devices.size());

            cadImage.dispose();
            return devices;

        } catch (Exception e) {
            log.error("【Aspose.CAD】识别DXF设备失败", e);
            throw new RuntimeException("识别设备失败: " + e.getMessage(), e);
        }
    }

    /**
     * 识别所有实体（使用反射安全调用）
     */
    private void recognizeEntities(CadImage cadImage, List<RecognizedDevice> devices) {
        try {
            Object[] entities = cadImage.getEntities();
            int deviceIndex = 1;

            for (Object entity : entities) {
                if (entity == null) continue;
                
                try {
                    String className = entity.getClass().getSimpleName();
                    
                    // 识别块引用（Insert）
                    if (className.contains("Insert")) {
                        RecognizedDevice device = createDeviceFromBlock(entity, deviceIndex++);
                        if (device != null) {
                            devices.add(device);
                        }
                    }
                    // 识别点实体（Point）
                    else if (entity instanceof CadPoint) {
                        CadPoint point = (CadPoint) entity;
                        RecognizedDevice device = createDeviceFromPoint(point, deviceIndex++);
                        if (device != null) {
                            devices.add(device);
                        }
                    }
                    // 识别圆形（可能是设备符号）
                    else if (entity instanceof CadCircle) {
                        CadCircle circle = (CadCircle) entity;
                        // 只识别小圆（半径<500mm），大圆可能是房间
                        if (circle.getRadius() < 500) {
                            RecognizedDevice device = createDeviceFromCircle(circle, deviceIndex++);
                            if (device != null) {
                                devices.add(device);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("【Aspose.CAD】跳过实体: {}", entity.getClass().getSimpleName());
                }
            }

        } catch (Exception e) {
            log.error("【Aspose.CAD】识别实体失败", e);
        }
    }

    /**
     * 从块引用创建设备（使用反射）
     */
    private RecognizedDevice createDeviceFromBlock(Object entity, int index) {
        try {
            RecognizedDevice device = new RecognizedDevice();
            
            // 使用反射获取块名称
            Method getNameMethod = entity.getClass().getMethod("getName");
            String blockName = (String) getNameMethod.invoke(entity);
            
            if (blockName == null || blockName.isEmpty()) {
                return null;
            }
            
            device.setBlockName(blockName);
            
            // 使用反射获取图层名称
            Method getLayerNameMethod = entity.getClass().getMethod("getLayerName");
            String layerName = (String) getLayerNameMethod.invoke(entity);
            device.setLayerName(layerName);
            device.setDeviceType(guessDeviceType(blockName, layerName));

            // 使用反射获取插入点
            Method getInsertionPointMethod = entity.getClass().getMethod("getInsertionPoint");
            Object insertionPoint = getInsertionPointMethod.invoke(entity);
            
            if (insertionPoint == null) {
                return null;
            }

            // 获取坐标
            Method getXMethod = insertionPoint.getClass().getMethod("getX");
            Method getYMethod = insertionPoint.getClass().getMethod("getY");
            Method getZMethod = insertionPoint.getClass().getMethod("getZ");
            
            double x = (Double) getXMethod.invoke(insertionPoint);
            double y = (Double) getYMethod.invoke(insertionPoint);
            double z = (Double) getZMethod.invoke(insertionPoint);
            
            device.setX(BigDecimal.valueOf(x * 0.001).setScale(2, RoundingMode.HALF_UP));
            device.setY(BigDecimal.valueOf(y * 0.001).setScale(2, RoundingMode.HALF_UP));
            device.setZ(BigDecimal.valueOf(z * 0.001).setScale(2, RoundingMode.HALF_UP));

            // 尝试获取旋转角度
            try {
                Method getRotationMethod = entity.getClass().getMethod("getRotation");
                Double rotation = (Double) getRotationMethod.invoke(entity);
                if (rotation != null) {
                    device.setRotation(BigDecimal.valueOf(rotation).setScale(2, RoundingMode.HALF_UP));
                }
            } catch (Exception e) {
                device.setRotation(BigDecimal.ZERO);
            }

            device.setCode(device.getDeviceType().toUpperCase() + "_" + String.format("%03d", index));

            return device;

        } catch (Exception e) {
            log.debug("从块引用创建设备失败", e);
            return null;
        }
    }

    /**
     * 从点实体创建设备
     */
    private RecognizedDevice createDeviceFromPoint(CadPoint point, int index) {
        try {
            RecognizedDevice device = new RecognizedDevice();
            device.setBlockName("POINT");
            device.setLayerName(point.getLayerName());
            device.setDeviceType(guessDeviceType("point", point.getLayerName()));

            // 使用反射获取位置
            try {
                Method getLocationMethod = point.getClass().getMethod("getLocation");
                Object location = getLocationMethod.invoke(point);
                
                if (location != null) {
                    Method getXMethod = location.getClass().getMethod("getX");
                    Method getYMethod = location.getClass().getMethod("getY");
                    Method getZMethod = location.getClass().getMethod("getZ");
                    
                    double x = (Double) getXMethod.invoke(location);
                    double y = (Double) getYMethod.invoke(location);
                    double z = (Double) getZMethod.invoke(location);
                    
                    device.setX(BigDecimal.valueOf(x * 0.001).setScale(2, RoundingMode.HALF_UP));
                    device.setY(BigDecimal.valueOf(y * 0.001).setScale(2, RoundingMode.HALF_UP));
                    device.setZ(BigDecimal.valueOf(z * 0.001).setScale(2, RoundingMode.HALF_UP));
                } else {
                    return null;
                }
            } catch (Exception e) {
                log.debug("无法获取点位置", e);
                return null;
            }

            device.setRotation(BigDecimal.ZERO);
            device.setCode("POINT_" + String.format("%03d", index));

            return device;

        } catch (Exception e) {
            log.debug("从点实体创建设备失败", e);
            return null;
        }
    }

    /**
     * 从圆形创建设备
     */
    private RecognizedDevice createDeviceFromCircle(CadCircle circle, int index) {
        try {
            RecognizedDevice device = new RecognizedDevice();
            device.setBlockName("CIRCLE");
            device.setLayerName(circle.getLayerName());
            device.setDeviceType(guessDeviceType("circle", circle.getLayerName()));

            Cad3DPoint center = circle.getCenterPoint();
            if (center != null) {
                device.setX(BigDecimal.valueOf(center.getX() * 0.001).setScale(2, RoundingMode.HALF_UP));
                device.setY(BigDecimal.valueOf(center.getY() * 0.001).setScale(2, RoundingMode.HALF_UP));
                device.setZ(BigDecimal.valueOf(center.getZ() * 0.001).setScale(2, RoundingMode.HALF_UP));
            } else {
                return null;
            }

            device.setRotation(BigDecimal.ZERO);
            device.setCode("CIRCLE_" + String.format("%03d", index));

            return device;

        } catch (Exception e) {
            log.debug("从圆形创建设备失败", e);
            return null;
        }
    }

    /**
     * 根据块名称和图层名推测设备类型
     */
    private String guessDeviceType(String blockName, String layerName) {
        String combined = (blockName + " " + layerName).toLowerCase();

        for (Map.Entry<String, String> entry : DEVICE_TYPE_MAP.entrySet()) {
            if (combined.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "unknown";
    }

    /**
     * 生成设备名称
     */
    private void generateDeviceNames(List<RecognizedDevice> devices) {
        Map<String, Integer> typeCounter = new HashMap<>();

        for (RecognizedDevice device : devices) {
            String type = device.getDeviceType();
            int count = typeCounter.getOrDefault(type, 0) + 1;
            typeCounter.put(type, count);

            String typeName = getDeviceTypeName(type);
            device.setName(typeName + " " + count);
        }
    }

    /**
     * 获取设备类型的中文名称
     */
    private String getDeviceTypeName(String deviceType) {
        switch (deviceType) {
            case "camera": return "摄像头";
            case "sensor": return "传感器";
            case "access_control": return "门禁";
            case "light": return "灯光";
            case "air_conditioner": return "空调";
            case "smoke_detector": return "烟感";
            default: return "设备";
        }
    }

    /**
     * 获取设备统计
     */
    public Map<String, Integer> getDeviceStatistics(List<RecognizedDevice> devices) {
        Map<String, Integer> stats = new HashMap<>();
        
        for (RecognizedDevice device : devices) {
            String type = device.getDeviceType();
            stats.put(type, stats.getOrDefault(type, 0) + 1);
        }
        
        return stats;
    }

    /**
     * 将设备匹配到所属区域
     * 通过判断设备坐标是否在区域边界内来确定设备所属区域
     */
    public void matchDevicesToAreas(List<RecognizedDevice> devices, List<DxfAreaRecognizer.RecognizedArea> areas) {
        for (RecognizedDevice device : devices) {
            if (device.getX() == null || device.getY() == null) {
                continue;
            }

            double deviceX = device.getX().doubleValue();
            double deviceY = device.getY().doubleValue();

            // 寻找包含此设备的区域
            for (DxfAreaRecognizer.RecognizedArea area : areas) {
                if (isPointInArea(deviceX, deviceY, area)) {
                    device.setRoomName(area.getName());
                    device.setRoomCode(area.getCode());
                    break;
                }
            }
        }
    }

    /**
     * 判断点是否在区域内（简化版本，使用边界框判断）
     */
    private boolean isPointInArea(double x, double y, DxfAreaRecognizer.RecognizedArea area) {
        if (area.getBounds() == null) {
            return false;
        }

        DxfAreaRecognizer.BoundingBox bounds = area.getBounds();
        if (bounds.getMinX() == null || bounds.getMinY() == null || 
            bounds.getMaxX() == null || bounds.getMaxY() == null) {
            return false;
        }

        double minX = bounds.getMinX().doubleValue();
        double minY = bounds.getMinY().doubleValue();
        double maxX = bounds.getMaxX().doubleValue();
        double maxY = bounds.getMaxY().doubleValue();

        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    /**
     * 使用配置识别设备（带过滤）
     * 
     * @param inputStream DXF文件流
     * @param configList 设备识别配置列表
     * @return 识别到的设备列表（仅包含匹配配置的设备）
     */
    public List<RecognizedDevice> recognizeDevicesWithConfig(InputStream inputStream, List<DeviceRecognitionConfigVO> configList) {
        // 先识别所有设备
        List<RecognizedDevice> allDevices = recognizeDevices(inputStream);
        
        // 如果没有配置，返回所有设备
        if (configList == null || configList.isEmpty()) {
            return allDevices;
        }

        // 过滤并映射设备
        List<RecognizedDevice> filteredDevices = new ArrayList<>();
        
        for (RecognizedDevice device : allDevices) {
            // 检查设备是否匹配任何配置规则
            for (DeviceRecognitionConfigVO config : configList) {
                if (!config.getEnabled()) {
                    continue;
                }
                
                if (matchDeviceConfig(device, config)) {
                    // 根据配置设置设备类型
                    device.setDeviceType(config.getDeviceType());
                    filteredDevices.add(device);
                    break; // 匹配到一个规则就跳出
                }
            }
        }
        
        log.info("【Aspose.CAD】配置过滤后，保留 {} 个设备（原始 {} 个）", 
                filteredDevices.size(), allDevices.size());
        
        return filteredDevices;
    }

    /**
     * 检查设备是否匹配配置规则
     * 
     * @param device 设备
     * @param config 配置规则
     * @return 是否匹配
     */
    private boolean matchDeviceConfig(RecognizedDevice device, DeviceRecognitionConfigVO config) {
        String namePattern = config.getNamePattern();
        if (namePattern == null || namePattern.trim().isEmpty()) {
            return false;
        }

        // 分割多个匹配词（逗号分隔）
        String[] patterns = namePattern.split(",");
        
        for (String pattern : patterns) {
            pattern = pattern.trim().toLowerCase();
            if (pattern.isEmpty()) {
                continue;
            }

            // 根据匹配类型检查
            String matchType = config.getMatchType();
            
            if ("block".equals(matchType)) {
                // 只匹配块名称
                if (device.getBlockName() != null && 
                    device.getBlockName().toLowerCase().contains(pattern)) {
                    return true;
                }
            } else if ("layer".equals(matchType)) {
                // 只匹配图层名称
                if (device.getLayerName() != null && 
                    device.getLayerName().toLowerCase().contains(pattern)) {
                    return true;
                }
            } else if ("both".equals(matchType)) {
                // 匹配块名称或图层名称
                boolean blockMatch = device.getBlockName() != null && 
                        device.getBlockName().toLowerCase().contains(pattern);
                boolean layerMatch = device.getLayerName() != null && 
                        device.getLayerName().toLowerCase().contains(pattern);
                
                if (blockMatch || layerMatch) {
                    return true;
                }
            }
        }
        
        return false;
    }
}

