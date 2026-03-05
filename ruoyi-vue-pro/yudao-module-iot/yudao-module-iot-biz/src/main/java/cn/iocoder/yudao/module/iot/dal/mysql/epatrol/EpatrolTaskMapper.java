package cn.iocoder.yudao.module.iot.dal.mysql.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolTaskDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 电子巡更 - 巡更任务 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface EpatrolTaskMapper extends BaseMapperX<EpatrolTaskDO> {

    /**
     * 分页查询任务
     * 排序规则：今天的任务优先，然后按日期降序
     */
    default PageResult<EpatrolTaskDO> selectPage(EpatrolTaskPageReqVO reqVO) {
        LocalDate today = LocalDate.now();
        return selectPage(reqVO, new LambdaQueryWrapperX<EpatrolTaskDO>()
                .likeIfPresent(EpatrolTaskDO::getTaskCode, reqVO.getTaskCode())
                .eqIfPresent(EpatrolTaskDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(EpatrolTaskDO::getRouteId, reqVO.getRouteId())
                .eqIfPresent(EpatrolTaskDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(EpatrolTaskDO::getTaskDate, reqVO.getTaskDateStart(), reqVO.getTaskDateEnd())
                // 今天的任务优先（使用 CASE WHEN 排序）
                .last("ORDER BY CASE WHEN task_date = '" + today + "' THEN 0 ELSE 1 END, task_date DESC, id DESC"));
    }

    /**
     * 按人员ID分页查询任务（person_ids 是 JSON 数组）
     * 排序规则：今天的任务优先，然后按日期降序
     */
    default PageResult<EpatrolTaskDO> selectPageByPersonId(EpatrolTaskPageReqVO reqVO) {
        LocalDate today = LocalDate.now();
        // 使用 apply 添加 JSON_CONTAINS 条件
        return selectPage(reqVO, new LambdaQueryWrapperX<EpatrolTaskDO>()
                .likeIfPresent(EpatrolTaskDO::getTaskCode, reqVO.getTaskCode())
                .eqIfPresent(EpatrolTaskDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(EpatrolTaskDO::getRouteId, reqVO.getRouteId())
                .eqIfPresent(EpatrolTaskDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(EpatrolTaskDO::getTaskDate, reqVO.getTaskDateStart(), reqVO.getTaskDateEnd())
                .apply(reqVO.getPersonId() != null, "JSON_CONTAINS(person_ids, {0})", reqVO.getPersonId())
                // 今天的任务优先（使用 CASE WHEN 排序）
                .last("ORDER BY CASE WHEN task_date = '" + today + "' THEN 0 ELSE 1 END, task_date DESC, id DESC"));
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
