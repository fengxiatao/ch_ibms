package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDiscoveredDeviceDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 发现设备 Mapper
 * 
 * ⚠️ 忽略租户拦截：因为 RocketMQ 消费者调用时，未传递租户上下文
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface IotDiscoveredDeviceMapper extends BaseMapperX<IotDiscoveredDeviceDO> {
    
    /**
     * 根据IP和时间查询设备
     */
    default IotDiscoveredDeviceDO selectByIpAndTime(String ip, LocalDateTime since) {
        return selectOne(new LambdaQueryWrapperX<IotDiscoveredDeviceDO>()
            .eq(IotDiscoveredDeviceDO::getIpAddress, ip)
            .ge(IotDiscoveredDeviceDO::getDiscoveryTime, since)
            .orderByDesc(IotDiscoveredDeviceDO::getDiscoveryTime)
            .last("LIMIT 1"));
    }
    
    /**
     * 查询最近发现的设备
     */
    default List<IotDiscoveredDeviceDO> selectRecentDevices(LocalDateTime since) {
        return selectList(new LambdaQueryWrapperX<IotDiscoveredDeviceDO>()
            .ge(IotDiscoveredDeviceDO::getDiscoveryTime, since)
            .orderByDesc(IotDiscoveredDeviceDO::getDiscoveryTime));
    }
    
    /**
     * 查询未添加的设备（包括未激活的设备）
     * 
     * 注意：同时过滤 added = false 和 activated = false 的设备
     */
    default List<IotDiscoveredDeviceDO> selectUnaddedDevices() {
        return selectList(new LambdaQueryWrapperX<IotDiscoveredDeviceDO>()
            .eq(IotDiscoveredDeviceDO::getAdded, false)
            .eq(IotDiscoveredDeviceDO::getActivated, false)  // ← 新增：过滤已激活的设备
            .orderByDesc(IotDiscoveredDeviceDO::getDiscoveryTime));
    }
    
    /**
     * 根据IP查询最新的设备记录
     */
    default IotDiscoveredDeviceDO selectByIp(String ip) {
        return selectOne(new LambdaQueryWrapperX<IotDiscoveredDeviceDO>()
            .eq(IotDiscoveredDeviceDO::getIpAddress, ip)
            .orderByDesc(IotDiscoveredDeviceDO::getDiscoveryTime)
            .last("LIMIT 1"));
    }
    
    /**
     * 查询未激活的设备（24小时内发现且未激活）
     */
    default List<IotDiscoveredDeviceDO> selectUnactivatedDevices() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return selectList(new LambdaQueryWrapperX<IotDiscoveredDeviceDO>()
            .ge(IotDiscoveredDeviceDO::getDiscoveryTime, since)
            .eq(IotDiscoveredDeviceDO::getActivated, false)  // 只查未激活的
            .ne(IotDiscoveredDeviceDO::getStatus, 3)  // 排除已忽略的（状态3=已忽略）
            .orderByDesc(IotDiscoveredDeviceDO::getDiscoveryTime));
    }
}

