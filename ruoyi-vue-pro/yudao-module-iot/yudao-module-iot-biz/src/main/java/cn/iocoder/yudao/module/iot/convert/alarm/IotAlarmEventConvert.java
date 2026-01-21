package cn.iocoder.yudao.module.iot.convert.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmEventDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 报警事件 Convert
 *
 * @author 长辉信息科技有限公司
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IotAlarmEventConvert {

    IotAlarmEventConvert INSTANCE = Mappers.getMapper(IotAlarmEventConvert.class);

    IotAlarmEventRespVO convert(IotAlarmEventDO bean);

    PageResult<IotAlarmEventRespVO> convertPage(PageResult<IotAlarmEventDO> page);
}
