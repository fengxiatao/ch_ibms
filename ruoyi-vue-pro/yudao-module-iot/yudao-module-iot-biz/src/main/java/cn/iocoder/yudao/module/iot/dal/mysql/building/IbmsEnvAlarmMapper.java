package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.env.IbmsEnvAlarmPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvAlarmDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 环境告警记录 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsEnvAlarmMapper extends BaseMapperX<IbmsEnvAlarmDO> {

    default PageResult<IbmsEnvAlarmDO> selectPage(IbmsEnvAlarmPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsEnvAlarmDO>()
                .eqIfPresent(IbmsEnvAlarmDO::getSensorId, reqVO.getSensorId())
                .likeIfPresent(IbmsEnvAlarmDO::getSensorName, reqVO.getSensorName())
                .eqIfPresent(IbmsEnvAlarmDO::getAlarmType, reqVO.getAlarmType())
                .eqIfPresent(IbmsEnvAlarmDO::getAlarmLevel, reqVO.getAlarmLevel())
                .eqIfPresent(IbmsEnvAlarmDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IbmsEnvAlarmDO::getAlarmTime, reqVO.getStartTime(), reqVO.getEndTime())
                .orderByDesc(IbmsEnvAlarmDO::getAlarmTime));
    }

    default long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapperX<IbmsEnvAlarmDO>()
                .eqIfPresent(IbmsEnvAlarmDO::getStatus, status));
    }

    default long selectCountByLevel(Integer alarmLevel) {
        return selectCount(new LambdaQueryWrapperX<IbmsEnvAlarmDO>()
                .eqIfPresent(IbmsEnvAlarmDO::getAlarmLevel, alarmLevel));
    }

}
