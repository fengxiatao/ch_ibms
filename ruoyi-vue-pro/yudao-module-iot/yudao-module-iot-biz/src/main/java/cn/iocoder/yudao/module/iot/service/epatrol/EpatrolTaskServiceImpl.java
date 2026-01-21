package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskSubmitReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.*;
import cn.iocoder.yudao.module.iot.dal.mysql.epatrol.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
        return taskMapper.selectPage(pageReqVO);
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

}
