package cn.iocoder.yudao.module.iot.dal.mysql.patrolplan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.plan.PatrolPlanPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolPlanDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 轮巡计划 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface IotVideoPatrolPlanMapper extends BaseMapperX<IotVideoPatrolPlanDO> {

    default PageResult<IotVideoPatrolPlanDO> selectPage(PatrolPlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotVideoPatrolPlanDO>()
                .likeIfPresent(IotVideoPatrolPlanDO::getPlanName, reqVO.getPlanName())
                .likeIfPresent(IotVideoPatrolPlanDO::getPlanCode, reqVO.getPlanCode())
                .eqIfPresent(IotVideoPatrolPlanDO::getStatus, reqVO.getStatus())
                .eqIfPresent(IotVideoPatrolPlanDO::getRunningStatus, reqVO.getRunningStatus())
                .betweenIfPresent(IotVideoPatrolPlanDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotVideoPatrolPlanDO::getSort)
                .orderByDesc(IotVideoPatrolPlanDO::getId));
    }

}
