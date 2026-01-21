package cn.iocoder.yudao.module.iot.convert.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.operationlog.IotAlarmOperationLogRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmOperationLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 报警主机操作记录 Convert
 *
 * @author 长辉信息科技有限公司
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IotAlarmOperationLogConvert {

    IotAlarmOperationLogConvert INSTANCE = Mappers.getMapper(IotAlarmOperationLogConvert.class);

    IotAlarmOperationLogRespVO convert(IotAlarmOperationLogDO bean);

    PageResult<IotAlarmOperationLogRespVO> convertPage(PageResult<IotAlarmOperationLogDO> page);
}
