package cn.iocoder.yudao.module.iot.dal.mysql.channel;

import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelHistoryDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 设备通道历史记录 Mapper
 *
 * @author IBMS Team
 */
@Mapper
public interface IotDeviceChannelHistoryMapper extends BaseMapper<IotDeviceChannelHistoryDO> {
}
