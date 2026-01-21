package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.service.IotDeviceServiceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceServiceLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 设备服务调用日志 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface IotDeviceServiceLogMapper extends BaseMapperX<IotDeviceServiceLogDO> {

    default PageResult<IotDeviceServiceLogDO> selectPage(IotDeviceServiceLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceServiceLogDO>()
                .eqIfPresent(IotDeviceServiceLogDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotDeviceServiceLogDO::getProductId, reqVO.getProductId())
                .eqIfPresent(IotDeviceServiceLogDO::getServiceIdentifier, reqVO.getServiceIdentifier())
                .eqIfPresent(IotDeviceServiceLogDO::getStatusCode, reqVO.getStatusCode())
                .betweenIfPresent(IotDeviceServiceLogDO::getRequestTime, reqVO.getRequestTime())
                .orderByDesc(IotDeviceServiceLogDO::getRequestTime));
    }
}












