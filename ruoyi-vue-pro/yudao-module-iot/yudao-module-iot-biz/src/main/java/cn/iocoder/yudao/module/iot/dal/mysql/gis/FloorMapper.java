package cn.iocoder.yudao.module.iot.dal.mysql.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.floor.FloorPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 楼层 Mapper
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author 芋道源码
 */
@Mapper
public interface FloorMapper extends BaseMapperX<FloorDO> {

    /**
     * ✅ 分页查询楼层（支持搜索条件）
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    default PageResult<FloorDO> selectPage(FloorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FloorDO>()
                .likeIfPresent(FloorDO::getName, reqVO.getName())              // 楼层名称模糊查询
                .likeIfPresent(FloorDO::getCode, reqVO.getCode())              // 楼层编码模糊查询
                .eqIfPresent(FloorDO::getBuildingId, reqVO.getBuildingId())    // 所属建筑精确查询
                .eqIfPresent(FloorDO::getFloorNumber, reqVO.getFloorNumber())  // 楼层号精确查询
                .betweenIfPresent(FloorDO::getCreateTime, reqVO.getCreateTime()) // 创建时间范围查询
                .orderByAsc(FloorDO::getBuildingId)                            // 先按建筑排序
                .orderByAsc(FloorDO::getFloorNumber));                          // 再按楼层号排序
    }

    /**
     * 根据建筑ID获取楼层列表
     *
     * @param buildingId 建筑ID
     * @return 楼层列表
     */
    @Select("SELECT id, tenant_id, building_id, name, code, floor_number, floor_type, floor_height, " +
            "floor_area, usable_area, primary_function, occupancy_rate, max_occupancy, " +
            "has_sprinkler, has_smoke_detector, has_emergency_exit, emergency_exit_count, " +
            "ac_type, design_temp_summer, design_temp_winter, geom, " +
            "absolute_elevation, z_base, z_top, remark, " +
            "dxf_file_path, dxf_file_name, dxf_file_size, dxf_upload_time, " +
            "create_time, update_time, creator, updater, deleted " +
            "FROM floor WHERE building_id = #{buildingId} AND deleted = 0 ORDER BY floor_number")
    List<FloorDO> selectListByBuildingId(@Param("buildingId") Long buildingId);

    /**
     * 根据楼层ID获取详情（包含几何数据的 WKT 格式）
     *
     * @param id 楼层ID
     * @return 楼层详情
     */
    @Select("SELECT id, tenant_id, building_id, name, code, floor_number, floor_type, floor_height, " +
            "floor_area, usable_area, primary_function, occupancy_rate, max_occupancy, " +
            "has_sprinkler, has_smoke_detector, has_emergency_exit, emergency_exit_count, " +
            "ac_type, design_temp_summer, design_temp_winter, geom, " +
            "absolute_elevation, z_base, z_top, remark, " +
            "dxf_file_path, dxf_file_name, dxf_file_size, dxf_upload_time, " +
            "create_time, update_time, creator, updater, deleted " +
            "FROM floor WHERE id = #{id} AND deleted = 0")
    FloorDO selectByIdWithGeom(@Param("id") Long id);

    /**
     * 查询所有配置了 jobConfig 的楼层
     *
     * @return 楼层列表
     */
    default List<FloorDO> selectFloorWithJobConfig() {
        return selectList(new LambdaQueryWrapper<FloorDO>()
                .isNotNull(FloorDO::getJobConfig)
                .ne(FloorDO::getJobConfig, "")
                .ne(FloorDO::getJobConfig, "{}"));
    }

}












