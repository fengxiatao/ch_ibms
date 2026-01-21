package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.DeviceVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.FloorDetailVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.FloorVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.floor.FloorPageReqVO;
import cn.iocoder.yudao.module.iot.convert.gis.IotGisConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.IotDeviceGisDO;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.AreaMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.FloorMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.IotDeviceGisMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * IoT GIS 楼层 Service 实现类
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotGisFloorServiceImpl implements IotGisFloorService {

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private AreaMapper areaMapper;

    @Autowired
    private IotDeviceGisMapper deviceMapper;

    @Override
    public List<FloorVO> getBuildingFloors(Long buildingId) {
        // 1. 查询楼层列表
        List<FloorDO> floorDOs = floorMapper.selectListByBuildingId(buildingId);
        
        // 2. 转换为 VO
        List<FloorVO> floorVOs = IotGisConvert.INSTANCE.convertFloorList(floorDOs);
        
        // 3. 填充区域和设备数量统计
        for (FloorVO floorVO : floorVOs) {
            // 统计区域数量（原 room 改为 area）
            List<AreaDO> areas = areaMapper.selectListByFloorId(floorVO.getId());
            floorVO.setRoomCount(areas.size());
            
            // 统计设备数量
            Integer deviceCount = deviceMapper.countByFloorId(floorVO.getId());
            floorVO.setDeviceCount(deviceCount != null ? deviceCount : 0);
            
            // 统计在线设备数量
            Integer onlineDeviceCount = deviceMapper.countOnlineByFloorId(floorVO.getId());
            floorVO.setOnlineDeviceCount(onlineDeviceCount != null ? onlineDeviceCount : 0);
        }
        
        return floorVOs;
    }

    @Override
    public FloorDetailVO getFloorDetail(Long floorId) {
        // 1. 查询楼层信息
        FloorDO floorDO = floorMapper.selectByIdWithGeom(floorId);
        if (floorDO == null) {
            return null;
        }
        
        FloorVO floorVO = IotGisConvert.INSTANCE.convert(floorDO);
        
        // 2. 查询区域列表（原 room 改为 area）
        List<AreaDO> areaDOs = areaMapper.selectListByFloorId(floorId);
        
        // 3. 查询该楼层所有设备
        List<IotDeviceGisDO> deviceDOs = deviceMapper.selectListByFloorId(floorId);
        List<DeviceVO> deviceVOs = IotGisConvert.INSTANCE.convertDeviceList(deviceDOs);
        
        // 4. 填充楼层统计
        floorVO.setRoomCount(areaDOs.size());
        floorVO.setDeviceCount(deviceVOs.size());
        floorVO.setOnlineDeviceCount((int) deviceVOs.stream()
                .filter(d -> "在线".equals(d.getStatus()))
                .count());
        
        // 5. 组装返回结果（暂时不返回区域列表详情，前端应该调用 /iot/area/floor/{floorId}/visualization 获取详细数据）
        FloorDetailVO result = new FloorDetailVO();
        result.setFloor(floorVO);
        result.setRooms(new ArrayList<>());  // 暂时返回空列表，前端使用 Area API
        
        return result;
    }

    @Override
    public FloorVO getFloorInfo(Long floorId) {
        FloorDO floorDO = floorMapper.selectByIdWithGeom(floorId);
        if (floorDO == null) {
            return null;
        }
        
        FloorVO floorVO = IotGisConvert.INSTANCE.convert(floorDO);
        
        // 填充统计信息（原 room 改为 area）
        List<AreaDO> areas = areaMapper.selectListByFloorId(floorId);
        floorVO.setRoomCount(areas.size());
        
        Integer deviceCount = deviceMapper.countByFloorId(floorId);
        floorVO.setDeviceCount(deviceCount != null ? deviceCount : 0);
        
        Integer onlineDeviceCount = deviceMapper.countOnlineByFloorId(floorId);
        floorVO.setOnlineDeviceCount(onlineDeviceCount != null ? onlineDeviceCount : 0);
        
        return floorVO;
    }

    // ========== 新增 CRUD 方法实现 ==========

    @Override
    public Long createFloor(FloorDO floor) {
        floorMapper.insert(floor);
        return floor.getId();
    }

    @Override
    public void updateFloor(FloorDO floor) {
        floorMapper.updateById(floor);
    }

    @Override
    public void deleteFloor(Long id) {
        floorMapper.deleteById(id);
    }

    @Override
    public FloorDO getFloor(Long id) {
        return floorMapper.selectById(id);
    }

    @Override
    public List<FloorDO> getFloorList() {
        return floorMapper.selectList();
    }

    @Override
    public PageResult<FloorDO> getFloorPage(FloorPageReqVO pageReqVO) {
        // ✅ 调用 Mapper 自定义的 selectPage 方法（单参数），而不是 BaseMapperX 的默认方法（双参数）
        return floorMapper.selectPage(pageReqVO);
    }

    @Override
    public List<FloorDO> getFloorListByBuildingId(Long buildingId) {
        return floorMapper.selectListByBuildingId(buildingId);
    }

    @Override
    public List<FloorDO> getFloorWithJobConfig() {
        return floorMapper.selectFloorWithJobConfig();
    }

    @Override
    public void updateFloorJobConfig(Long id, String jobConfig) {
        FloorDO updateObj = new FloorDO();
        updateObj.setId(id);
        updateObj.setJobConfig(jobConfig);
        floorMapper.updateById(updateObj);
    }

}

