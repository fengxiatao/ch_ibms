package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.*;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.IotGisMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * IoT GIS 服务实现
 *
 * @author IBMS Team
 */
@Service
@Validated
@Slf4j
public class IotGisServiceImpl implements IotGisService {

    @Autowired
    private IotGisMapper gisMapper;

    @Override
    public IotGisStatisticsRespVO getStatistics() {
        IotGisStatisticsRespVO statistics = new IotGisStatisticsRespVO();
        
        // 获取各类型统计数据
        statistics.setCampusCount(gisMapper.countCampus());
        statistics.setBuildingCount(gisMapper.countBuilding());
        statistics.setFloorCount(gisMapper.countFloor());
        statistics.setRoomCount(gisMapper.countRoom());
        
        // 设备统计 - 将 List<Map> 转换为 Map<String, Integer>
        List<Map<String, Object>> deviceStatsList = gisMapper.countDeviceByStatus();
        Map<String, Integer> deviceStats = new HashMap<>();
        int totalDevices = 0;
        
        for (Map<String, Object> row : deviceStatsList) {
            String status = (String) row.get("status");
            Object countObj = row.get("count");
            int count = countObj instanceof Integer ? (Integer) countObj : ((Number) countObj).intValue();
            deviceStats.put(status, count);
            totalDevices += count;
        }
        statistics.setDeviceCount(totalDevices);
        // 使用英文状态值（符合国际化规范，前端通过字典显示中文）
        statistics.setOnlineDeviceCount(deviceStats.getOrDefault("online", 0));
        statistics.setOfflineDeviceCount(deviceStats.getOrDefault("offline", 0));
        statistics.setAlarmDeviceCount(deviceStats.getOrDefault("fault", 0));
        return statistics;
    }

    @Override
    public IotGisLayerBoundsRespVO getLayerBounds(String layer) {
        Map<String, BigDecimal> bounds = gisMapper.getLayerBounds(layer);
        
        if (bounds == null || bounds.isEmpty()) {
            // 返回默认范围（广州）
            IotGisLayerBoundsRespVO defaultBounds = new IotGisLayerBoundsRespVO();
            defaultBounds.setMinX(new BigDecimal("113.0"));
            defaultBounds.setMinY(new BigDecimal("23.0"));
            defaultBounds.setMaxX(new BigDecimal("114.0"));
            defaultBounds.setMaxY(new BigDecimal("24.0"));
            defaultBounds.setCenterX(new BigDecimal("113.5"));
            defaultBounds.setCenterY(new BigDecimal("23.5"));
            return defaultBounds;
        }
        
        IotGisLayerBoundsRespVO result = new IotGisLayerBoundsRespVO();
        result.setMinX(bounds.get("minX"));
        result.setMinY(bounds.get("minY"));
        result.setMaxX(bounds.get("maxX"));
        result.setMaxY(bounds.get("maxY"));
        
        // 计算中心点
        BigDecimal centerX = bounds.get("minX").add(bounds.get("maxX")).divide(new BigDecimal("2"), 6, RoundingMode.HALF_UP);
        BigDecimal centerY = bounds.get("minY").add(bounds.get("maxY")).divide(new BigDecimal("2"), 6, RoundingMode.HALF_UP);
        result.setCenterX(centerX);
        result.setCenterY(centerY);
        
        return result;
    }

    @Override
    public PageResult<IotGisFeatureRespVO> searchFeatures(IotGisSearchReqVO searchReqVO) {
        // 执行空间搜索
        List<IotGisFeatureRespVO> features = gisMapper.searchFeatures(searchReqVO);
        Long total = gisMapper.searchFeaturesCount(searchReqVO);
        
        return new PageResult<>(features, total);
    }

    @Override
    public List<IotGisDeviceRespVO> getNearbyDevices(IotGisNearbyReqVO nearbyReqVO) {
        return gisMapper.getNearbyDevices(nearbyReqVO);
    }

    @Override
    public List<IotGisDeviceRespVO> getDevicesInBounds(IotGisDevicesInBoundsReqVO boundsReqVO) {
        return gisMapper.getDevicesInBounds(boundsReqVO);
    }

    @Override
    public IotGisDeviceLocationRespVO getDeviceLocation(Long deviceId) {
        return gisMapper.getDeviceLocation(deviceId);
    }

    @Override
    public void updateDeviceLocation(IotGisUpdateLocationReqVO updateReqVO) {
        gisMapper.updateDeviceLocation(updateReqVO);
    }

    @Override
    public IotGisSpatialRelationRespVO getSpatialRelation(Long deviceId) {
        return gisMapper.getSpatialRelation(deviceId);
    }

    @Override
    public List<IotGisHeatmapPointRespVO> getHeatmapData(IotGisHeatmapReqVO heatmapReqVO) {
        return gisMapper.getHeatmapData(heatmapReqVO);
    }

    @Override
    public List<IotGisClusterRespVO> getClusterData(IotGisClusterReqVO clusterReqVO) {
        // 获取所有设备
        List<IotGisDeviceRespVO> devices = gisMapper.getDevicesForCluster(clusterReqVO);
        
        // 简单的聚合算法（实际生产环境可以使用更复杂的聚合算法）
        List<IotGisClusterRespVO> clusters = performClustering(devices, clusterReqVO.getClusterDistance());
        
        return clusters;
    }

