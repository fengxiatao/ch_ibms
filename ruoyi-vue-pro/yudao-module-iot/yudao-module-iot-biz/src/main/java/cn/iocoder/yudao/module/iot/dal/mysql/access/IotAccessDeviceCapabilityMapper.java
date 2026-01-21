package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessDeviceCapabilityDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备能力缓存 Mapper
 * 
 * @author Kiro
 */
@Mapper
public interface IotAccessDeviceCapabilityMapper extends BaseMapperX<IotAccessDeviceCapabilityDO> {

    /**
     * 根据设备ID查询能力缓存
     */
    default IotAccessDeviceCapabilityDO selectByDeviceId(Long deviceId) {
        return selectOne(IotAccessDeviceCapabilityDO::getDeviceId, deviceId);
    }
    
    /**
     * 根据设备ID删除能力缓存
     */
    default int deleteByDeviceId(Long deviceId) {
        return delete(IotAccessDeviceCapabilityDO::getDeviceId, deviceId);
    }
}
