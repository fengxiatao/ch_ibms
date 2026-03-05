package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting.IbmsLightingScenePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsLightingSceneDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 照明场景 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsLightingSceneMapper extends BaseMapperX<IbmsLightingSceneDO> {

    default PageResult<IbmsLightingSceneDO> selectPage(IbmsLightingScenePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsLightingSceneDO>()
                .likeIfPresent(IbmsLightingSceneDO::getSceneCode, reqVO.getSceneCode())
                .likeIfPresent(IbmsLightingSceneDO::getSceneName, reqVO.getSceneName())
                .eqIfPresent(IbmsLightingSceneDO::getAreaId, reqVO.getAreaId())
                .orderByAsc(IbmsLightingSceneDO::getSort));
    }

    default List<IbmsLightingSceneDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<IbmsLightingSceneDO>()
                .orderByAsc(IbmsLightingSceneDO::getSort));
    }

}
