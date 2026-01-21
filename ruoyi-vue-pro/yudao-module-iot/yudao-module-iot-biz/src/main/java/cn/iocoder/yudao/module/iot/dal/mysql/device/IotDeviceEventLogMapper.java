package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.event.IotDeviceEventLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceEventLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 设备事件日志 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface IotDeviceEventLogMapper extends BaseMapperX<IotDeviceEventLogDO> {

    default PageResult<IotDeviceEventLogDO> selectPage(IotDeviceEventLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceEventLogDO>()
                .eqIfPresent(IotDeviceEventLogDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotDeviceEventLogDO::getProductId, reqVO.getProductId())
                .eqIfPresent(IotDeviceEventLogDO::getEventIdentifier, reqVO.getEventIdentifier())
                .eqIfPresent(IotDeviceEventLogDO::getEventType, reqVO.getEventType())
                .betweenIfPresent(IotDeviceEventLogDO::getEventTime, reqVO.getEventTime())
                .orderByDesc(IotDeviceEventLogDO::getEventTime));
    }
}












