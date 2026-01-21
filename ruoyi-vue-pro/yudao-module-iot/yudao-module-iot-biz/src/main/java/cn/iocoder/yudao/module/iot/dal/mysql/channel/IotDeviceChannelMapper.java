package cn.iocoder.yudao.module.iot.dal.mysql.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotDeviceChannelPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 设备通道 Mapper
 *
 * @author IBMS Team
 */
@Mapper
public interface IotDeviceChannelMapper extends BaseMapperX<IotDeviceChannelDO> {

    /**
     * 分页查询通道列表
     */
    default PageResult<IotDeviceChannelDO> selectPage(IotDeviceChannelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceChannelDO>()
                .eqIfPresent(IotDeviceChannelDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotDeviceChannelDO::getDeviceType, reqVO.getDeviceType())
                .eqIfPresent(IotDeviceChannelDO::getChannelType, reqVO.getChannelType())
                .likeIfPresent(IotDeviceChannelDO::getChannelName, reqVO.getChannelName())
                .eqIfPresent(IotDeviceChannelDO::getOnlineStatus, reqVO.getOnlineStatus())
                .eqIfPresent(IotDeviceChannelDO::getEnableStatus, reqVO.getEnableStatus())
                .eqIfPresent(IotDeviceChannelDO::getBuildingId, reqVO.getBuildingId())
                .eqIfPresent(IotDeviceChannelDO::getFloorId, reqVO.getFloorId())
                .eqIfPresent(IotDeviceChannelDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(IotDeviceChannelDO::getSpaceId, reqVO.getSpaceId())
                .betweenIfPresent(IotDeviceChannelDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotDeviceChannelDO::getId));
    }

    /**
     * 查询设备的所有通道
     */
    default List<IotDeviceChannelDO> selectListByDeviceId(Long deviceId) {
        return selectList(new LambdaQueryWrapperX<IotDeviceChannelDO>()
                .eq(IotDeviceChannelDO::getDeviceId, deviceId)
                .orderByAsc(IotDeviceChannelDO::getChannelNo));
    }

    /**
     * 查询视频通道列表
     */
    default List<IotDeviceChannelDO> selectVideoChannels(String deviceType, Integer onlineStatus, Boolean isPatrol, Boolean isMonitor) {
        return selectList(new LambdaQueryWrapperX<IotDeviceChannelDO>()
                .eq(IotDeviceChannelDO::getChannelType, "VIDEO")
                .eqIfPresent(IotDeviceChannelDO::getDeviceType, deviceType)
                .eqIfPresent(IotDeviceChannelDO::getOnlineStatus, onlineStatus)
                .eqIfPresent(IotDeviceChannelDO::getIsPatrol, isPatrol != null ? (isPatrol ? 1 : 0) : null)
                .eqIfPresent(IotDeviceChannelDO::getIsMonitor, isMonitor != null ? (isMonitor ? 1 : 0) : null)
                .eq(IotDeviceChannelDO::getEnableStatus, 1)
                .orderByAsc(IotDeviceChannelDO::getSort));
    }

    /**
     * 查询巡更通道列表
     */
    default List<IotDeviceChannelDO> selectPatrolChannels() {
        return selectList(new LambdaQueryWrapperX<IotDeviceChannelDO>()
                .eq(IotDeviceChannelDO::getChannelType, "VIDEO")
                .eq(IotDeviceChannelDO::getIsPatrol, 1)
                .eq(IotDeviceChannelDO::getOnlineStatus, 1)
                .eq(IotDeviceChannelDO::getEnableStatus, 1)
                .orderByAsc(IotDeviceChannelDO::getSort));
    }

    /**
     * 查询监控墙通道列表
     */
    default List<IotDeviceChannelDO> selectMonitorChannels() {
        return selectList(new LambdaQueryWrapperX<IotDeviceChannelDO>()
                .eq(IotDeviceChannelDO::getChannelType, "VIDEO")
                .eq(IotDeviceChannelDO::getIsMonitor, 1)
                .eq(IotDeviceChannelDO::getOnlineStatus, 1)
                .eq(IotDeviceChannelDO::getEnableStatus, 1)
                .orderByAsc(IotDeviceChannelDO::getMonitorPosition));
    }

    /**
     * 检查通道号是否存在
     */
    default boolean existsByDeviceIdAndChannelNo(Long deviceId, Integer channelNo, Long excludeId) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceChannelDO>()
                .eq(IotDeviceChannelDO::getDeviceId, deviceId)
                .eq(IotDeviceChannelDO::getChannelNo, channelNo)
                .ne(excludeId != null, IotDeviceChannelDO::getId, excludeId)) > 0;
    }

    /**
     * 查询目标设备的所有通道（反向查询）
     */
    default List<IotDeviceChannelDO> selectListByTargetDeviceId(Long targetDeviceId) {
        return selectList(new LambdaQueryWrapperX<IotDeviceChannelDO>()
                .eq(IotDeviceChannelDO::getTargetDeviceId, targetDeviceId)
                .orderByAsc(IotDeviceChannelDO::getChannelNo));
    }
}
