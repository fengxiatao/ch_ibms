package cn.iocoder.yudao.module.iot.dal.mysql.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolTaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 电子巡更 - 巡更任务 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface EpatrolTaskMapper extends BaseMapperX<EpatrolTaskDO> {

    default PageResult<EpatrolTaskDO> selectPage(EpatrolTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EpatrolTaskDO>()
                .likeIfPresent(EpatrolTaskDO::getTaskCode, reqVO.getTaskCode())
                .eqIfPresent(EpatrolTaskDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(EpatrolTaskDO::getRouteId, reqVO.getRouteId())
                .eqIfPresent(EpatrolTaskDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(EpatrolTaskDO::getTaskDate, reqVO.getTaskDateStart(), reqVO.getTaskDateEnd())
                .orderByDesc(EpatrolTaskDO::getTaskDate)
                .orderByDesc(EpatrolTaskDO::getId));
    }

    default EpatrolTaskDO selectByTaskCode(String taskCode) {
        return selectOne(EpatrolTaskDO::getTaskCode, taskCode);
    }

    default List<EpatrolTaskDO> selectByTaskDate(LocalDate taskDate) {
        return selectList(new LambdaQueryWrapperX<EpatrolTaskDO>()
                .eq(EpatrolTaskDO::getTaskDate, taskDate)
                .orderByAsc(EpatrolTaskDO::getPlannedStartTime));
    }

    default boolean existsByPlanIdAndPeriodIdAndDate(Long planId, Long periodId, LocalDate taskDate) {
        return exists(new LambdaQueryWrapperX<EpatrolTaskDO>()
                .eq(EpatrolTaskDO::getPlanId, planId)
                .eq(EpatrolTaskDO::getPeriodId, periodId)
                .eq(EpatrolTaskDO::getTaskDate, taskDate));
    }

}
