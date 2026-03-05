package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting.IbmsLightingSchedulePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsLightingScheduleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 照明定时任务 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsLightingScheduleMapper extends BaseMapperX<IbmsLightingScheduleDO> {

    default PageResult<IbmsLightingScheduleDO> selectPage(IbmsLightingSchedulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsLightingScheduleDO>()
                .likeIfPresent(IbmsLightingScheduleDO::getScheduleName, reqVO.getScheduleName())
                .eqIfPresent(IbmsLightingScheduleDO::getSceneId, reqVO.getSceneId())
                .eqIfPresent(IbmsLightingScheduleDO::getEnabled, reqVO.getEnabled())
                .orderByDesc(IbmsLightingScheduleDO::getId));
    }

    default List<IbmsLightingScheduleDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<IbmsLightingScheduleDO>()
                .eq(IbmsLightingScheduleDO::getEnabled, true)
                .orderByDesc(IbmsLightingScheduleDO::getId));
    }

}
