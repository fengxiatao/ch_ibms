package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy.IbmsEnergyRatePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnergyRateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 能耗费率设置 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsEnergyRateMapper extends BaseMapperX<IbmsEnergyRateDO> {

    default PageResult<IbmsEnergyRateDO> selectPage(IbmsEnergyRatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsEnergyRateDO>()
                .likeIfPresent(IbmsEnergyRateDO::getRateName, reqVO.getRateName())
                .eqIfPresent(IbmsEnergyRateDO::getEnergyType, reqVO.getEnergyType())
                .eqIfPresent(IbmsEnergyRateDO::getRateType, reqVO.getRateType())
                .eqIfPresent(IbmsEnergyRateDO::getStatus, reqVO.getStatus())
                .orderByAsc(IbmsEnergyRateDO::getEnergyType)
                .orderByAsc(IbmsEnergyRateDO::getTierLevel));
    }

    default List<IbmsEnergyRateDO> selectListByEnergyType(Integer energyType) {
        return selectList(new LambdaQueryWrapperX<IbmsEnergyRateDO>()
                .eqIfPresent(IbmsEnergyRateDO::getEnergyType, energyType)
                .eq(IbmsEnergyRateDO::getStatus, 1)
                .orderByAsc(IbmsEnergyRateDO::getTierLevel));
    }

    default List<IbmsEnergyRateDO> selectListByEnergyTypeAndRateType(Integer energyType, Integer rateType) {
        return selectList(new LambdaQueryWrapperX<IbmsEnergyRateDO>()
                .eqIfPresent(IbmsEnergyRateDO::getEnergyType, energyType)
                .eqIfPresent(IbmsEnergyRateDO::getRateType, rateType)
                .eq(IbmsEnergyRateDO::getStatus, 1)
                .orderByAsc(IbmsEnergyRateDO::getTierLevel));
    }

}
