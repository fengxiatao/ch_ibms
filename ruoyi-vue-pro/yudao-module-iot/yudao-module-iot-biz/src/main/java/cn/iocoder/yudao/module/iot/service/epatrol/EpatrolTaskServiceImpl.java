package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskStatisticsVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskSubmitReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.*;
import cn.iocoder.yudao.module.iot.dal.mysql.epatrol.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 电子巡更 - 巡更任务 Service 实现类
 *
 * @author 长辉信息
 */
@Slf4j
@Service
@Validated
public class EpatrolTaskServiceImpl implements EpatrolTaskService {

    @Resource
    private EpatrolTaskMapper taskMapper;

    @Resource
    private EpatrolTaskRecordMapper taskRecordMapper;

    @Resource
    private EpatrolPlanMapper planMapper;

    @Resource
    private EpatrolRouteMapper routeMapper;

    @Resource
    private EpatrolRoutePointMapper routePointMapper;

    @Resource
    private EpatrolPointMapper pointMapper;

    @Resource
    private EpatrolPersonMapper personMapper;

    // 巡更状态常量
    private static final int PATROL_STATUS_ON_TIME = 1;    // 准时
    private static final int PATROL_STATUS_EARLY = 2;      // 早到
    private static final int PATROL_STATUS_LATE = 3;       // 晚到
    private static final int PATROL_STATUS_MISSED = 4;     // 未到
    private static final int PATROL_STATUS_WRONG_ORDER = 5; // 顺序错

    // 巡更状态描述
    private static final Map<Integer, String> PATROL_STATUS_DESC_MAP = Map.of(
            PATROL_STATUS_ON_TIME, "准时",
            PATROL_STATUS_EARLY, "早到",
            PATROL_STATUS_LATE, "晚到",
            PATROL_STATUS_MISSED, "未到",
            PATROL_STATUS_WRONG_ORDER, "顺序错"
    );

    // 准时的时间容差（秒）
    private static final int ON_TIME_TOLERANCE_SECONDS = 300; // 5分钟

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public EpatrolTaskDO getTask(Long id) {
        return taskMapper.selectById(id);
    }

    @Override
    public EpatrolTaskRespVO getTaskDetail(Long id) {
        EpatrolTaskDO task = taskMapper.selectById(id);
        if (task == null) {
            return null;
        }

        EpatrolTaskRespVO respVO = BeanUtils.toBean(task, EpatrolTaskRespVO.class);

        // 获取计划名称
        EpatrolPlanDO plan = planMapper.selectById(task.getPlanId());
        if (plan != null) {
            respVO.setPlanName(plan.getPlanName());
        }

        // 获取路线名称
        EpatrolRouteDO route = routeMapper.selectById(task.getRouteId());
        if (route != null) {
            respVO.setRouteName(route.getRouteName());
        }

        // 获取人员姓名
        if (CollUtil.isNotEmpty(task.getPersonIds())) {
            List<EpatrolPersonDO> persons = personMapper.selectBatchIds(task.getPersonIds());
            List<String> names = persons.stream().map(EpatrolPersonDO::getName).collect(Collectors.toList());
            respVO.setPersonNames(names);
        }

        // 生成计划时间描述
        respVO.setPlannedTimeDesc(task.getPlannedStartTime().format(TIME_FORMATTER) + "-" +
                task.getPlannedEndTime().format(TIME_FORMATTER));

        // 获取任务记录
        List<EpatrolTaskRecordDO> records = taskRecordMapper.selectByTaskId(id);
        if (CollUtil.isNotEmpty(records)) {
            List<EpatrolTaskRespVO.TaskRecordRespVO> recordRespVOs = new ArrayList<>();
            for (EpatrolTaskRecordDO record : records) {
                EpatrolTaskRespVO.TaskRecordRespVO recordResp = BeanUtils.toBean(record, EpatrolTaskRespVO.TaskRecordRespVO.class);
                recordResp.setPatrolStatusDesc(PATROL_STATUS_DESC_MAP.get(record.getPatrolStatus()));
                recordRespVOs.add(recordResp);
            }
            respVO.setRecords(recordRespVOs);
        }

        return respVO;
    }