    @Override
    public IotGisDistanceRespVO measureDistance(BigDecimal lon1, BigDecimal lat1, BigDecimal lon2, BigDecimal lat2) {
        // 使用 PostGIS 的 ST_Distance_Sphere 函数计算球面距离
        BigDecimal distanceInMeters = gisMapper.calculateDistance(lon1, lat1, lon2, lat2);
        
        IotGisDistanceRespVO result = new IotGisDistanceRespVO();
        result.setDistanceInMeters(distanceInMeters.setScale(2, RoundingMode.HALF_UP));
        result.setDistanceInKilometers(distanceInMeters.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP));
        result.setStartLongitude(lon1);
        result.setStartLatitude(lat1);
        result.setEndLongitude(lon2);
        result.setEndLatitude(lat2);
        
        return result;
    }

    @Override
    public PageResult<IotGisFeatureRespVO> getLayerFeatures(IotGisLayerFeaturesReqVO reqVO) {
        List<IotGisFeatureRespVO> features = gisMapper.getLayerFeatures(reqVO);
        Long total = gisMapper.getLayerFeaturesCount(reqVO);
        
        return new PageResult<>(features, total);
    }

    /**
     * 执行设备聚合
     * 
     * @param devices 设备列表
     * @param clusterDistance 聚合距离（像素）
     * @return 聚合结果列表
     */
    private List<IotGisClusterRespVO> performClustering(List<IotGisDeviceRespVO> devices, Integer clusterDistance) {
        List<IotGisClusterRespVO> clusters = new ArrayList<>();
        
        if (devices == null || devices.isEmpty()) {
            return clusters;
        }
        
        // 简单的聚合算法：如果设备数量较少，直接返回单个设备
        if (devices.size() <= 100) {
            for (IotGisDeviceRespVO device : devices) {
                IotGisClusterRespVO cluster = new IotGisClusterRespVO();
                cluster.setLongitude(device.getLongitude());
                cluster.setLatitude(device.getLatitude());
                cluster.setCount(1);
                cluster.setIsCluster(false);
                cluster.setDeviceIds(Collections.singletonList(device.getId()));
                // 使用英文状态值（符合国际化规范）
                cluster.setOnlineCount("online".equals(device.getStatus()) ? 1 : 0);
                cluster.setOfflineCount("offline".equals(device.getStatus()) ? 1 : 0);
                cluster.setAlarmCount("fault".equals(device.getStatus()) ? 1 : 0);
                clusters.add(cluster);
            }
        } else {
            // 对于大量设备，使用网格聚合
            Map<String, List<IotGisDeviceRespVO>> grid = new HashMap<>();
            BigDecimal gridSize = new BigDecimal("0.01"); // 约 1km
            
            for (IotGisDeviceRespVO device : devices) {
                String gridKey = getGridKey(device.getLongitude(), device.getLatitude(), gridSize);
                grid.computeIfAbsent(gridKey, k -> new ArrayList<>()).add(device);
            }
            
            // 为每个网格创建聚合
            for (List<IotGisDeviceRespVO> gridDevices : grid.values()) {
                IotGisClusterRespVO cluster = new IotGisClusterRespVO();
                
                // 计算中心点
                BigDecimal avgLon = gridDevices.stream()
                        .map(IotGisDeviceRespVO::getLongitude)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(gridDevices.size()), 6, RoundingMode.HALF_UP);
                
                BigDecimal avgLat = gridDevices.stream()
                        .map(IotGisDeviceRespVO::getLatitude)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(gridDevices.size()), 6, RoundingMode.HALF_UP);
                
                cluster.setLongitude(avgLon);
                cluster.setLatitude(avgLat);
                cluster.setCount(gridDevices.size());
                cluster.setIsCluster(gridDevices.size() > 1);
                cluster.setDeviceIds(gridDevices.stream().map(IotGisDeviceRespVO::getId).toList());
                
                // 统计状态（使用英文状态值，符合国际化规范）
                long onlineCount = gridDevices.stream().filter(d -> "online".equals(d.getStatus())).count();
                long offlineCount = gridDevices.stream().filter(d -> "offline".equals(d.getStatus())).count();
                long alarmCount = gridDevices.stream().filter(d -> "fault".equals(d.getStatus())).count();
                
                cluster.setOnlineCount((int) onlineCount);
                cluster.setOfflineCount((int) offlineCount);
                cluster.setAlarmCount((int) alarmCount);
                
                clusters.add(cluster);
            }
        }
        
        return clusters;
    }

    /**
     * 获取网格键
     */
    private String getGridKey(BigDecimal lon, BigDecimal lat, BigDecimal gridSize) {
        int gridX = lon.divide(gridSize, 0, RoundingMode.DOWN).intValue();
        int gridY = lat.divide(gridSize, 0, RoundingMode.DOWN).intValue();
        return gridX + "_" + gridY;
    }

    @Override
    public IotGisBuildingRespVO getBuildingByCode(String code) {
        log.info("根据建筑编码查询建筑信息: code={}", code);
        return gisMapper.getBuildingByCode(code);
    }
}

