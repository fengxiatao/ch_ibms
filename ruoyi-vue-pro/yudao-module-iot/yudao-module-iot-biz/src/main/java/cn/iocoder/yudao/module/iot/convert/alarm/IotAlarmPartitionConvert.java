package cn.iocoder.yudao.module.iot.convert.alarm;

import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.partition.IotAlarmPartitionRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmPartitionDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 报警主机分区 Convert
 *
 * @author 长辉信息科技有限公司
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IotAlarmPartitionConvert {

    IotAlarmPartitionConvert INSTANCE = Mappers.getMapper(IotAlarmPartitionConvert.class);

    IotAlarmPartitionRespVO convert(IotAlarmPartitionDO bean);

    List<IotAlarmPartitionRespVO> convertList(List<IotAlarmPartitionDO> list);
}