    @Override
    public PageResult<EpatrolTaskDO> getTaskPage(EpatrolTaskPageReqVO pageReqVO) {
        // 如果有人员ID搜索条件，使用自定义查询
        if (pageReqVO.getPersonId() != null) {
            return taskMapper.selectPageByPersonId(pageReqVO);
        }
        return taskMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<EpatrolTaskRespVO> getTaskPageWithDetails(EpatrolTaskPageReqVO pageReqVO) {
        PageResult<EpatrolTaskDO> pageResult = getTaskPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(new ArrayList<>(), pageResult.getTotal());
        }

        // 收集所有需要的ID
        Set<Long> planIds = new HashSet<>();
        Set<Long> routeIds = new HashSet<>();
        Set<Long> allPersonIds = new HashSet<>();
        for (EpatrolTaskDO task : pageResult.getList()) {
            if (task.getPlanId() != null) planIds.add(task.getPlanId());
            if (task.getRouteId() != null) routeIds.add(task.getRouteId());
            if (CollUtil.isNotEmpty(task.getPersonIds())) allPersonIds.addAll(task.getPersonIds());
        }

        // 批量获取关联数据
        Map<Long, EpatrolPlanDO> planMap = CollUtil.isEmpty(planIds) ? Map.of() :
                planMapper.selectBatchIds(planIds).stream()
                        .collect(Collectors.toMap(EpatrolPlanDO::getId, p -> p));
        Map<Long, String> routeNameMap = CollUtil.isEmpty(routeIds) ? Map.of() :
                routeMapper.selectBatchIds(routeIds).stream()
                        .collect(Collectors.toMap(EpatrolRouteDO::getId, EpatrolRouteDO::getRouteName));
        Map<Long, String> personNameMap = CollUtil.isEmpty(allPersonIds) ? Map.of() :
                personMapper.selectBatchIds(allPersonIds).stream()
                        .collect(Collectors.toMap(EpatrolPersonDO::getId, EpatrolPersonDO::getName));

        // 构建响应
        List<EpatrolTaskRespVO> respList = new ArrayList<>();
        for (EpatrolTaskDO task : pageResult.getList()) {
            EpatrolTaskRespVO respVO = BeanUtils.toBean(task, EpatrolTaskRespVO.class);
            
            // 设置计划名称和编号
            if (task.getPlanId() != null) {
                EpatrolPlanDO plan = planMap.get(task.getPlanId());
                if (plan != null) {
                    respVO.setPlanName(plan.getPlanName());
                    respVO.setPlanCode(plan.getPlanCode());
                }
            }
            
            // 设置路线名称
            if (task.getRouteId() != null) {
                respVO.setRouteName(routeNameMap.get(task.getRouteId()));
            }
            
            // 设置人员名称
            if (CollUtil.isNotEmpty(task.getPersonIds())) {
                List<String> names = task.getPersonIds().stream()
                        .map(personNameMap::get)
                        .filter(name -> name != null)
                        .collect(Collectors.toList());
                respVO.setPersonNames(names);
                respVO.setPersonName(String.join("、", names));
            }
            
            // 设置计划时间描述
            if (task.getPlannedStartTime() != null && task.getPlannedEndTime() != null) {
                respVO.setPlannedTimeDesc(task.getPlannedStartTime().format(TIME_FORMATTER) + "-" +
                        task.getPlannedEndTime().format(TIME_FORMATTER));
            }
            
            respList.add(respVO);
        }

        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTask(EpatrolTaskSubmitReqVO submitReqVO) {
        // 校验任务存在
        EpatrolTaskDO task = taskMapper.selectById(submitReqVO.getTaskId());
        if (task == null) {
            throw exception(EPATROL_TASK_NOT_EXISTS);
        }

        // 校验任务状态
        if (task.getStatus() == 1) {
            throw exception(EPATROL_TASK_ALREADY_SUBMITTED);
        }

        // 获取路线点位信息
        List<EpatrolRoutePointDO> routePoints = routePointMapper.selectByRouteId(task.getRouteId());
        Map<Long, EpatrolRoutePointDO> routePointMap = routePoints.stream()
                .collect(Collectors.toMap(EpatrolRoutePointDO::getPointId, rp -> rp));

        // 获取所有点位
        List<Long> pointIds = routePoints.stream().map(EpatrolRoutePointDO::getPointId).collect(Collectors.toList());
        List<EpatrolPointDO> points = pointMapper.selectBatchIds(pointIds);
        Map<String, EpatrolPointDO> pointNoMap = points.stream()
                .collect(Collectors.toMap(EpatrolPointDO::getPointNo, p -> p));
        Map<Long, EpatrolPointDO> pointIdMap = points.stream()
                .collect(Collectors.toMap(EpatrolPointDO::getId, p -> p));

        // 删除旧的记录（如果有）
        taskRecordMapper.deleteByTaskId(submitReqVO.getTaskId());

        // 计算每个点位的预期到达时间
        Map<Long, LocalDateTime> expectedTimeMap = new HashMap<>();
        LocalDateTime currentTime = task.getPlannedStartTime();
        for (EpatrolRoutePointDO rp : routePoints) {
            expectedTimeMap.put(rp.getPointId(), currentTime);
            currentTime = currentTime.plusMinutes(rp.getIntervalMinutes());
        }

        // 处理提交的记录
        Map<String, EpatrolTaskSubmitReqVO.PatrolRecordItem> submitRecordMap = new HashMap<>();
        int actualSort = 1;
        for (EpatrolTaskSubmitReqVO.PatrolRecordItem item : submitReqVO.getRecords()) {
            item.setActualSort(actualSort++);
            submitRecordMap.put(item.getPointNo(), item);
        }

        // 生成任务记录
        for (EpatrolRoutePointDO rp : routePoints) {
            EpatrolPointDO point = pointIdMap.get(rp.getPointId());
            if (point == null) continue;

            EpatrolTaskRecordDO record = new EpatrolTaskRecordDO();
            record.setTaskId(task.getId());
            record.setPointId(rp.getPointId());
            record.setPointNo(point.getPointNo());
            record.setPointName(point.getPointName());
            record.setExpectedSort(rp.getSort());
            record.setPlannedTime(expectedTimeMap.get(rp.getPointId()));

            EpatrolTaskSubmitReqVO.PatrolRecordItem submitRecord = submitRecordMap.get(point.getPointNo());
            if (submitRecord != null) {
                // 有打卡记录
                record.setActualTime(submitRecord.getActualTime());
                record.setActualSort(submitRecord.getActualSort());

                // 获取人员信息
                if (submitRecord.getPersonCardNo() != null) {
                    EpatrolPersonDO person = personMapper.selectByPersonCardNo(submitRecord.getPersonCardNo());
                    if (person != null) {
                        record.setPersonId(person.getId());
                        record.setPersonName(person.getName());
                    }
                }

                // 计算时间差
                long diffSeconds = Duration.between(record.getPlannedTime(), record.getActualTime()).getSeconds();
                record.setTimeDiffSeconds((int) diffSeconds);

                // 判断巡更状态
                if (!rp.getSort().equals(submitRecord.getActualSort())) {
                    record.setPatrolStatus(PATROL_STATUS_WRONG_ORDER);
                } else if (Math.abs(diffSeconds) <= ON_TIME_TOLERANCE_SECONDS) {
                    record.setPatrolStatus(PATROL_STATUS_ON_TIME);
                } else if (diffSeconds < 0) {
                    record.setPatrolStatus(PATROL_STATUS_EARLY);
                } else {
                    record.setPatrolStatus(PATROL_STATUS_LATE);
                }
            } else {
                // 没有打卡记录，标记为未到
                record.setPatrolStatus(PATROL_STATUS_MISSED);
            }

            taskRecordMapper.insert(record);
        }

        // 更新任务状态
        EpatrolTaskDO updateTask = new EpatrolTaskDO();
        updateTask.setId(task.getId());
        updateTask.setStatus(1); // 已巡
        updateTask.setSubmitTime(LocalDateTime.now());
        taskMapper.updateById(updateTask);

        log.info("[submitTask][任务{}提交成功，共{}条记录]", task.getTaskCode(), submitReqVO.getRecords().size());
    }

    @Override
    public EpatrolTaskStatisticsVO getTaskStatistics(EpatrolTaskPageReqVO reqVO) {
        EpatrolTaskStatisticsVO statistics = new EpatrolTaskStatisticsVO();

        // 构建查询条件（复用分页查询的条件，但不分页）
        LambdaQueryWrapperX<EpatrolTaskDO> wrapper = new LambdaQueryWrapperX<EpatrolTaskDO>()
                .likeIfPresent(EpatrolTaskDO::getTaskCode, reqVO.getTaskCode())
                .eqIfPresent(EpatrolTaskDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(EpatrolTaskDO::getRouteId, reqVO.getRouteId())
                .eqIfPresent(EpatrolTaskDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(EpatrolTaskDO::getTaskDate, reqVO.getTaskDateStart(), reqVO.getTaskDateEnd());

        // 如果有 personId 条件，使用 JSON_CONTAINS
        if (reqVO.getPersonId() != null) {
            wrapper.apply("JSON_CONTAINS(person_ids, {0})", reqVO.getPersonId());
        }

        List<EpatrolTaskDO> allTasks = taskMapper.selectList(wrapper);

        // 计算总体统计
        int total = allTasks.size();
        int completed = (int) allTasks.stream().filter(t -> t.getStatus() != null && t.getStatus() == 1).count();
        int pending = total - completed;

        statistics.setTotal(total);
        statistics.setCompleted(completed);
        statistics.setPending(pending);
        statistics.setRate(total > 0 ? Math.round((float) completed / total * 100) : 0);

        // 计算今日统计
        LocalDate today = LocalDate.now();
        List<EpatrolTaskDO> todayTasks = allTasks.stream()
                .filter(t -> t.getTaskDate() != null && t.getTaskDate().equals(today))
                .collect(Collectors.toList());

        int todayTotal = todayTasks.size();
        int todayCompleted = (int) todayTasks.stream().filter(t -> t.getStatus() != null && t.getStatus() == 1).count();

        statistics.setTodayTotal(todayTotal);
        statistics.setTodayCompleted(todayCompleted);
        statistics.setTodayRate(todayTotal > 0 ? Math.round((float) todayCompleted / todayTotal * 100) : 0);

        // 计算人员统计
        List<EpatrolTaskStatisticsVO.PersonStatistics> personStatsList = calculatePersonStatistics(allTasks);
        statistics.setPersonStatistics(personStatsList);

        return statistics;
    }

    /**
     * 计算人员统计
     */
    private List<EpatrolTaskStatisticsVO.PersonStatistics> calculatePersonStatistics(List<EpatrolTaskDO> allTasks) {
        // 收集所有涉及的人员ID
        Set<Long> allPersonIds = new HashSet<>();
        for (EpatrolTaskDO task : allTasks) {
            if (CollUtil.isNotEmpty(task.getPersonIds())) {
                allPersonIds.addAll(task.getPersonIds());
            }
        }

        if (CollUtil.isEmpty(allPersonIds)) {
            return new ArrayList<>();
        }

        // 获取人员信息
        Map<Long, EpatrolPersonDO> personMap = personMapper.selectBatchIds(allPersonIds).stream()
                .collect(Collectors.toMap(EpatrolPersonDO::getId, p -> p));

        // 获取所有已完成任务的记录，用于计算打卡状态统计
        List<Long> completedTaskIds = allTasks.stream()
                .filter(t -> t.getStatus() != null && t.getStatus() == 1)
                .map(EpatrolTaskDO::getId)
                .collect(Collectors.toList());

        Map<Long, List<EpatrolTaskRecordDO>> taskRecordsMap = new HashMap<>();
        if (CollUtil.isNotEmpty(completedTaskIds)) {
            for (Long taskId : completedTaskIds) {
                List<EpatrolTaskRecordDO> records = taskRecordMapper.selectByTaskId(taskId);
                taskRecordsMap.put(taskId, records);
            }
        }

        // 按人员统计
        List<EpatrolTaskStatisticsVO.PersonStatistics> result = new ArrayList<>();
        for (Long personId : allPersonIds) {
            EpatrolPersonDO person = personMap.get(personId);
            if (person == null) continue;

            EpatrolTaskStatisticsVO.PersonStatistics personStats = new EpatrolTaskStatisticsVO.PersonStatistics();
            personStats.setPersonId(personId);
            personStats.setPersonName(person.getName());

            // 统计该人员的任务
            List<EpatrolTaskDO> personTasks = allTasks.stream()
                    .filter(t -> t.getPersonIds() != null && t.getPersonIds().contains(personId))
                    .collect(Collectors.toList());

            int personTotal = personTasks.size();
            int personCompleted = (int) personTasks.stream()
                    .filter(t -> t.getStatus() != null && t.getStatus() == 1)
                    .count();

            personStats.setTotal(personTotal);
            personStats.setCompleted(personCompleted);
            personStats.setPending(personTotal - personCompleted);
            personStats.setRate(personTotal > 0 ? Math.round((float) personCompleted / personTotal * 100) : 0);

            // 统计打卡状态（准时/迟到/早到）
            int onTimeCount = 0;
            int lateCount = 0;
            int earlyCount = 0;

            for (EpatrolTaskDO task : personTasks) {
                if (task.getStatus() != null && task.getStatus() == 1) {
                    List<EpatrolTaskRecordDO> records = taskRecordsMap.get(task.getId());
                    if (CollUtil.isNotEmpty(records)) {
                        for (EpatrolTaskRecordDO record : records) {
                            // 只统计该人员的打卡记录
                            if (record.getPersonId() != null && record.getPersonId().equals(personId)) {
                                if (record.getPatrolStatus() != null) {
                                    switch (record.getPatrolStatus()) {
                                        case 1: onTimeCount++; break; // 准时
                                        case 2: earlyCount++; break;  // 早到
                                        case 3: lateCount++; break;   // 迟到
                                    }
                                }
                            }
                        }
                    }
                }
            }

            personStats.setOnTimeCount(onTimeCount);
            personStats.setLateCount(lateCount);
            personStats.setEarlyCount(earlyCount);

            result.add(personStats);
        }

        // 按完成率降序排序
        result.sort((a, b) -> b.getRate().compareTo(a.getRate()));

        return result;
    }

}
