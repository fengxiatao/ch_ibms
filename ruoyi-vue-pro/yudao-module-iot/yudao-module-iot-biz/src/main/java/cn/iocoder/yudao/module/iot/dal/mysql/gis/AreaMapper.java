package cn.iocoder.yudao.module.iot.dal.mysql.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.area.AreaPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 区域 Mapper
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author ruoyi
 */
@Mapper
public interface AreaMapper extends BaseMapperX<AreaDO> {

    /**
     * ✅ 分页查询区域（支持搜索条件）
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    default PageResult<AreaDO> selectPage(AreaPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AreaDO>()
                .likeIfPresent(AreaDO::getName, reqVO.getName())              // 区域名称模糊查询
                .likeIfPresent(AreaDO::getCode, reqVO.getCode())              // 区域编码模糊查询
                .eqIfPresent(AreaDO::getFloorId, reqVO.getFloorId())          // 所属楼层精确查询
                .eqIfPresent(AreaDO::getAreaType, reqVO.getAreaType())        // 区域类型精确查询
                .betweenIfPresent(AreaDO::getCreateTime, reqVO.getCreateTime()) // 创建时间范围查询
                .orderByAsc(AreaDO::getFloorId)                               // 先按楼层排序
                .orderByAsc(AreaDO::getDisplayOrder)                          // 再按显示顺序排序
                .orderByAsc(AreaDO::getId));                                   // 最后按ID排序
    }

    /**
     * 根据楼层ID查询所有区域
     */
    @Select("SELECT * FROM area WHERE floor_id = #{floorId} AND deleted = 0 ORDER BY display_order, id")
    List<AreaDO> selectListByFloorId(@Param("floorId") Long floorId);

    /**
     * 根据建筑ID查询所有区域
     */
    @Select("SELECT * FROM area WHERE building_id = #{buildingId} AND deleted = 0 ORDER BY floor_id, display_order, id")
    List<AreaDO> selectListByBuildingId(@Param("buildingId") Long buildingId);

    /**
     * 根据区域类型查询
     */
    @Select("SELECT * FROM area WHERE floor_id = #{floorId} AND area_type = #{areaType} AND deleted = 0")
    List<AreaDO> selectListByFloorIdAndType(@Param("floorId") Long floorId, @Param("areaType") String areaType);

    /**
     * 查询楼层可视化数据（调用PostgreSQL函数）
     */
    @Select("SELECT get_floor_visualization_data(#{floorId})")
    String getFloorVisualizationData(@Param("floorId") Long floorId);

    /**
     * 查找最短路径（调用PostgreSQL函数）
     */
    @Select("SELECT * FROM find_shortest_path(#{fromAreaId}, #{toAreaId}, #{accessibleOnly})")
    List<Map<String, Object>> findShortestPath(
        @Param("fromAreaId") Long fromAreaId,
        @Param("toAreaId") Long toAreaId,
        @Param("accessibleOnly") Boolean accessibleOnly
    );

    /**
     * 查找附近可导航区域
     */
    @Select("SELECT * FROM find_nearby_navigable_areas(#{areaId}, #{maxDistance}, #{includeVertical})")
    List<Map<String, Object>> findNearbyAreas(
        @Param("areaId") Long areaId,
        @Param("maxDistance") Double maxDistance,
        @Param("includeVertical") Boolean includeVertical
    );

    /**
     * 分析区域可达性
     */
    @Select("SELECT * FROM analyze_area_reachability(#{areaId}, #{maxHops})")
    List<Map<String, Object>> analyzeReachability(
        @Param("areaId") Long areaId,
        @Param("maxHops") Integer maxHops
    );

    /**
     * 设备定位到区域
     */
    @Select("SELECT locate_device_to_area(#{deviceId})")
    Long locateDeviceToArea(@Param("deviceId") Long deviceId);

    /**
     * 查询所有配置了 jobConfig 的区域
     *
     * @return 区域列表
     */
    default List<AreaDO> selectAreaWithJobConfig() {
        return selectList(new LambdaQueryWrapper<AreaDO>()
                .isNotNull(AreaDO::getJobConfig)
                .ne(AreaDO::getJobConfig, "")
                .ne(AreaDO::getJobConfig, "{}"));
    }

}











