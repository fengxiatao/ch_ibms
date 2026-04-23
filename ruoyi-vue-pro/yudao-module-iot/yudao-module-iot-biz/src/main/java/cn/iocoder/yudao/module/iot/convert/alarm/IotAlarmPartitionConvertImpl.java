package cn.iocoder.yudao.module.iot.convert.alarm;

import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.partition.IotAlarmPartitionRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmPartitionDO;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报警主机分区 Convert 实现（手写，避免 MapStruct 与增量编译/并行构建下的 Filer 冲突）
 */
public class IotAlarmPartitionConvertImpl implements IotAlarmPartitionConvert {

    @Override
    public IotAlarmPartitionRespVO convert(IotAlarmPartitionDO bean) {
        if (bean == null) {
            return null;
        }
        IotAlarmPartitionRespVO vo = new IotAlarmPartitionRespVO();
        BeanUtils.copyProperties(bean, vo);
        return vo;
    }

    @Override
    public List<IotAlarmPartitionRespVO> convertList(List<IotAlarmPartitionDO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::convert).collect(Collectors.toList());
    }
}
