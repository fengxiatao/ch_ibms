package cn.iocoder.yudao.module.iot.dal.mysql.patrolplan;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolSceneChannelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 轮巡场景通道 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface IotVideoPatrolSceneChannelMapper extends BaseMapperX<IotVideoPatrolSceneChannelDO> {

    default List<IotVideoPatrolSceneChannelDO> selectListBySceneId(Long sceneId) {
        return selectList(new LambdaQueryWrapperX<IotVideoPatrolSceneChannelDO>()
                .eq(IotVideoPatrolSceneChannelDO::getSceneId, sceneId)
                .orderByAsc(IotVideoPatrolSceneChannelDO::getGridPosition));
    }

    /**
     * 根据场景ID删除场景通道
     */
    default int deleteBySceneId(Long sceneId) {
        return delete(new LambdaQueryWrapperX<IotVideoPatrolSceneChannelDO>()
                .eq(IotVideoPatrolSceneChannelDO::getSceneId, sceneId));
    }

    /**
     * 根据场景ID列表批量删除场景通道
     */
    default int deleteBySceneIds(java.util.Collection<Long> sceneIds) {
        if (sceneIds == null || sceneIds.isEmpty()) {
            return 0;
        }
        return delete(new LambdaQueryWrapperX<IotVideoPatrolSceneChannelDO>()
                .in(IotVideoPatrolSceneChannelDO::getSceneId, sceneIds));
    }

}
