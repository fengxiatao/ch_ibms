package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPlanPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPlanRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPlanSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.*;
import cn.iocoder.yudao.module.iot.dal.mysql.epatrol.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 电子巡更 - 巡更计划 Service 实现类
 *
 * @author 长辉信息
 */
@Slf4j
@Service
@Validated
public class EpatrolPlanServiceImpl implements EpatrolPlanService {

    @Resource
    private EpatrolPlanMapper planMapper;

    @Resource
    private EpatrolPlanPeriodMapper planPeriodMapper;

    @Resource
    private EpatrolRouteMapper routeMapper;

    @Resource
    private EpatrolPersonMapper personMapper;

    @Resource
    private EpatrolTaskMapper taskMapper;

    @Resource
    private EpatrolTaskRecordMapper taskRecordMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(EpatrolPlanSaveReqVO createReqVO) {
        // 生成计划编号
        String planCode = "JH" + LocalDate.now().format(DATE_FORMATTER) + IdUtil.getSnowflakeNextIdStr().substring(10);

        // 插入计划
        EpatrolPlanDO plan = new EpatrolPlanDO();
        plan.setPlanCode(planCode);
        plan.setPlanName(createReqVO.getPlanName());
        plan.setRouteId(createReqVO.getRouteId());
        plan.setStartDate(createReqVO.getStartDate());
        plan.setEndDate(createReqVO.getEndDate());
        plan.setWeekdays(createReqVO.getWeekdays());
        plan.setStatus(0); // 默认未开始
        plan.setRemark(createReqVO.getRemark());
        planMapper.insert(plan);

        // 插入时段
        if (CollUtil.isNotEmpty(createReqVO.getPeriods())) {
            for (EpatrolPlanSaveReqVO.PlanPeriodItem item : createReqVO.getPeriods()) {
                EpatrolPlanPeriodDO period = new EpatrolPlanPeriodDO();
                period.setPlanId(plan.getId());
                period.setRouteId(item.getRouteId());
                period.setStartTime(item.getStartTime());
                period.setDurationMinutes(item.getDurationMinutes());
                period.setPersonIds(item.getPersonIds());
                planPeriodMapper.insert(period);
            }
        }

        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlan(EpatrolPlanSaveReqVO updateReqVO) {
        // 校验存在
        validatePlanExists(updateReqVO.getId());

        // 更新计划
        EpatrolPlanDO updateObj = new EpatrolPlanDO();
        updateObj.setId(updateReqVO.getId());
        updateObj.setPlanName(updateReqVO.getPlanName());
        updateObj.setRouteId(updateReqVO.getRouteId());
        updateObj.setStartDate(updateReqVO.getStartDate());
        updateObj.setEndDate(updateReqVO.getEndDate());
        updateObj.setWeekdays(updateReqVO.getWeekdays());
        updateObj.setRemark(updateReqVO.getRemark());
        planMapper.updateById(updateObj);

        // 删除旧的时段
        List<EpatrolPlanPeriodDO> oldPeriods = planPeriodMapper.selectByPlanId(updateReqVO.getId());
        planPeriodMapper.deleteByPlanId(updateReqVO.getId());

        // 插入新的时段，并建立旧时段ID到新时段ID的映射
        Map<Long, Long> oldToNewPeriodIdMap = new HashMap<>();
        List<EpatrolPlanPeriodDO> newPeriods = new ArrayList<>();
        if (CollUtil.isNotEmpty(updateReqVO.getPeriods())) {
            for (EpatrolPlanSaveReqVO.PlanPeriodItem item : updateReqVO.getPeriods()) {
                EpatrolPlanPeriodDO period = new EpatrolPlanPeriodDO();
                period.setPlanId(updateReqVO.getId());
                period.setRouteId(item.getRouteId());
                period.setStartTime(item.getStartTime());
                period.setDurationMinutes(item.getDurationMinutes());
                period.setPersonIds(item.getPersonIds());
                planPeriodMapper.insert(period);
                newPeriods.add(period);
            }
        }

        // 删除该计划下所有未执行(status=0)的任务及其记录
        // 已执行的任务保留历史记录不受影响
        LocalDate today = LocalDate.now();
        List<EpatrolTaskDO> pendingTasks = taskMapper.selectList(new LambdaQueryWrapperX<EpatrolTaskDO>()
                .eq(EpatrolTaskDO::getPlanId, updateReqVO.getId())
                .eq(EpatrolTaskDO::getStatus, 0)  // 只删除未执行的任务
                .ge(EpatrolTaskDO::getTaskDate, today));  // 只删除今天及以后的任务
        
        if (CollUtil.isNotEmpty(pendingTasks)) {
            for (EpatrolTaskDO task : pendingTasks) {
                // 删除任务记录
                taskRecordMapper.deleteByTaskId(task.getId());
            }
            // 删除任务
            List<Long> taskIds = pendingTasks.stream().map(EpatrolTaskDO::getId).collect(Collectors.toList());
            taskMapper.deleteBatchIds(taskIds);
            log.info("[updatePlan][计划{}删除了{}条未执行的任务，将重新生成]", updateReqVO.getPlanName(), taskIds.size());
        }

        // 重新生成今天及以后的任务（如果在有效期内）
        if (today.compareTo(updateReqVO.getStartDate()) >= 0 && today.compareTo(updateReqVO.getEndDate()) <= 0) {
            regenerateTasksForPlan(updateReqVO.getId(), today);
        }
    }

    /**
     * 为计划重新生成指定日期及以后的任务
     */
    private void regenerateTasksForPlan(Long planId, LocalDate fromDate) {
        EpatrolPlanDO plan = planMapper.selectById(planId);
        if (plan == null) return;

        List<EpatrolPlanPeriodDO> periods = planPeriodMapper.selectByPlanId(planId);
        if (CollUtil.isEmpty(periods)) return;

        // 生成从 fromDate 到 plan.getEndDate() 的任务
        LocalDate currentDate = fromDate;
        while (!currentDate.isAfter(plan.getEndDate())) {
            // 检查是否在有效星期内
            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            if (plan.getWeekdays() != null && plan.getWeekdays().contains(dayOfWeek)) {
                // 为每个时段生成任务
                for (EpatrolPlanPeriodDO period : periods) {
                    generateTaskForPeriod(plan, period, currentDate);
                }
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    /**
     * 为指定时段和日期生成任务
     */
    private void generateTaskForPeriod(EpatrolPlanDO plan, EpatrolPlanPeriodDO period, LocalDate taskDate) {
        // 检查是否已存在任务
        if (taskMapper.existsByPlanIdAndPeriodIdAndDate(plan.getId(), period.getId(), taskDate)) {
            return;
        }

        // 生成任务编号：RW + 日期 + 雪花ID后缀
        String taskCode = "RW" + taskDate.format(DATE_FORMATTER) + IdUtil.getSnowflakeNextIdStr().substring(10);

        EpatrolTaskDO task = new EpatrolTaskDO();
        task.setTaskCode(taskCode);
        task.setPlanId(plan.getId());
        task.setPeriodId(period.getId());
        task.setRouteId(period.getRouteId() != null ? period.getRouteId() : plan.getRouteId());
        task.setTaskDate(taskDate);

        // 计算开始和结束时间
        LocalDateTime startTime = LocalDateTime.of(taskDate, period.getStartTime());
        LocalDateTime endTime = startTime.plusMinutes(period.getDurationMinutes());
        task.setPlannedStartTime(startTime);
        task.setPlannedEndTime(endTime);

        task.setPersonIds(period.getPersonIds());
        task.setStatus(0);

        taskMapper.insert(task);
        log.debug("[regenerateTasksForPlan][生成任务: {} 日期: {}]", task.getTaskCode(), taskDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long id) {
        // 校验存在
        validatePlanExists(id);
        // 删除时段
        planPeriodMapper.deleteByPlanId(id);
        // 删除计划
        planMapper.deleteById(id);
    }

    private void validatePlanExists(Long id) {
        if (planMapper.selectById(id) == null) {
            throw exception(EPATROL_PLAN_NOT_EXISTS);
        }
    }

    @Override
    public EpatrolPlanDO getPlan(Long id) {
        return planMapper.selectById(id);
    }

    @Override
    public EpatrolPlanRespVO getPlanDetail(Long id) {
        EpatrolPlanDO plan = planMapper.selectById(id);
        if (plan == null) {
            return null;
        }

        EpatrolPlanRespVO respVO = BeanUtils.toBean(plan, EpatrolPlanRespVO.class);

        // 获取路线名称
        EpatrolRouteDO route = routeMapper.selectById(plan.getRouteId());
        if (route != null) {
            respVO.setRouteName(route.getRouteName());
        }

        // 获取时段列表
        List<EpatrolPlanPeriodDO> periods = planPeriodMapper.selectByPlanId(id);
        if (CollUtil.isNotEmpty(periods)) {
            // 获取所有涉及的人员ID
            List<Long> allPersonIds = new ArrayList<>();
            // 获取所有涉及的路线ID
            List<Long> allRouteIds = new ArrayList<>();
            for (EpatrolPlanPeriodDO period : periods) {
                if (CollUtil.isNotEmpty(period.getPersonIds())) {
                    allPersonIds.addAll(period.getPersonIds());
                }
                if (period.getRouteId() != null) {
                    allRouteIds.add(period.getRouteId());
                }
            }

            // 获取人员信息
            Map<Long, String> personNameMap = CollUtil.isEmpty(allPersonIds) ? Map.of() :
                    personMapper.selectBatchIds(allPersonIds).stream()
                            .collect(Collectors.toMap(EpatrolPersonDO::getId, EpatrolPersonDO::getName));

            // 获取路线信息
            Map<Long, String> routeNameMap = CollUtil.isEmpty(allRouteIds) ? Map.of() :
                    routeMapper.selectBatchIds(allRouteIds.stream().distinct().collect(Collectors.toList())).stream()
                            .collect(Collectors.toMap(EpatrolRouteDO::getId, EpatrolRouteDO::getRouteName));

            List<EpatrolPlanRespVO.PlanPeriodRespVO> periodRespVOs = new ArrayList<>();
            List<String> allPersonNames = new ArrayList<>();
            List<String> allRouteNames = new ArrayList<>();

            for (EpatrolPlanPeriodDO period : periods) {
                EpatrolPlanRespVO.PlanPeriodRespVO periodResp = new EpatrolPlanRespVO.PlanPeriodRespVO();
                periodResp.setId(period.getId());
                periodResp.setRouteId(period.getRouteId());
                periodResp.setStartTime(period.getStartTime());
                periodResp.setEndTime(period.getStartTime().plusMinutes(period.getDurationMinutes()));
                periodResp.setDurationMinutes(period.getDurationMinutes());
                periodResp.setPersonIds(period.getPersonIds());

                // 获取路线名称
                if (period.getRouteId() != null) {
                    String routeName = routeNameMap.get(period.getRouteId());
                    periodResp.setRouteName(routeName);
                    if (routeName != null) {
                        allRouteNames.add(routeName);
                    }
                }

                // 获取人员姓名
                if (CollUtil.isNotEmpty(period.getPersonIds())) {
                    List<String> names = period.getPersonIds().stream()
                            .map(personNameMap::get)
                            .filter(name -> name != null)
                            .collect(Collectors.toList());
                    periodResp.setPersonNames(names);
                    allPersonNames.addAll(names);
                }

                periodRespVOs.add(periodResp);
            }

            respVO.setPeriods(periodRespVOs);
            respVO.setPersonNames(allPersonNames.stream().distinct().collect(Collectors.toList()));
            // 设置所有路线名称
            respVO.setRouteNames(allRouteNames.stream().distinct().collect(Collectors.toList()));

            // 生成执行时间段描述
            if (!periodRespVOs.isEmpty()) {
                String timeDesc = periodRespVOs.stream()
                        .map(p -> p.getStartTime().toString() + "-" + p.getEndTime().toString())
                        .collect(Collectors.joining(", "));
                respVO.setTimeRangeDesc(timeDesc);
            }
        }

        return respVO;
    }

    @Override
    public PageResult<EpatrolPlanDO> getPlanPage(EpatrolPlanPageReqVO pageReqVO) {
        // 自动更新计划状态
        updateAllPlanStatus();
        
        // 如果有人员ID搜索条件，先查询包含该人员的计划ID
        if (pageReqVO.getPersonId() != null) {
            // 使用SQL查询包含该人员的时段
            List<EpatrolPlanPeriodDO> periods = planPeriodMapper.selectListByPersonId(pageReqVO.getPersonId());
            if (CollUtil.isEmpty(periods)) {
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            // 获取计划ID列表
            List<Long> planIds = periods.stream().map(EpatrolPlanPeriodDO::getPlanId).distinct().collect(Collectors.toList());
            // 在这些计划中分页查询
            return planMapper.selectPage(pageReqVO, new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<EpatrolPlanDO>()
                    .in(EpatrolPlanDO::getId, planIds)
                    .likeIfPresent(EpatrolPlanDO::getPlanCode, pageReqVO.getPlanCode())
                    .likeIfPresent(EpatrolPlanDO::getPlanName, pageReqVO.getPlanName())
                    .eqIfPresent(EpatrolPlanDO::getRouteId, pageReqVO.getRouteId())
                    .eqIfPresent(EpatrolPlanDO::getStatus, pageReqVO.getStatus())
                    .orderByDesc(EpatrolPlanDO::getId));
        }
        return planMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<EpatrolPlanRespVO> getPlanPageWithDetails(EpatrolPlanPageReqVO pageReqVO) {
        PageResult<EpatrolPlanDO> pageResult = getPlanPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(new ArrayList<>(), pageResult.getTotal());
        }

        // 获取所有计划ID
        List<Long> planIds = pageResult.getList().stream().map(EpatrolPlanDO::getId).collect(Collectors.toList());

        // 批量获取所有时段
        List<EpatrolPlanPeriodDO> allPeriods = new ArrayList<>();
        for (Long planId : planIds) {
            allPeriods.addAll(planPeriodMapper.selectByPlanId(planId));
        }

        // 获取所有涉及的人员ID和路线ID
        List<Long> allPersonIds = new ArrayList<>();
        List<Long> allRouteIds = new ArrayList<>();
        for (EpatrolPlanPeriodDO period : allPeriods) {
            if (CollUtil.isNotEmpty(period.getPersonIds())) {
                allPersonIds.addAll(period.getPersonIds());
            }
            if (period.getRouteId() != null) {
                allRouteIds.add(period.getRouteId());
            }
        }

        // 获取人员和路线映射
        Map<Long, String> personNameMap = CollUtil.isEmpty(allPersonIds) ? Map.of() :
                personMapper.selectBatchIds(allPersonIds.stream().distinct().collect(Collectors.toList())).stream()
                        .collect(Collectors.toMap(EpatrolPersonDO::getId, EpatrolPersonDO::getName));
        Map<Long, String> routeNameMap = CollUtil.isEmpty(allRouteIds) ? Map.of() :
                routeMapper.selectBatchIds(allRouteIds.stream().distinct().collect(Collectors.toList())).stream()
                        .collect(Collectors.toMap(EpatrolRouteDO::getId, EpatrolRouteDO::getRouteName));

        // 按计划ID分组时段
        Map<Long, List<EpatrolPlanPeriodDO>> periodsByPlanId = allPeriods.stream()
                .collect(Collectors.groupingBy(EpatrolPlanPeriodDO::getPlanId));

        // 构建响应
        List<EpatrolPlanRespVO> respList = new ArrayList<>();
        for (EpatrolPlanDO plan : pageResult.getList()) {
            EpatrolPlanRespVO respVO = BeanUtils.toBean(plan, EpatrolPlanRespVO.class);

            List<EpatrolPlanPeriodDO> periods = periodsByPlanId.getOrDefault(plan.getId(), new ArrayList<>());
            List<String> personNames = new ArrayList<>();
            List<String> routeNames = new ArrayList<>();

            for (EpatrolPlanPeriodDO period : periods) {
                if (CollUtil.isNotEmpty(period.getPersonIds())) {
                    period.getPersonIds().forEach(pid -> {
                        String name = personNameMap.get(pid);
                        if (name != null && !personNames.contains(name)) {
                            personNames.add(name);
                        }
                    });
                }
                if (period.getRouteId() != null) {
                    String routeName = routeNameMap.get(period.getRouteId());
                    if (routeName != null && !routeNames.contains(routeName)) {
                        routeNames.add(routeName);
                    }
                }
            }

            respVO.setPersonNames(personNames);
            respVO.setRouteNames(routeNames);
            respList.add(respVO);
        }

        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    public List<EpatrolPlanDO> getActivePlanList() {
        return planMapper.selectListByStatus(1);
    }

    @Override
    public void updatePlanStatus(Long id, Integer status) {
        // 校验存在
        validatePlanExists(id);
        // 更新状态
        EpatrolPlanDO updateObj = new EpatrolPlanDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        planMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateDailyTasks() {
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue(); // 1=周一

        log.info("[generateDailyTasks][开始生成{}的巡更任务，星期{}]", today, dayOfWeek);

        // 获取所有执行中的计划
        List<EpatrolPlanDO> plans = planMapper.selectActivePlans(today);

        for (EpatrolPlanDO plan : plans) {
            // 检查今天是否在计划的星期范围内
            if (!plan.getWeekdays().contains(dayOfWeek)) {
                continue;
            }

            // 获取计划的所有时段
            List<EpatrolPlanPeriodDO> periods = planPeriodMapper.selectByPlanId(plan.getId());

            for (EpatrolPlanPeriodDO period : periods) {
                // 检查是否已经生成过该任务
                if (taskMapper.existsByPlanIdAndPeriodIdAndDate(plan.getId(), period.getId(), today)) {
                    continue;
                }

                // 生成任务编号
                String taskCode = "RW" + today.format(DATE_FORMATTER) + IdUtil.getSnowflakeNextIdStr().substring(10);

                // 计算计划开始和结束时间
                LocalDateTime plannedStartTime = LocalDateTime.of(today, period.getStartTime());
                LocalDateTime plannedEndTime = plannedStartTime.plusMinutes(period.getDurationMinutes());

                // 创建任务（优先使用时段的 routeId，如果没有则使用计划的 routeId）
                EpatrolTaskDO task = new EpatrolTaskDO();
                task.setTaskCode(taskCode);
                task.setPlanId(plan.getId());
                task.setPeriodId(period.getId());
                task.setRouteId(period.getRouteId() != null ? period.getRouteId() : plan.getRouteId());
                task.setTaskDate(today);
                task.setPlannedStartTime(plannedStartTime);
                task.setPlannedEndTime(plannedEndTime);
                task.setPersonIds(period.getPersonIds());
                task.setStatus(0); // 未巡
                taskMapper.insert(task);

                log.info("[generateDailyTasks][生成任务: {}, 计划: {}, 时段: {}-{}]",
                        taskCode, plan.getPlanName(), period.getStartTime(),
                        period.getStartTime().plusMinutes(period.getDurationMinutes()));
            }
        }

        log.info("[generateDailyTasks][{}的巡更任务生成完成]", today);
    }

    /**
     * 自动更新所有计划的状态
     * 根据当前日期和计划的开始/结束日期自动计算状态：
     * - 0: 未开始 (当前日期 < 开始日期)
     * - 1: 执行中 (开始日期 <= 当前日期 <= 结束日期)
     * - 2: 已过期 (当前日期 > 结束日期)
     */
    @Override
    public void updateAllPlanStatus() {
        LocalDate today = LocalDate.now();
        List<EpatrolPlanDO> allPlans = planMapper.selectList();
        
        for (EpatrolPlanDO plan : allPlans) {
            int newStatus;
            if (plan.getStartDate() != null && today.isBefore(plan.getStartDate())) {
                newStatus = 0; // 未开始
            } else if (plan.getEndDate() != null && today.isAfter(plan.getEndDate())) {
                newStatus = 2; // 已过期
            } else {
                newStatus = 1; // 执行中
            }
            
            // 只有状态变化时才更新
            if (plan.getStatus() == null || plan.getStatus() != newStatus) {
                EpatrolPlanDO updateObj = new EpatrolPlanDO();
                updateObj.setId(plan.getId());
                updateObj.setStatus(newStatus);
                planMapper.updateById(updateObj);
                log.debug("[updateAllPlanStatus][计划 {} 状态从 {} 更新为 {}]", 
                        plan.getPlanName(), plan.getStatus(), newStatus);
            }
        }
    }

}
