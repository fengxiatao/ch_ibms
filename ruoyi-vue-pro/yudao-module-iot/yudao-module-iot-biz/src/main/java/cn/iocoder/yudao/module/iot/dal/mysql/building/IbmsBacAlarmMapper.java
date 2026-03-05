package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac.IbmsBacAlarmPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsBacAlarmDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 楼宇自控告警 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsBacAlarmMapper extends BaseMapperX<IbmsBacAlarmDO> {

    default PageResult<IbmsBacAlarmDO> selectPage(IbmsBacAlarmPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsBacAlarmDO>()
                .eqIfPresent(IbmsBacAlarmDO::getDeviceType, reqVO.getDeviceType())
                .likeIfPresent(IbmsBacAlarmDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IbmsBacAlarmDO::getAlarmLevel, reqVO.getAlarmLevel())
                .eqIfPresent(IbmsBacAlarmDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IbmsBacAlarmDO::getAlarmTime, reqVO.getStartTime(), reqVO.getEndTime())
                .orderByDesc(IbmsBacAlarmDO::getAlarmTime));
    }

    default long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapperX<IbmsBacAlarmDO>()
                .eqIfPresent(IbmsBacAlarmDO::getStatus, status));
    }

    default long selectCountByLevel(Integer alarmLevel) {
        return selectCount(new LambdaQueryWrapperX<IbmsBacAlarmDO>()
                .eqIfPresent(IbmsBacAlarmDO::getAlarmLevel, alarmLevel));
    }

}
