package cn.iocoder.yudao.module.iot.dal.mysql.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building.BuildingPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 建筑 Mapper
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author IBMS Team
 */
@Mapper
public interface BuildingMapper extends BaseMapperX<BuildingDO> {

    default PageResult<BuildingDO> selectPage(BuildingPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BuildingDO>()
                .likeIfPresent(BuildingDO::getName, reqVO.getName())
                .likeIfPresent(BuildingDO::getCode, reqVO.getCode())
                .eqIfPresent(BuildingDO::getCampusId, reqVO.getCampusId())
                .eqIfPresent(BuildingDO::getBuildingType, reqVO.getBuildingType())
                .betweenIfPresent(BuildingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BuildingDO::getId));
    }

    default Long countByCampusId(Long campusId) {
        return selectCount(BuildingDO::getCampusId, campusId);
    }

    /**
     * 查询所有配置了 jobConfig 的建筑
     *
     * @return 建筑列表
     */
    default List<BuildingDO> selectBuildingWithJobConfig() {
        return selectList(new LambdaQueryWrapper<BuildingDO>()
                .isNotNull(BuildingDO::getJobConfig)
                .ne(BuildingDO::getJobConfig, "")
                .ne(BuildingDO::getJobConfig, "{}"));
    }

}

