package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy.IbmsEnergyMeterPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnergyMeterDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 能耗计量表 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsEnergyMeterMapper extends BaseMapperX<IbmsEnergyMeterDO> {

    default PageResult<IbmsEnergyMeterDO> selectPage(IbmsEnergyMeterPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsEnergyMeterDO>()
                .likeIfPresent(IbmsEnergyMeterDO::getMeterCode, reqVO.getMeterCode())
                .likeIfPresent(IbmsEnergyMeterDO::getMeterName, reqVO.getMeterName())
                .eqIfPresent(IbmsEnergyMeterDO::getMeterType, reqVO.getMeterType())
                .eqIfPresent(IbmsEnergyMeterDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(IbmsEnergyMeterDO::getStatus, reqVO.getStatus())
                .orderByDesc(IbmsEnergyMeterDO::getId));
    }

    default List<IbmsEnergyMeterDO> selectListByType(Integer meterType) {
        return selectList(new LambdaQueryWrapperX<IbmsEnergyMeterDO>()
                .eqIfPresent(IbmsEnergyMeterDO::getMeterType, meterType)
                .orderByDesc(IbmsEnergyMeterDO::getId));
    }

    default long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapperX<IbmsEnergyMeterDO>()
                .eqIfPresent(IbmsEnergyMeterDO::getStatus, status));
    }

    default long selectCountByType(Integer meterType) {
        return selectCount(new LambdaQueryWrapperX<IbmsEnergyMeterDO>()
                .eqIfPresent(IbmsEnergyMeterDO::getMeterType, meterType));
    }

}
