package cn.iocoder.yudao.module.iot.dal.mysql.alarm;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmPartitionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 报警主机分区 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAlarmPartitionMapper extends BaseMapperX<IotAlarmPartitionDO> {

    /**
     * 根据主机ID查询分区列表
     *
     * @param hostId 主机ID
     * @return 分区列表
     */
    default List<IotAlarmPartitionDO> selectListByHostId(Long hostId) {
        return selectList(IotAlarmPartitionDO::getHostId, hostId);
    }

    /**
     * 根据主机ID和分区编号查询分区
     *
     * @param hostId 主机ID
     * @param partitionNo 分区编号
     * @return 分区信息
     */
    default IotAlarmPartitionDO selectByHostIdAndPartitionNo(Long hostId, Integer partitionNo) {
        return selectOne(IotAlarmPartitionDO::getHostId, hostId,
                IotAlarmPartitionDO::getPartitionNo, partitionNo);
    }

}
