package cn.iocoder.yudao.module.iot.convert.alarm;

import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.partition.IotAlarmPartitionRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmPartitionDO;

import java.util.List;

/**
 * 报警主机分区 Convert
 *
 * @author 长辉信息科技有限公司
 */
public interface IotAlarmPartitionConvert {

    IotAlarmPartitionConvert INSTANCE = new IotAlarmPartitionConvertImpl();

    IotAlarmPartitionRespVO convert(IotAlarmPartitionDO bean);

    List<IotAlarmPartitionRespVO> convertList(List<IotAlarmPartitionDO> list);
}
