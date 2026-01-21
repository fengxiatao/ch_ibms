package cn.iocoder.yudao.module.iot.dal.mysql.patrolplan;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolSceneDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 轮巡场景 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface IotVideoPatrolSceneMapper extends BaseMapperX<IotVideoPatrolSceneDO> {

    default List<IotVideoPatrolSceneDO> selectListByTaskId(Long taskId) {
        return selectList(new LambdaQueryWrapperX<IotVideoPatrolSceneDO>()
                .eq(IotVideoPatrolSceneDO::getTaskId, taskId)
                .orderByAsc(IotVideoPatrolSceneDO::getSceneOrder)
                .orderByDesc(IotVideoPatrolSceneDO::getId));
    }

}
