package cn.iocoder.yudao.module.iot.convert.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 报警主机 Convert
 *
 * @author 长辉信息科技有限公司
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IotAlarmHostConvert {

    IotAlarmHostConvert INSTANCE = Mappers.getMapper(IotAlarmHostConvert.class);

    IotAlarmHostDO convert(IotAlarmHostCreateReqVO bean);

    IotAlarmHostDO convert(IotAlarmHostUpdateReqVO bean);

    IotAlarmHostRespVO convert(IotAlarmHostDO bean);

    PageResult<IotAlarmHostRespVO> convertPage(PageResult<IotAlarmHostDO> page);

}
