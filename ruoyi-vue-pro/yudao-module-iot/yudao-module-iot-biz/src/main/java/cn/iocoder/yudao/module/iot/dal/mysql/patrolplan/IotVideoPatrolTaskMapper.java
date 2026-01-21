package cn.iocoder.yudao.module.iot.dal.mysql.patrolplan;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolTaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 轮巡任务 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface IotVideoPatrolTaskMapper extends BaseMapperX<IotVideoPatrolTaskDO> {

    default List<IotVideoPatrolTaskDO> selectListByPlanId(Long planId) {
        return selectList(new LambdaQueryWrapperX<IotVideoPatrolTaskDO>()
                .eq(IotVideoPatrolTaskDO::getPlanId, planId)
                .orderByAsc(IotVideoPatrolTaskDO::getSort)
                .orderByDesc(IotVideoPatrolTaskDO::getId));
    }

}
