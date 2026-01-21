package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.DeviceLocationDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.DeviceLocationMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.AreaMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.BuildingMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.FloorMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 设备位置信息 Service 实现类
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author IBMS Team
 */
@Service
@Validated
@Slf4j
public class DeviceLocationServiceImpl implements DeviceLocationService {

    @Resource
    private DeviceLocationMapper deviceLocationMapper;

    @Resource
    private FloorMapper floorMapper;

    @Resource
    private BuildingMapper buildingMapper;

    @Resource
    private AreaMapper areaMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDeviceLocation(DeviceLocationSaveReqVO createReqVO) {
        // 验证设备位置
        validateDeviceLocationForCreate(createReqVO);

        // 插入
        DeviceLocationDO deviceLocation = BeanUtils.toBean(createReqVO, DeviceLocationDO.class);
        
        // 如果提供了楼层ID，自动填充建筑ID
        if (deviceLocation.getFloorId() != null) {
            FloorDO floor = floorMapper.selectById(deviceLocation.getFloorId());
            if (floor != null) {
                deviceLocation.setBuildingId(floor.getBuildingId());
            }
        }

        // 计算全局坐标（如果有建筑信息）
        calculateGlobalCoordinates(deviceLocation);

        deviceLocationMapper.insert(deviceLocation);
        return deviceLocation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceLocation(DeviceLocationSaveReqVO updateReqVO) {
        // 校验存在
        validateDeviceLocationExists(updateReqVO.getId());

        // 更新
        DeviceLocationDO updateObj = BeanUtils.toBean(updateReqVO, DeviceLocationDO.class);
        
        // 更新建筑ID
        if (updateObj.getFloorId() != null) {
            FloorDO floor = floorMapper.selectById(updateObj.getFloorId());
            if (floor != null) {
                updateObj.setBuildingId(floor.getBuildingId());
            }
        }

        // 重新计算全局坐标
        calculateGlobalCoordinates(updateObj);

        deviceLocationMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeviceLocation(Long id) {
        // 校验存在
        validateDeviceLocationExists(id);
        // 删除
        deviceLocationMapper.deleteById(id);
    }

    @Override
    public DeviceLocationRespVO getDeviceLocation(Long id) {
        DeviceLocationDO deviceLocation = deviceLocationMapper.selectById(id);
        return BeanUtils.toBean(deviceLocation, DeviceLocationRespVO.class);
    }

    @Override
    public DeviceLocationRespVO getDeviceLocationByDeviceId(Long deviceId) {
        DeviceLocationDO deviceLocation = deviceLocationMapper.selectByDeviceId(deviceId);
        return BeanUtils.toBean(deviceLocation, DeviceLocationRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateDeviceLocation(DeviceLocationBatchUpdateReqVO batchUpdateReqVO) {
        for (DeviceLocationBatchUpdateReqVO.DevicePositionUpdateDTO dto : batchUpdateReqVO.getDevices()) {
            DeviceLocationDO existingLocation = deviceLocationMapper.selectByDeviceId(dto.getDeviceId());
            
            if (existingLocation != null) {
                // 更新现有位置
                existingLocation.setLocalX(dto.getLocalX());
                existingLocation.setLocalY(dto.getLocalY());
                existingLocation.setLocalZ(dto.getLocalZ());
                existingLocation.setAreaId(dto.getAreaId());
                
                // 重新计算全局坐标
                calculateGlobalCoordinates(existingLocation);
                
                deviceLocationMapper.updateById(existingLocation);
            } else {
                log.warn("设备 {} 没有位置信息，跳过更新", dto.getDeviceId());
            }
        }
    }

    @Override
    public List<DeviceWithLocationRespVO> getDevicesInFloor(Long floorId) {
        List<DeviceLocationDO> locations = deviceLocationMapper.selectListByFloorId(floorId);
        List<DeviceWithLocationRespVO> result = new ArrayList<>();
        
        for (DeviceLocationDO location : locations) {
            DeviceWithLocationRespVO vo = buildDeviceWithLocationVO(location);
            if (vo != null) {
                result.add(vo);
            }
        }
        
        return result;
    }

    @Override
    public List<DeviceWithLocationRespVO> getDevicesInArea(Long areaId) {
        List<DeviceLocationDO> locations = deviceLocationMapper.selectListByAreaId(areaId);
        List<DeviceWithLocationRespVO> result = new ArrayList<>();
        
        for (DeviceLocationDO location : locations) {
            DeviceWithLocationRespVO vo = buildDeviceWithLocationVO(location);
            if (vo != null) {
                result.add(vo);
            }
        }
        
        return result;
    }

    @Override
    public List<DeviceWithLocationRespVO> getDevicesInBuilding(Long buildingId) {
        List<DeviceLocationDO> locations = deviceLocationMapper.selectListByBuildingId(buildingId);
        List<DeviceWithLocationRespVO> result = new ArrayList<>();
        
        for (DeviceLocationDO location : locations) {
            DeviceWithLocationRespVO vo = buildDeviceWithLocationVO(location);
            if (vo != null) {
                result.add(vo);
            }
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoAssignDeviceToArea(Long deviceId) {
        DeviceLocationDO location = deviceLocationMapper.selectByDeviceId(deviceId);
        if (location == null || location.getFloorId() == null) {
            return false;
        }

        // 获取楼层的所有区域
        List<AreaDO> areas = areaMapper.selectListByFloorId(location.getFloorId());
        
        // 查找包含设备坐标的区域
        for (AreaDO area : areas) {
            if (isPointInPolygon(location.getLocalX(), location.getLocalY(), area.getLocalGeom())) {
                location.setAreaId(area.getId());
                deviceLocationMapper.updateById(location);
                log.info("设备 {} 自动分配到区域 {}", deviceId, area.getName());
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAutoAssignDeviceToArea(Long floorId) {
        List<DeviceLocationDO> locations = deviceLocationMapper.selectListByFloorId(floorId);
        List<AreaDO> areas = areaMapper.selectListByFloorId(floorId);
        
        int successCount = 0;
        
        for (DeviceLocationDO location : locations) {
            for (AreaDO area : areas) {
                if (isPointInPolygon(location.getLocalX(), location.getLocalY(), area.getLocalGeom())) {
                    location.setAreaId(area.getId());
                    deviceLocationMapper.updateById(location);
                    successCount++;
                    break;
                }
            }
        }
        
        return successCount;
    }

    @Override
    public GlobalCoordinateDTO getDeviceGlobalCoordinate(Long deviceId) {
        DeviceLocationDO location = deviceLocationMapper.selectByDeviceId(deviceId);
        if (location == null) {
            throw ServiceExceptionUtil.exception(DEVICE_LOCATION_NOT_EXISTS);
        }

        // 如果已缓存全局坐标，直接返回
        if (location.getGlobalLongitude() != null && location.getGlobalLatitude() != null) {
            return new GlobalCoordinateDTO(
                location.getGlobalLongitude(),
                location.getGlobalLatitude(),
                location.getGlobalAltitude()
            );
        }

        // 重新计算并缓存
        calculateGlobalCoordinates(location);
        deviceLocationMapper.updateById(location);

        return new GlobalCoordinateDTO(
            location.getGlobalLongitude(),
            location.getGlobalLatitude(),
            location.getGlobalAltitude()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveDevice(Long deviceId, Long targetFloorId, Long targetAreaId,
                           BigDecimal localX, BigDecimal localY, BigDecimal localZ) {
        DeviceLocationDO location = deviceLocationMapper.selectByDeviceId(deviceId);
        if (location == null) {
            throw ServiceExceptionUtil.exception(DEVICE_LOCATION_NOT_EXISTS);
        }

        // 更新位置信息
        if (targetFloorId != null) {
            location.setFloorId(targetFloorId);
            
            // 更新建筑ID
            FloorDO floor = floorMapper.selectById(targetFloorId);
            if (floor != null) {
                location.setBuildingId(floor.getBuildingId());
            }
        }
        
        location.setAreaId(targetAreaId);
        location.setLocalX(localX);
        location.setLocalY(localY);
        if (localZ != null) {
            location.setLocalZ(localZ);
        }

        // 重新计算全局坐标
        calculateGlobalCoordinates(location);

        deviceLocationMapper.updateById(location);
    }

    @Override
    public ValidationResultDTO validateDeviceLocation(Long floorId, BigDecimal localX, BigDecimal localY) {
        FloorDO floor = floorMapper.selectById(floorId);
        if (floor == null) {
            return new ValidationResultDTO(false, "楼层不存在");
        }

        // 检查是否在楼层范围内
        String floorGeom = floor.getGeom();
        if (floorGeom != null && !isPointInPolygon(localX, localY, floorGeom)) {
            return new ValidationResultDTO(false, "设备坐标不在楼层范围内");
        }

        // 查找建议的区域
        List<AreaDO> areas = areaMapper.selectListByFloorId(floorId);
        for (AreaDO area : areas) {
            if (isPointInPolygon(localX, localY, area.getLocalGeom())) {
                ValidationResultDTO result = new ValidationResultDTO(true, "位置有效");
                result.setSuggestedAreaId(area.getId());
                result.setSuggestedAreaName(area.getName());
                return result;
            }
        }

        return new ValidationResultDTO(true, "位置有效，但不在任何区域内");
    }

    // ==================== 私有方法 ====================

    /**
     * 验证设备位置是否存在
     */
    private void validateDeviceLocationExists(Long id) {
        if (deviceLocationMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(DEVICE_LOCATION_NOT_EXISTS);
        }
    }

    /**
     * 验证设备位置创建参数
     */
    private void validateDeviceLocationForCreate(DeviceLocationSaveReqVO createReqVO) {
        // 检查设备ID是否已存在位置信息
        DeviceLocationDO existing = deviceLocationMapper.selectByDeviceId(createReqVO.getDeviceId());
        if (existing != null) {
            throw ServiceExceptionUtil.exception(DEVICE_LOCATION_ALREADY_EXISTS);
        }
    }

    /**
     * 计算全局坐标（经纬度）
     */
    private void calculateGlobalCoordinates(DeviceLocationDO location) {
        if (location.getBuildingId() == null) {
            return;
        }

        BuildingDO building = buildingMapper.selectById(location.getBuildingId());
        if (building == null || building.getGeom() == null) {
            return;
        }

        // TODO: 实现坐标转换逻辑
        // 这里需要根据建筑的方位角、经纬度等信息进行坐标转换
        // 暂时使用简单的偏移算法
        
        try {
            // 解析建筑中心点坐标
            String geom = building.getGeom();
            Pattern pattern = Pattern.compile("POINT\\(([\\d.]+)\\s+([\\d.]+)\\)");
            Matcher matcher = pattern.matcher(geom);
            
            if (matcher.find()) {
                BigDecimal buildingLon = new BigDecimal(matcher.group(1));
                BigDecimal buildingLat = new BigDecimal(matcher.group(2));
                
                // 简单的坐标转换（1米 ≈ 0.00001度）
                BigDecimal lonOffset = location.getLocalX().multiply(new BigDecimal("0.00001"));
                BigDecimal latOffset = location.getLocalY().multiply(new BigDecimal("0.00001"));
                
                location.setGlobalLongitude(buildingLon.add(lonOffset).setScale(8, RoundingMode.HALF_UP));
                location.setGlobalLatitude(buildingLat.add(latOffset).setScale(8, RoundingMode.HALF_UP));
                
                // 计算海拔
                BigDecimal buildingElevation = building.getFloorHeight() != null ? 
                    building.getFloorHeight() : BigDecimal.ZERO;
                location.setGlobalAltitude(buildingElevation.add(location.getLocalZ() != null ? 
                    location.getLocalZ() : BigDecimal.ZERO));
            }
        } catch (Exception e) {
            log.error("计算全局坐标失败", e);
        }
    }

    /**
     * 判断点是否在多边形内（Ray Casting算法）
     */
    private boolean isPointInPolygon(BigDecimal x, BigDecimal y, String polygonGeom) {
        if (polygonGeom == null || polygonGeom.isEmpty()) {
            return false;
        }

        try {
            // 解析多边形坐标
            // 格式: POLYGON((x1 y1, x2 y2, x3 y3, ...))
            Pattern pattern = Pattern.compile("\\(([\\d.,\\s]+)\\)");
            Matcher matcher = pattern.matcher(polygonGeom);
            
            if (!matcher.find()) {
                return false;
            }

            String coords = matcher.group(1);
            String[] points = coords.split(",");
            
            if (points.length < 3) {
                return false;
            }

            boolean inside = false;
            double px = x.doubleValue();
            double py = y.doubleValue();

            double[] xPoints = new double[points.length];
            double[] yPoints = new double[points.length];

            for (int i = 0; i < points.length; i++) {
                String[] point = points[i].trim().split("\\s+");
                xPoints[i] = Double.parseDouble(point[0]);
                yPoints[i] = Double.parseDouble(point[1]);
            }

            // Ray Casting算法
            for (int i = 0, j = points.length - 1; i < points.length; j = i++) {
                if ((yPoints[i] > py) != (yPoints[j] > py) &&
                    (px < (xPoints[j] - xPoints[i]) * (py - yPoints[i]) / (yPoints[j] - yPoints[i]) + xPoints[i])) {
                    inside = !inside;
                }
            }

            return inside;
        } catch (Exception e) {
            log.error("判断点是否在多边形内失败", e);
            return false;
        }
    }

    /**
     * 构建设备（含位置）VO
     */
    private DeviceWithLocationRespVO buildDeviceWithLocationVO(DeviceLocationDO location) {
        DeviceWithLocationRespVO vo = new DeviceWithLocationRespVO();
        
        // 复制位置信息
        BeanUtils.copyProperties(location, vo);
        vo.setLocationId(location.getId());
        
        // TODO: 从设备表查询设备基本信息并填充
        // 这里需要注入DeviceService或DeviceMapper
        
        // 填充楼层、建筑、区域名称
        if (location.getFloorId() != null) {
            FloorDO floor = floorMapper.selectById(location.getFloorId());
            if (floor != null) {
                vo.setFloorName(floor.getName());
                vo.setFloorNumber(floor.getFloorNumber());
            }
        }
        
        if (location.getBuildingId() != null) {
            BuildingDO building = buildingMapper.selectById(location.getBuildingId());
            if (building != null) {
                vo.setBuildingName(building.getName());
            }
        }
        
        if (location.getAreaId() != null) {
            AreaDO area = areaMapper.selectById(location.getAreaId());
            if (area != null) {
                vo.setAreaName(area.getName());
                vo.setAreaType(area.getAreaType());
            }
        }
        
        return vo;
    }

}












