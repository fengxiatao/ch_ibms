package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.FloorMapper;
import cn.iocoder.yudao.module.iot.service.spatial.DxfDeviceRecognizer;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备坐标同步服务
 * 从DXF文件中提取设备坐标，并同步到 iot_device 表
 *
 * @author IBMS Team
 */
@Service
@Slf4j
public class DeviceCoordinateSyncService {

    @Resource
    private FloorMapper floorMapper;

    @Resource
    private IotDeviceMapper deviceMapper;

    @Resource
    private DxfDeviceRecognizer deviceRecognizer;

    /**
     * 同步结果
     */
    @Data
    public static class SyncResult {
        /** DXF中识别到的设备总数 */
        private int totalInDxf;
        
        /** 按设备类型统计 */
        private Map<String, Integer> deviceTypeStats = new HashMap<>();
        
        /** 成功匹配的设备数 */
        private int matched;
        
        /** 成功更新坐标的设备数 */
        private int updated;
        
        /** 更新失败的设备数 */
        private int failed;
        
        /** 未匹配的设备列表（DXF中有，但数据库中找不到） */
        private List<UnmatchedDevice> unmatchedDevices = new ArrayList<>();
        
        /** 详细日志 */
        private List<String> logs = new ArrayList<>();
    }

    /**
     * 未匹配的设备
     */
    @Data
    public static class UnmatchedDevice {
        private String name;
        private String deviceType;
        private String layerName;
        private String blockName;
        private BigDecimal x;
        private BigDecimal y;
        private BigDecimal z;
    }

    /**
     * 从楼层DXF中提取摄像头坐标并同步到设备表
     *
     * @param floorId 楼层ID
     * @return 同步结果
     */
    @Transactional
    public SyncResult syncCameraCoordinates(Long floorId) {
        return syncDeviceCoordinates(floorId, "camera");
    }

    /**
     * 从楼层DXF中提取所有设备坐标并同步到设备表
     *
     * @param floorId 楼层ID
     * @return 同步结果
     */
    @Transactional
    public SyncResult syncAllDeviceCoordinates(Long floorId) {
        return syncDeviceCoordinates(floorId, null);
    }

