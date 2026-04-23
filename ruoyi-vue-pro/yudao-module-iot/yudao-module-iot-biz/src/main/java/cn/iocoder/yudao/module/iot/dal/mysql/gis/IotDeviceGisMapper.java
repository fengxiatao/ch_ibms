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
    @Select("SELECT " +
            "r.device_id AS id, " +
            "r.room_id, " +
            "r.floor_id, " +
            "r.building_id, " +
            "d.name, " +
            "d.device_code AS code, " +
            "NULL AS category, " +
            "d.device_type_code AS device_type, " +
            "NULL AS sub_type, " +
            "d.brand, " +
            "d.product_model AS model, " +
            "d.ip AS ip_address, " +
            "CASE WHEN r.state IN (1, 3) THEN 'online' WHEN r.state = 2 THEN 'offline' ELSE 'fault' END AS status, " +
            "NULL AS health_status, " +
            "r.online_time, " +
            "r.offline_time, " +
            "r.install_location, " +
            "NULL AS install_height, " +
            "NULL AS install_date, " +
            "CONCAT('POINT(', r.longitude, ' ', r.latitude, ')') AS geom, " +
            "NULL AS altitude, " +
            "r.address, " +
            "d.extra AS properties, " +
            "r.config AS config, " +
            "d.nickname AS remark, " +
            "r.create_time, r.update_time, r.creator, r.updater, r.deleted, r.tenant_id " +
            "FROM ibms_device_runtime r " +
            "JOIN ibms_device d ON d.id = r.device_id " +
            "WHERE r.room_id = #{roomId} AND r.deleted = 0 " +
            "ORDER BY r.device_id")
    List<IotDeviceGisDO> selectListByRoomId(@Param("roomId") Long roomId);

    /**
     * 根据楼层ID获取设备列表
     *
     * @param floorId 楼层ID
     * @return 设备列表
     */
    @Select("SELECT " +
            "r.device_id AS id, " +
            "r.room_id, " +
            "r.floor_id, " +
            "r.building_id, " +
            "d.name, " +
            "d.device_code AS code, " +
            "NULL AS category, " +
            "d.device_type_code AS device_type, " +
            "NULL AS sub_type, " +
            "d.brand, " +
            "d.product_model AS model, " +
            "d.ip AS ip_address, " +
            "CASE WHEN r.state IN (1, 3) THEN 'online' WHEN r.state = 2 THEN 'offline' ELSE 'fault' END AS status, " +
            "NULL AS health_status, " +
            "r.online_time, " +
            "r.offline_time, " +
            "r.install_location, " +
            "NULL AS install_height, " +
            "NULL AS install_date, " +
            "CONCAT('POINT(', r.longitude, ' ', r.latitude, ')') AS geom, " +
            "NULL AS altitude, " +
            "r.address, " +
            "d.extra AS properties, " +
            "r.config AS config, " +
            "d.nickname AS remark, " +
            "r.create_time, r.update_time, r.creator, r.updater, r.deleted, r.tenant_id " +
            "FROM ibms_device_runtime r " +
            "JOIN ibms_device d ON d.id = r.device_id " +
            "WHERE r.floor_id = #{floorId} AND r.deleted = 0 " +
            "ORDER BY r.room_id, r.device_id")
    List<IotDeviceGisDO> selectListByFloorId(@Param("floorId") Long floorId);

    /**
     * 根据建筑ID获取设备列表
     *
     * @param buildingId 建筑ID
     * @return 设备列表
     */
    @Select("SELECT " +
            "r.device_id AS id, " +
            "r.room_id, " +
            "r.floor_id, " +
            "r.building_id, " +
            "d.name, " +
            "d.device_code AS code, " +
            "NULL AS category, " +
            "d.device_type_code AS device_type, " +
            "NULL AS sub_type, " +
            "d.brand, " +
            "d.product_model AS model, " +
            "d.ip AS ip_address, " +
            "CASE WHEN r.state IN (1, 3) THEN 'online' WHEN r.state = 2 THEN 'offline' ELSE 'fault' END AS status, " +
            "NULL AS health_status, " +
            "r.online_time, " +
            "r.offline_time, " +
            "r.install_location, " +
            "NULL AS install_height, " +
            "NULL AS install_date, " +
            "CONCAT('POINT(', r.longitude, ' ', r.latitude, ')') AS geom, " +
            "NULL AS altitude, " +
            "r.address, " +
            "d.extra AS properties, " +
            "r.config AS config, " +
            "d.nickname AS remark, " +
            "r.create_time, r.update_time, r.creator, r.updater, r.deleted, r.tenant_id " +
            "FROM ibms_device_runtime r " +
            "JOIN ibms_device d ON d.id = r.device_id " +
            "WHERE r.building_id = #{buildingId} AND r.deleted = 0 " +
            "ORDER BY r.floor_id, r.room_id, r.device_id")
    List<IotDeviceGisDO> selectListByBuildingId(@Param("buildingId") Long buildingId);

    /**
     * 统计楼层设备数量
     *
     * @param floorId 楼层ID
     * @return 设备数量
     */
    @Select("SELECT COUNT(*) FROM ibms_device_runtime WHERE floor_id = #{floorId} AND deleted = 0")
    Integer countByFloorId(@Param("floorId") Long floorId);

    /**
     * 统计楼层在线设备数量
     *
     * @param floorId 楼层ID
     * @return 在线设备数量
     */
    @Select("SELECT COUNT(*) FROM ibms_device_runtime WHERE floor_id = #{floorId} AND state IN (1, 3) AND deleted = 0")
    Integer countOnlineByFloorId(@Param("floorId") Long floorId);

    /**
     * 统计房间设备数量
     *
     * @param roomId 房间ID
     * @return 设备数量
     */
    @Select("SELECT COUNT(*) FROM ibms_device_runtime WHERE room_id = #{roomId} AND deleted = 0")
    Integer countByRoomId(@Param("roomId") Long roomId);

}












