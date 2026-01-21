package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.DeviceLocationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 设备位置信息 Mapper
 *
 * @author IBMS Team
 */
@Mapper
public interface DeviceLocationMapper extends BaseMapperX<DeviceLocationDO> {

    /**
     * 根据设备ID查询位置信息
     */
    default DeviceLocationDO selectByDeviceId(Long deviceId) {
        return selectOne(DeviceLocationDO::getDeviceId, deviceId);
    }

    /**
     * 根据楼层ID查询所有设备位置
     */
    default List<DeviceLocationDO> selectListByFloorId(Long floorId) {
        return selectList(DeviceLocationDO::getFloorId, floorId);
    }

    /**
     * 根据建筑ID查询所有设备位置
     */
    default List<DeviceLocationDO> selectListByBuildingId(Long buildingId) {
        return selectList(DeviceLocationDO::getBuildingId, buildingId);
    }

    /**
     * 根据区域ID查询所有设备位置
     */
    default List<DeviceLocationDO> selectListByAreaId(Long areaId) {
        return selectList(DeviceLocationDO::getAreaId, areaId);
    }

    /**
     * 批量插入设备位置
     */
    @Select("INSERT INTO device_location (tenant_id, device_id, floor_id, building_id, area_id, " +
            "local_x, local_y, local_z, creator, create_time, updater, update_time, deleted) " +
            "VALUES (#{tenantId}, #{deviceId}, #{floorId}, #{buildingId}, #{areaId}, " +
            "#{localX}, #{localY}, #{localZ}, #{creator}, NOW(), #{updater}, NOW(), 0)")
    int batchInsert(@Param("list") List<DeviceLocationDO> list);

}


















