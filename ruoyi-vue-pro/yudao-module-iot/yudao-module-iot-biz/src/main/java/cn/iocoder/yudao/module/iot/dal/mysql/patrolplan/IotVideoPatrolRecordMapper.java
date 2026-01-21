package cn.iocoder.yudao.module.iot.dal.mysql.patrolplan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.record.PatrolRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 轮巡执行记录 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface IotVideoPatrolRecordMapper extends BaseMapperX<IotVideoPatrolRecordDO> {

    default PageResult<IotVideoPatrolRecordDO> selectPage(PatrolRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotVideoPatrolRecordDO>()
                .eqIfPresent(IotVideoPatrolRecordDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(IotVideoPatrolRecordDO::getTaskId, reqVO.getTaskId())
                .eqIfPresent(IotVideoPatrolRecordDO::getSceneId, reqVO.getSceneId())
                .eqIfPresent(IotVideoPatrolRecordDO::getIsAbnormal, reqVO.getHasAbnormal())
                .eqIfPresent(IotVideoPatrolRecordDO::getHandled, reqVO.getHandled())
                .betweenIfPresent(IotVideoPatrolRecordDO::getStartTime, reqVO.getExecuteTime())
                .orderByDesc(IotVideoPatrolRecordDO::getStartTime));
    }

}
