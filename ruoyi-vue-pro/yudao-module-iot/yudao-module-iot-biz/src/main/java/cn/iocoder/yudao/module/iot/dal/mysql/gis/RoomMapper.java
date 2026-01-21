package cn.iocoder.yudao.module.iot.dal.mysql.gis;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.RoomDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 房间 Mapper
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author 芋道源码
 */
@Mapper
public interface RoomMapper extends BaseMapperX<RoomDO> {

    /**
     * 根据楼层ID获取房间列表
     *
     * @param floorId 楼层ID
     * @return 房间列表
     */
    @Select("SELECT id, floor_id, building_id, name, code, room_number, room_type, room_area, ceiling_height, " +
            "primary_use, capacity, is_public, access_level, decoration_level, " +
            "floor_material, wall_material, has_window, window_count, has_door, door_type, " +
            "occupancy_status, tenant_name, lease_start_date, lease_end_date, " +
            "design_temperature, design_humidity, ventilation_rate, " +
            "geom, door_location, remark, " +
            "create_time, update_time, creator, updater, deleted, tenant_id " +
            "FROM room WHERE floor_id = #{floorId} AND deleted = 0 ORDER BY id")
    List<RoomDO> selectListByFloorId(@Param("floorId") Long floorId);

    /**
     * 根据建筑ID获取所有房间
     *
     * @param buildingId 建筑ID
     * @return 房间列表
     */
    @Select("SELECT id, floor_id, building_id, name, code, room_type, room_area, " +
            "geom, remark " +
            "FROM room WHERE building_id = #{buildingId} AND deleted = 0 ORDER BY floor_id, id")
    List<RoomDO> selectListByBuildingId(@Param("buildingId") Long buildingId);

}












