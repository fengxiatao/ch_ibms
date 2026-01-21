package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDeviceAuthDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 人员设备授权状态 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessPersonDeviceAuthMapper extends BaseMapperX<IotAccessPersonDeviceAuthDO> {

    /**
     * 根据人员ID查询授权状态列表
     */
    default List<IotAccessPersonDeviceAuthDO> selectListByPersonId(Long personId) {
        return selectList(new LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO>()
                .eq(IotAccessPersonDeviceAuthDO::getPersonId, personId));
    }

    /**
     * 根据设备ID查询授权状态列表
     */
    default List<IotAccessPersonDeviceAuthDO> selectListByDeviceId(Long deviceId) {
        return selectList(new LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO>()
                .eq(IotAccessPersonDeviceAuthDO::getDeviceId, deviceId));
    }

    /**
     * 根据人员ID和设备ID查询授权状态
     */
    default IotAccessPersonDeviceAuthDO selectByPersonIdAndDeviceId(Long personId, Long deviceId, Long channelId) {
        return selectOne(new LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO>()
                .eq(IotAccessPersonDeviceAuthDO::getPersonId, personId)
                .eq(IotAccessPersonDeviceAuthDO::getDeviceId, deviceId)
                .eqIfPresent(IotAccessPersonDeviceAuthDO::getChannelId, channelId));
    }

    /**
     * 根据人员ID、设备ID和通道ID查询授权状态
     */
    default IotAccessPersonDeviceAuthDO selectByPersonIdAndDeviceIdAndChannelId(Long personId, Long deviceId, Long channelId) {
        return selectOne(new LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO>()
                .eq(IotAccessPersonDeviceAuthDO::getPersonId, personId)
                .eq(IotAccessPersonDeviceAuthDO::getDeviceId, deviceId)
                .eqIfPresent(IotAccessPersonDeviceAuthDO::getChannelId, channelId));
    }

    /**
     * 根据人员ID删除授权状态
     */
    default int deleteByPersonId(Long personId) {
        return delete(new LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO>()
                .eq(IotAccessPersonDeviceAuthDO::getPersonId, personId));
    }

    /**
     * 根据设备ID删除授权状态
     */
    default int deleteByDeviceId(Long deviceId) {
        return delete(new LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO>()
                .eq(IotAccessPersonDeviceAuthDO::getDeviceId, deviceId));
    }

    /**
     * 根据授权状态查询列表
     */
    default List<IotAccessPersonDeviceAuthDO> selectListByAuthStatus(Integer authStatus) {
        return selectList(new LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO>()
                .eq(IotAccessPersonDeviceAuthDO::getAuthStatus, authStatus));
    }

    /**
     * 根据设备ID和授权状态查询列表
     * 用于查询设备上待撤销的人员
     */
    default List<IotAccessPersonDeviceAuthDO> selectListByDeviceIdAndAuthStatus(Long deviceId, Integer authStatus) {
        return selectList(new LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO>()
                .eq(IotAccessPersonDeviceAuthDO::getDeviceId, deviceId)
                .eq(IotAccessPersonDeviceAuthDO::getAuthStatus, authStatus));
    }

    /**
     * 根据人员ID列表查询授权状态
     */
    default List<IotAccessPersonDeviceAuthDO> selectListByPersonIds(List<Long> personIds) {
        return selectList(new LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO>()
                .in(IotAccessPersonDeviceAuthDO::getPersonId, personIds));
    }

    /**
     * 根据人员ID列表和授权状态查询
     */
    default List<IotAccessPersonDeviceAuthDO> selectListByPersonIdsAndAuthStatus(List<Long> personIds, Integer authStatus) {
        return selectList(new LambdaQueryWrapperX<IotAccessPersonDeviceAuthDO>()
                .in(IotAccessPersonDeviceAuthDO::getPersonId, personIds)
                .eq(IotAccessPersonDeviceAuthDO::getAuthStatus, authStatus));
    }

}
