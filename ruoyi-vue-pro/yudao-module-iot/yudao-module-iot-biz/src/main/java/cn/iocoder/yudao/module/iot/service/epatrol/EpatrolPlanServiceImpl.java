package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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
import java.util.List;
import java.util.Map;
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
        planPeriodMapper.deleteByPlanId(updateReqVO.getId());

        // 插入新的时段
        if (CollUtil.isNotEmpty(updateReqVO.getPeriods())) {
            for (EpatrolPlanSaveReqVO.PlanPeriodItem item : updateReqVO.getPeriods()) {
                EpatrolPlanPeriodDO period = new EpatrolPlanPeriodDO();
                period.setPlanId(updateReqVO.getId());
                period.setStartTime(item.getStartTime());
                period.setDurationMinutes(item.getDurationMinutes());
                period.setPersonIds(item.getPersonIds());
                planPeriodMapper.insert(period);
            }
        }
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
            for (EpatrolPlanPeriodDO period : periods) {
                if (CollUtil.isNotEmpty(period.getPersonIds())) {
                    allPersonIds.addAll(period.getPersonIds());
                }
            }

            // 获取人员信息
            Map<Long, String> personNameMap = CollUtil.isEmpty(allPersonIds) ? Map.of() :
                    personMapper.selectBatchIds(allPersonIds).stream()
                            .collect(Collectors.toMap(EpatrolPersonDO::getId, EpatrolPersonDO::getName));

            List<EpatrolPlanRespVO.PlanPeriodRespVO> periodRespVOs = new ArrayList<>();
            List<String> allPersonNames = new ArrayList<>();

            for (EpatrolPlanPeriodDO period : periods) {
                EpatrolPlanRespVO.PlanPeriodRespVO periodResp = new EpatrolPlanRespVO.PlanPeriodRespVO();
                periodResp.setId(period.getId());
                periodResp.setStartTime(period.getStartTime());
                periodResp.setEndTime(period.getStartTime().plusMinutes(period.getDurationMinutes()));
                periodResp.setDurationMinutes(period.getDurationMinutes());
                periodResp.setPersonIds(period.getPersonIds());

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
        return planMapper.selectPage(pageReqVO);
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

                // 创建任务
                EpatrolTaskDO task = new EpatrolTaskDO();
                task.setTaskCode(taskCode);
                task.setPlanId(plan.getId());
                task.setPeriodId(period.getId());
                task.setRouteId(plan.getRouteId());
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

}
