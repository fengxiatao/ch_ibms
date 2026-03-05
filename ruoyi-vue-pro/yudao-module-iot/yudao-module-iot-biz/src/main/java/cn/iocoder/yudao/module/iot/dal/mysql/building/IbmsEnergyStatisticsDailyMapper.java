package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy.IbmsEnergyStatisticsPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnergyStatisticsDailyDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 能耗日统计 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsEnergyStatisticsDailyMapper extends BaseMapperX<IbmsEnergyStatisticsDailyDO> {

    default PageResult<IbmsEnergyStatisticsDailyDO> selectPage(IbmsEnergyStatisticsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsEnergyStatisticsDailyDO>()
                .eqIfPresent(IbmsEnergyStatisticsDailyDO::getMeterId, reqVO.getMeterId())
                .eqIfPresent(IbmsEnergyStatisticsDailyDO::getMeterType, reqVO.getMeterType())
                .eqIfPresent(IbmsEnergyStatisticsDailyDO::getAreaId, reqVO.getAreaId())
                .betweenIfPresent(IbmsEnergyStatisticsDailyDO::getStatDate, reqVO.getStartDate(), reqVO.getEndDate())
                .orderByDesc(IbmsEnergyStatisticsDailyDO::getStatDate));
    }

    default List<IbmsEnergyStatisticsDailyDO> selectListByDateRange(LocalDate startDate, LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<IbmsEnergyStatisticsDailyDO>()
                .betweenIfPresent(IbmsEnergyStatisticsDailyDO::getStatDate, startDate, endDate)
                .orderByAsc(IbmsEnergyStatisticsDailyDO::getStatDate));
    }

    default List<IbmsEnergyStatisticsDailyDO> selectListByMeterIdAndDateRange(Long meterId, LocalDate startDate, LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<IbmsEnergyStatisticsDailyDO>()
                .eq(IbmsEnergyStatisticsDailyDO::getMeterId, meterId)
                .between(IbmsEnergyStatisticsDailyDO::getStatDate, startDate, endDate)
                .orderByAsc(IbmsEnergyStatisticsDailyDO::getStatDate));
    }

    default List<IbmsEnergyStatisticsDailyDO> selectListByTypeAndDateRange(Integer meterType, LocalDate startDate, LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<IbmsEnergyStatisticsDailyDO>()
                .eqIfPresent(IbmsEnergyStatisticsDailyDO::getMeterType, meterType)
                .between(IbmsEnergyStatisticsDailyDO::getStatDate, startDate, endDate)
                .orderByAsc(IbmsEnergyStatisticsDailyDO::getStatDate));
    }

}
