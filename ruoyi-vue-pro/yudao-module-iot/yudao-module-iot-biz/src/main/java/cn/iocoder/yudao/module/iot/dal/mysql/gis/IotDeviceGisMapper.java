package cn.iocoder.yudao.module.iot.dal.mysql.gis;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.IotDeviceGisDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * IoT 设备 GIS Mapper
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author 芋道源码
 */
@Mapper
public interface IotDeviceGisMapper extends BaseMapperX<IotDeviceGisDO> {

    /**
     * 根据房间ID获取设备列表
     *
     * @param roomId 房间ID
     * @return 设备列表
     */
    @Select("SELECT id, room_id, floor_id, building_id, name, code, category, device_type, sub_type, " +
            "brand, model, ip_address, status, health_status, online_time, offline_time, " +
            "install_location, install_height, install_date, " +
            "geom, altitude, address, properties, config, remark, " +
            "create_time, update_time, creator, updater, deleted, tenant_id " +
            "FROM iot_device WHERE room_id = #{roomId} AND deleted = 0 ORDER BY id")
    List<IotDeviceGisDO> selectListByRoomId(@Param("roomId") Long roomId);

    /**
     * 根据楼层ID获取设备列表
     *
     * @param floorId 楼层ID
     * @return 设备列表
     */
    @Select("SELECT id, room_id, floor_id, building_id, name, code, device_type, status, health_status, " +
            "geom, remark " +
            "FROM iot_device WHERE floor_id = #{floorId} AND deleted = 0 ORDER BY room_id, id")
    List<IotDeviceGisDO> selectListByFloorId(@Param("floorId") Long floorId);

    /**
     * 根据建筑ID获取设备列表
     *
     * @param buildingId 建筑ID
     * @return 设备列表
     */
    @Select("SELECT id, room_id, floor_id, building_id, name, code, device_type, status, health_status, " +
            "geom " +
            "FROM iot_device WHERE building_id = #{buildingId} AND deleted = 0 ORDER BY floor_id, room_id, id")
    List<IotDeviceGisDO> selectListByBuildingId(@Param("buildingId") Long buildingId);

    /**
     * 统计楼层设备数量
     *
     * @param floorId 楼层ID
     * @return 设备数量
     */
    @Select("SELECT COUNT(*) FROM iot_device WHERE floor_id = #{floorId} AND deleted = 0")
    Integer countByFloorId(@Param("floorId") Long floorId);

    /**
     * 统计楼层在线设备数量
     *
     * @param floorId 楼层ID
     * @return 在线设备数量
     */
    @Select("SELECT COUNT(*) FROM iot_device WHERE floor_id = #{floorId} AND status = '在线' AND deleted = 0")
    Integer countOnlineByFloorId(@Param("floorId") Long floorId);

    /**
     * 统计房间设备数量
     *
     * @param roomId 房间ID
     * @return 设备数量
     */
    @Select("SELECT COUNT(*) FROM iot_device WHERE room_id = #{roomId} AND deleted = 0")
    Integer countByRoomId(@Param("roomId") Long roomId);

}












