package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting.IbmsLightingCircuitPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsLightingCircuitDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 照明回路 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsLightingCircuitMapper extends BaseMapperX<IbmsLightingCircuitDO> {

    default PageResult<IbmsLightingCircuitDO> selectPage(IbmsLightingCircuitPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsLightingCircuitDO>()
                .likeIfPresent(IbmsLightingCircuitDO::getCircuitCode, reqVO.getCircuitCode())
                .likeIfPresent(IbmsLightingCircuitDO::getCircuitName, reqVO.getCircuitName())
                .eqIfPresent(IbmsLightingCircuitDO::getCircuitType, reqVO.getCircuitType())
                .eqIfPresent(IbmsLightingCircuitDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(IbmsLightingCircuitDO::getStatus, reqVO.getStatus())
                .orderByDesc(IbmsLightingCircuitDO::getId));
    }

    default List<IbmsLightingCircuitDO> selectListByType(Integer circuitType) {
        return selectList(new LambdaQueryWrapperX<IbmsLightingCircuitDO>()
                .eqIfPresent(IbmsLightingCircuitDO::getCircuitType, circuitType)
                .orderByDesc(IbmsLightingCircuitDO::getId));
    }

    default long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapperX<IbmsLightingCircuitDO>()
                .eqIfPresent(IbmsLightingCircuitDO::getStatus, status));
    }

}