    /**
     * 从楼层DXF中提取设备坐标并同步到设备表（核心方法）
     *
     * @param floorId 楼层ID
     * @param deviceTypeFilter 设备类型过滤（null表示所有类型）
     * @return 同步结果
     */
    @Transactional
    public SyncResult syncDeviceCoordinates(Long floorId, String deviceTypeFilter) {
        SyncResult result = new SyncResult();
        
        log.info("【设备坐标同步】开始，楼层ID: {}, 设备类型过滤: {}", floorId, deviceTypeFilter);
        result.getLogs().add("开始同步楼层 " + floorId + " 的设备坐标");

        // 1. 获取楼层信息
        FloorDO floor = floorMapper.selectById(floorId);
        if (floor == null) {
            result.getLogs().add("❌ 错误：楼层不存在");
            log.error("【设备坐标同步】楼层不存在，ID: {}", floorId);
            return result;
        }

        if (floor.getDxfFilePath() == null || floor.getDxfFilePath().isEmpty()) {
            result.getLogs().add("❌ 错误：楼层尚未上传DXF文件");
            log.warn("【设备坐标同步】楼层尚未上传DXF文件，ID: {}", floorId);
            return result;
        }

        result.getLogs().add("✅ 找到楼层: " + floor.getName());
        result.getLogs().add("✅ DXF文件: " + floor.getDxfFileName());

        // 2. 识别DXF中的设备
        List<DxfDeviceRecognizer.RecognizedDevice> dxfDevices;
        try {
            FileInputStream fis = new FileInputStream(floor.getDxfFilePath());
            dxfDevices = deviceRecognizer.recognizeDevices(fis);
            fis.close();
            
            result.setTotalInDxf(dxfDevices.size());
            result.getLogs().add("✅ DXF识别完成，共找到 " + dxfDevices.size() + " 个设备");
            
            // 统计设备类型
            Map<String, Integer> typeStats = deviceRecognizer.getDeviceStatistics(dxfDevices);
            result.setDeviceTypeStats(typeStats);
            
            for (Map.Entry<String, Integer> entry : typeStats.entrySet()) {
                result.getLogs().add("   - " + getDeviceTypeName(entry.getKey()) + ": " + entry.getValue() + " 个");
            }
            
        } catch (Exception e) {
            result.getLogs().add("❌ 错误：DXF识别失败 - " + e.getMessage());
            log.error("【设备坐标同步】DXF识别失败", e);
            return result;
        }

        // 3. 过滤设备类型
        if (deviceTypeFilter != null && !deviceTypeFilter.isEmpty()) {
            int beforeFilter = dxfDevices.size();
            dxfDevices = dxfDevices.stream()
                .filter(d -> deviceTypeFilter.equals(d.getDeviceType()))
                .collect(java.util.stream.Collectors.toList());
            
            result.getLogs().add("✅ 类型过滤: " + beforeFilter + " → " + dxfDevices.size() + " (" + getDeviceTypeName(deviceTypeFilter) + ")");
        }

        // 4. 匹配并更新设备坐标
        result.getLogs().add("开始匹配数据库中的设备...");
        
        for (DxfDeviceRecognizer.RecognizedDevice dxfDevice : dxfDevices) {
            // 尝试多种方式匹配设备
            IotDeviceDO device = findMatchingDevice(floorId, dxfDevice);
            
            if (device != null) {
                result.setMatched(result.getMatched() + 1);
                
                try {
                    // 更新坐标
                    device.setLocalX(dxfDevice.getX());
                    device.setLocalY(dxfDevice.getY());
                    device.setLocalZ(dxfDevice.getZ());
                    device.setFloorId(floorId);
                    
                    deviceMapper.updateById(device);
                    result.setUpdated(result.getUpdated() + 1);
                    
                    result.getLogs().add(String.format("✅ 更新设备 [%s]: (%.2fm, %.2fm, %.2fm)",
                        device.getDeviceName(),
                        dxfDevice.getX(),
                        dxfDevice.getY(),
                        dxfDevice.getZ()
                    ));
                    
                } catch (Exception e) {
                    result.setFailed(result.getFailed() + 1);
                    result.getLogs().add("❌ 更新失败 [" + device.getDeviceName() + "]: " + e.getMessage());
                    log.error("【设备坐标同步】更新设备坐标失败: {}", device.getId(), e);
                }
            } else {
                // 未匹配的设备
                UnmatchedDevice unmatchedDevice = new UnmatchedDevice();
                unmatchedDevice.setName(dxfDevice.getName());
                unmatchedDevice.setDeviceType(dxfDevice.getDeviceType());
                unmatchedDevice.setLayerName(dxfDevice.getLayerName());
                unmatchedDevice.setBlockName(dxfDevice.getBlockName());
                unmatchedDevice.setX(dxfDevice.getX());
                unmatchedDevice.setY(dxfDevice.getY());
                unmatchedDevice.setZ(dxfDevice.getZ());
                
                result.getUnmatchedDevices().add(unmatchedDevice);
                
                result.getLogs().add(String.format("⚠️  未匹配设备 [%s] (%s图层, %s块): (%.2fm, %.2fm)",
                    dxfDevice.getName(),
                    dxfDevice.getLayerName(),
                    dxfDevice.getBlockName(),
                    dxfDevice.getX(),
                    dxfDevice.getY()
                ));
            }
        }

        // 5. 汇总结果
        result.getLogs().add("=============== 同步完成 ===============");
        result.getLogs().add("DXF中设备总数: " + result.getTotalInDxf());
        result.getLogs().add("成功匹配: " + result.getMatched());
        result.getLogs().add("成功更新: " + result.getUpdated());
        result.getLogs().add("更新失败: " + result.getFailed());
        result.getLogs().add("未匹配: " + result.getUnmatchedDevices().size());

        log.info("【设备坐标同步】完成，楼层ID: {}, 识别 {} 个，匹配 {} 个，更新 {} 个，失败 {} 个，未匹配 {} 个",
            floorId,
            result.getTotalInDxf(),
            result.getMatched(),
            result.getUpdated(),
            result.getFailed(),
            result.getUnmatchedDevices().size()
        );

        return result;
    }

