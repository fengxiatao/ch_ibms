package cn.iocoder.yudao.module.iot.dal.mysql.ibms;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDiscoveredDeviceDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IBMS 发现设备 Mapper
 *
 * <p>忽略租户拦截：RocketMQ 消费者等场景可能无租户上下文
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface IbmsDiscoveredDeviceMapper extends BaseMapperX<IbmsDiscoveredDeviceDO> {

    default IbmsDiscoveredDeviceDO selectByIpAndTime(String ip, LocalDateTime since) {
        return selectOne(new LambdaQueryWrapperX<IbmsDiscoveredDeviceDO>()
            .eq(IbmsDiscoveredDeviceDO::getIpAddress, ip)
            .ge(IbmsDiscoveredDeviceDO::getDiscoveryTime, since)
            .orderByDesc(IbmsDiscoveredDeviceDO::getDiscoveryTime)
            .last("LIMIT 1"));
    }

    default List<IbmsDiscoveredDeviceDO> selectRecentDevices(LocalDateTime since) {
        return selectList(new LambdaQueryWrapperX<IbmsDiscoveredDeviceDO>()
            .ge(IbmsDiscoveredDeviceDO::getDiscoveryTime, since)
            .orderByDesc(IbmsDiscoveredDeviceDO::getDiscoveryTime));
    }

    default List<IbmsDiscoveredDeviceDO> selectUnaddedDevices() {
        return selectList(new LambdaQueryWrapperX<IbmsDiscoveredDeviceDO>()
            .eq(IbmsDiscoveredDeviceDO::getAdded, false)
            .eq(IbmsDiscoveredDeviceDO::getActivated, false)
            .orderByDesc(IbmsDiscoveredDeviceDO::getDiscoveryTime));
    }

    default IbmsDiscoveredDeviceDO selectByIp(String ip) {
        return selectOne(new LambdaQueryWrapperX<IbmsDiscoveredDeviceDO>()
            .eq(IbmsDiscoveredDeviceDO::getIpAddress, ip)
            .orderByDesc(IbmsDiscoveredDeviceDO::getDiscoveryTime)
            .last("LIMIT 1"));
    }

    default List<IbmsDiscoveredDeviceDO> selectUnactivatedDevices() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return selectList(new LambdaQueryWrapperX<IbmsDiscoveredDeviceDO>()
            .ge(IbmsDiscoveredDeviceDO::getDiscoveryTime, since)
            .eq(IbmsDiscoveredDeviceDO::getActivated, false)
            .ne(IbmsDiscoveredDeviceDO::getStatus, 3)
            .orderByDesc(IbmsDiscoveredDeviceDO::getDiscoveryTime));
    }
}
