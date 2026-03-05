package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy.IbmsEnergyAlarmPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnergyAlarmDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * 能耗告警 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsEnergyAlarmMapper extends BaseMapperX<IbmsEnergyAlarmDO> {

    default PageResult<IbmsEnergyAlarmDO> selectPage(IbmsEnergyAlarmPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsEnergyAlarmDO>()
                .eqIfPresent(IbmsEnergyAlarmDO::getMeterId, reqVO.getMeterId())
                .likeIfPresent(IbmsEnergyAlarmDO::getMeterName, reqVO.getMeterName())
                .eqIfPresent(IbmsEnergyAlarmDO::getAlarmType, reqVO.getAlarmType())
                .eqIfPresent(IbmsEnergyAlarmDO::getAlarmLevel, reqVO.getAlarmLevel())
                .eqIfPresent(IbmsEnergyAlarmDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IbmsEnergyAlarmDO::getAlarmTime, reqVO.getStartTime(), reqVO.getEndTime())
                .orderByDesc(IbmsEnergyAlarmDO::getAlarmTime));
    }

    default long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapperX<IbmsEnergyAlarmDO>()
                .eqIfPresent(IbmsEnergyAlarmDO::getStatus, status));
    }

    default long selectCountByLevel(Integer alarmLevel) {
        return selectCount(new LambdaQueryWrapperX<IbmsEnergyAlarmDO>()
                .eqIfPresent(IbmsEnergyAlarmDO::getAlarmLevel, alarmLevel));
    }

    default long selectCountByStatusAndLevel(Integer status, Integer alarmLevel) {
        return selectCount(new LambdaQueryWrapperX<IbmsEnergyAlarmDO>()
                .eqIfPresent(IbmsEnergyAlarmDO::getStatus, status)
                .eqIfPresent(IbmsEnergyAlarmDO::getAlarmLevel, alarmLevel));
    }

    default long selectCountByToday() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        return selectCount(new LambdaQueryWrapperX<IbmsEnergyAlarmDO>()
                .between(IbmsEnergyAlarmDO::getAlarmTime, todayStart, todayEnd));
    }

}