    /**
     * 查找匹配的设备
     * 匹配策略：
     * 1. 优先按设备序列号匹配（device.serialNumber == dxfDevice.blockName）
     * 2. 按设备Key匹配（device.deviceKey 包含 dxfDevice.blockName）
     * 3. 按设备名称匹配（device.deviceName 包含 dxfDevice.name）
     * 4. 最后按设备类型+图层匹配
     */
    private IotDeviceDO findMatchingDevice(Long floorId, DxfDeviceRecognizer.RecognizedDevice dxfDevice) {
        // 策略1：按序列号匹配
        if (dxfDevice.getCode() != null && !dxfDevice.getCode().isEmpty()) {
            IotDeviceDO device = deviceMapper.selectOne(
                new LambdaQueryWrapper<IotDeviceDO>()
                    .eq(IotDeviceDO::getFloorId, floorId)
                    .eq(IotDeviceDO::getSerialNumber, dxfDevice.getCode())
            );
            if (device != null) {
                log.debug("【匹配】按序列号匹配成功: {}", dxfDevice.getCode());
                return device;
            }
        }

        // 策略2：按块名称匹配deviceKey或serialNumber（精确）
        if (dxfDevice.getBlockName() != null && !dxfDevice.getBlockName().isEmpty()) {
            IotDeviceDO device = deviceMapper.selectOne(
                new LambdaQueryWrapper<IotDeviceDO>()
                    .eq(IotDeviceDO::getFloorId, floorId)
                    .and(wrapper -> wrapper
                        .eq(IotDeviceDO::getSerialNumber, dxfDevice.getBlockName())
                        .or()
                        .like(IotDeviceDO::getDeviceKey, dxfDevice.getBlockName())
                    )
            );
            if (device != null) {
                log.debug("【匹配】按块名称匹配成功: {}", dxfDevice.getBlockName());
                return device;
            }
        }

        // 策略3：按设备名称模糊匹配
        if (dxfDevice.getName() != null && !dxfDevice.getName().isEmpty()) {
            // 去除数字后缀，例如 "摄像头 1" → "摄像头"
            String namePrefix = dxfDevice.getName().replaceAll("\\s*\\d+$", "").trim();
            
            List<IotDeviceDO> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<IotDeviceDO>()
                    .eq(IotDeviceDO::getFloorId, floorId)
                    .like(IotDeviceDO::getDeviceName, namePrefix)
                    .isNull(IotDeviceDO::getLocalX) // 优先匹配未设置坐标的设备
            );
            
            if (!devices.isEmpty()) {
                log.debug("【匹配】按名称前缀匹配成功: {} (找到 {} 个)", namePrefix, devices.size());
                return devices.get(0); // 返回第一个
            }
        }

        // 策略4：按图层名称匹配（监控系统 → 摄像头）
        if ("监控系统".equals(dxfDevice.getLayerName()) || "camera".equals(dxfDevice.getDeviceType())) {
            List<IotDeviceDO> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<IotDeviceDO>()
                    .eq(IotDeviceDO::getFloorId, floorId)
                    .like(IotDeviceDO::getDeviceName, "摄像头")
                    .isNull(IotDeviceDO::getLocalX) // 优先匹配未设置坐标的设备
            );
            
            if (!devices.isEmpty()) {
                log.debug("【匹配】按图层名称匹配成功: 监控系统 → 摄像头 (找到 {} 个)", devices.size());
                return devices.get(0);
            }
        }

        log.debug("【匹配】未找到匹配设备: {} (图层: {}, 块: {})", 
            dxfDevice.getName(), 
            dxfDevice.getLayerName(), 
            dxfDevice.getBlockName()
        );
        
        return null;
    }

    /**
     * 获取设备类型中文名称
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
}

