package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupDeviceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门禁权限组设备关联 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessPermissionGroupDeviceMapper extends BaseMapperX<IotAccessPermissionGroupDeviceDO> {

    default List<IotAccessPermissionGroupDeviceDO> selectListByGroupId(Long groupId) {
        return selectList(IotAccessPermissionGroupDeviceDO::getGroupId, groupId);
    }

    default List<IotAccessPermissionGroupDeviceDO> selectListByDeviceId(Long deviceId) {
        return selectList(IotAccessPermissionGroupDeviceDO::getDeviceId, deviceId);
    }

    default void deleteByGroupId(Long groupId) {
        delete(IotAccessPermissionGroupDeviceDO::getGroupId, groupId);
    }

    default IotAccessPermissionGroupDeviceDO selectByGroupIdAndDeviceIdAndChannelId(Long groupId, Long deviceId, Long channelId) {
        return selectOne(new LambdaQueryWrapperX<IotAccessPermissionGroupDeviceDO>()
                .eq(IotAccessPermissionGroupDeviceDO::getGroupId, groupId)
                .eq(IotAccessPermissionGroupDeviceDO::getDeviceId, deviceId)
                .eqIfPresent(IotAccessPermissionGroupDeviceDO::getChannelId, channelId));
    }

    default Long selectCountByGroupId(Long groupId) {
        return selectCount(IotAccessPermissionGroupDeviceDO::getGroupId, groupId);
    }

    /**
     * 物理删除指定权限组、设备和通道的关联记录
     * 关联表不需要保留软删除记录，使用物理删除避免唯一约束冲突
     *
     * @param groupId 权限组ID
     * @param deviceId 设备ID
     * @param channelId 通道ID（可为null）
     * @return 删除的记录数
     */
    @org.apache.ibatis.annotations.Delete("<script>" +
            "DELETE FROM iot_access_permission_group_device WHERE group_id = #{groupId} AND device_id = #{deviceId}" +
            "<if test='channelId != null'> AND channel_id = #{channelId}</if>" +
            "<if test='channelId == null'> AND channel_id IS NULL</if>" +
            "</script>")
    int physicalDeleteByGroupIdAndDeviceIdAndChannelId(@org.apache.ibatis.annotations.Param("groupId") Long groupId,
                                                        @org.apache.ibatis.annotations.Param("deviceId") Long deviceId,
                                                        @org.apache.ibatis.annotations.Param("channelId") Long channelId);

}
