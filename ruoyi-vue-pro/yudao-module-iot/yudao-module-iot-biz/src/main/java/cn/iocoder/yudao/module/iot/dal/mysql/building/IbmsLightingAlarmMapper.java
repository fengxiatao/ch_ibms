package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting.IbmsLightingAlarmPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsLightingAlarmDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 照明告警 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsLightingAlarmMapper extends BaseMapperX<IbmsLightingAlarmDO> {

    default PageResult<IbmsLightingAlarmDO> selectPage(IbmsLightingAlarmPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsLightingAlarmDO>()
                .eqIfPresent(IbmsLightingAlarmDO::getDeviceType, reqVO.getDeviceType())
                .likeIfPresent(IbmsLightingAlarmDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IbmsLightingAlarmDO::getAlarmLevel, reqVO.getAlarmLevel())
                .eqIfPresent(IbmsLightingAlarmDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IbmsLightingAlarmDO::getAlarmTime, reqVO.getStartTime(), reqVO.getEndTime())
                .orderByDesc(IbmsLightingAlarmDO::getAlarmTime));
    }

    default long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapperX<IbmsLightingAlarmDO>()
                .eqIfPresent(IbmsLightingAlarmDO::getStatus, status));
    }

}
