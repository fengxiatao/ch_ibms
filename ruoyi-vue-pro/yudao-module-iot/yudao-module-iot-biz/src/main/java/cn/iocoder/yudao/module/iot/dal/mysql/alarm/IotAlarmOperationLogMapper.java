package cn.iocoder.yudao.module.iot.dal.mysql.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.operationlog.IotAlarmOperationLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmOperationLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报警主机操作记录 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface IotAlarmOperationLogMapper extends BaseMapperX<IotAlarmOperationLogDO> {

    default PageResult<IotAlarmOperationLogDO> selectPage(IotAlarmOperationLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotAlarmOperationLogDO>()
                .eqIfPresent(IotAlarmOperationLogDO::getHostId, reqVO.getHostId())
                .eqIfPresent(IotAlarmOperationLogDO::getPartitionId, reqVO.getPartitionId())
                .eqIfPresent(IotAlarmOperationLogDO::getZoneId, reqVO.getZoneId())
                .eqIfPresent(IotAlarmOperationLogDO::getOperationType, reqVO.getOperationType())
                .betweenIfPresent(IotAlarmOperationLogDO::getOperationTime, reqVO.getOperationTime())
                .orderByDesc(IotAlarmOperationLogDO::getOperationTime));
    }

}
