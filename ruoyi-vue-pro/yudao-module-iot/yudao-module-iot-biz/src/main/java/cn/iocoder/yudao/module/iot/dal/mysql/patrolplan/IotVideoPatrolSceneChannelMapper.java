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

}
