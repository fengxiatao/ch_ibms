package cn.iocoder.yudao.module.iot.service.patrolplan;

import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.task.PatrolTaskSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolPlanDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolSceneDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolTaskDO;
import cn.iocoder.yudao.module.iot.dal.mysql.patrolplan.IotVideoPatrolPlanMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.patrolplan.IotVideoPatrolSceneMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.patrolplan.IotVideoPatrolTaskMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 轮巡任务 Service 实现类
 *
 * @author 长辉信息
 */
@Service
@Validated
public class PatrolTaskServiceImpl implements PatrolTaskService {

    @Resource
    private IotVideoPatrolTaskMapper patrolTaskMapper;

    @Resource
    private IotVideoPatrolPlanMapper patrolPlanMapper;

    @Resource
    private IotVideoPatrolSceneMapper patrolSceneMapper;

    @Override
    public Long createPatrolTask(PatrolTaskSaveReqVO createReqVO) {
        // 校验计划是否存在（自动过滤租户）
        validatePlanExists(createReqVO.getPlanId());
        
        // 校验任务编码唯一性（同一租户下）
        validateTaskCodeUnique(null, createReqVO.getTaskCode());
        
        // 插入
        IotVideoPatrolTaskDO task = IotVideoPatrolTaskDO.builder()
                .planId(createReqVO.getPlanId())
                .taskName(createReqVO.getTaskName())
                .taskCode(createReqVO.getTaskCode())
                .description(createReqVO.getDescription())
                .scheduleType(createReqVO.getScheduleType())
                .scheduleConfig(createReqVO.getScheduleConfig())
                .timeSlots(convertTimeSlots(createReqVO.getTimeSlots()))
                .loopMode(createReqVO.getLoopMode() != null ? createReqVO.getLoopMode() : 1)
                .intervalMinutes(createReqVO.getIntervalMinutes() != null ? createReqVO.getIntervalMinutes() : 5)
                .autoSnapshot(createReqVO.getAutoSnapshot())
                .autoRecording(createReqVO.getAutoRecording())
                .recordingDuration(createReqVO.getRecordingDuration())
                .aiAnalysis(createReqVO.getAiAnalysis())
                .alertOnAbnormal(createReqVO.getAlertOnAbnormal())
                .alertUserIds(convertAlertUserIds(createReqVO.getAlertUserIds()))
                .status(createReqVO.getStatus() != null ? createReqVO.getStatus() : 1)
                .runningStatus("stopped")
                .sort(createReqVO.getSort() != null ? createReqVO.getSort() : 0)
                .build();
        
        patrolTaskMapper.insert(task);
        
        // 返回
        return task.getId();
    }

    @Override
    public void updatePatrolTask(PatrolTaskSaveReqVO updateReqVO) {
        // 校验存在
        validatePatrolTaskExists(updateReqVO.getId());
        
        // 校验计划是否存在（自动过滤租户）
        validatePlanExists(updateReqVO.getPlanId());
        
        // 校验任务编码唯一性（同一租户下）
        validateTaskCodeUnique(updateReqVO.getId(), updateReqVO.getTaskCode());
        
        // 更新
        IotVideoPatrolTaskDO updateObj = IotVideoPatrolTaskDO.builder()
                .id(updateReqVO.getId())
                .planId(updateReqVO.getPlanId())
                .taskName(updateReqVO.getTaskName())
                .taskCode(updateReqVO.getTaskCode())
                .description(updateReqVO.getDescription())
                .scheduleType(updateReqVO.getScheduleType())
                .scheduleConfig(updateReqVO.getScheduleConfig())
                .timeSlots(convertTimeSlots(updateReqVO.getTimeSlots()))
                .loopMode(updateReqVO.getLoopMode())
                .intervalMinutes(updateReqVO.getIntervalMinutes())
                .autoSnapshot(updateReqVO.getAutoSnapshot())
                .autoRecording(updateReqVO.getAutoRecording())
                .recordingDuration(updateReqVO.getRecordingDuration())
                .aiAnalysis(updateReqVO.getAiAnalysis())
                .alertOnAbnormal(updateReqVO.getAlertOnAbnormal())
                .alertUserIds(convertAlertUserIds(updateReqVO.getAlertUserIds()))
                .status(updateReqVO.getStatus())
                .sort(updateReqVO.getSort())
                .build();
        
        patrolTaskMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePatrolTask(Long id) {
        // 校验存在
        validatePatrolTaskExists(id);
        
        // 检查是否有关联的场景（自动过滤租户）
        List<IotVideoPatrolSceneDO> scenes = patrolSceneMapper.selectListByTaskId(id);
        if (!scenes.isEmpty()) {
            throw exception(VIDEO_PATROL_TASK_HAS_SCENES);
        }
        
        // 删除（自动过滤租户）
        patrolTaskMapper.deleteById(id);
    }

    @Override
    public IotVideoPatrolTaskDO getPatrolTask(Long id) {
        // 自动过滤租户
        return patrolTaskMapper.selectById(id);
    }

    @Override
    public List<IotVideoPatrolTaskDO> getPatrolTaskListByPlanId(Long planId) {
        // 自动过滤租户
        return patrolTaskMapper.selectListByPlanId(planId);
    }

    @Override
    public void startPatrolTask(Long id) {
        // 校验存在
        IotVideoPatrolTaskDO task = validatePatrolTaskExists(id);
        
        // 校验状态
        if (task.getStatus() != 1) {
            throw exception(VIDEO_PATROL_TASK_NOT_ENABLED);
        }
        
        if ("running".equals(task.getRunningStatus())) {
            throw exception(VIDEO_PATROL_TASK_ALREADY_RUNNING);
        }
        
        // 更新运行状态
        IotVideoPatrolTaskDO updateObj = IotVideoPatrolTaskDO.builder()
                .id(id)
                .runningStatus("running")
                .build();
        
        patrolTaskMapper.updateById(updateObj);
        
        // TODO: 启动轮巡执行引擎
    }

    @Override
    public void pausePatrolTask(Long id) {
        // 校验存在
        IotVideoPatrolTaskDO task = validatePatrolTaskExists(id);
        
        // 校验状态
        if (!"running".equals(task.getRunningStatus())) {
            throw exception(VIDEO_PATROL_TASK_NOT_RUNNING);
        }
        
        // 更新运行状态
        IotVideoPatrolTaskDO updateObj = IotVideoPatrolTaskDO.builder()
                .id(id)
                .runningStatus("paused")
                .build();
        
        patrolTaskMapper.updateById(updateObj);
        
        // TODO: 暂停轮巡执行引擎
    }

    @Override
    public void stopPatrolTask(Long id) {
        // 校验存在
        IotVideoPatrolTaskDO task = validatePatrolTaskExists(id);
        
        // 更新运行状态
        IotVideoPatrolTaskDO updateObj = IotVideoPatrolTaskDO.builder()
                .id(id)
                .runningStatus("stopped")
                .build();
        
        patrolTaskMapper.updateById(updateObj);
        
        // TODO: 停止轮巡执行引擎
    }

    // ==================== 私有方法 ====================

    /**
     * 校验轮巡任务是否存在
     */
    private IotVideoPatrolTaskDO validatePatrolTaskExists(Long id) {
        IotVideoPatrolTaskDO task = patrolTaskMapper.selectById(id);
        if (task == null) {
            throw exception(VIDEO_PATROL_TASK_NOT_EXISTS);
        }
        return task;
    }

    /**
     * 校验计划是否存在
     */
    private void validatePlanExists(Long planId) {
        IotVideoPatrolPlanDO plan = patrolPlanMapper.selectById(planId);
        if (plan == null) {
            throw exception(VIDEO_PATROL_PLAN_NOT_EXISTS);
        }
    }

    /**
     * 校验任务编码唯一性（同一租户下）
     * 
     * @param id 任务ID（更新时传入，新建时传null）
     * @param taskCode 任务编码
     */
    private void validateTaskCodeUnique(Long id, String taskCode) {
        IotVideoPatrolTaskDO task = patrolTaskMapper.selectOne("task_code", taskCode);
        if (task == null) {
            return;
        }
        
        // 如果 id 为空，说明是新建，存在即重复
        if (id == null) {
            throw exception(VIDEO_PATROL_TASK_CODE_DUPLICATE);
        }
        
        // 如果 id 不为空，说明是更新，需要排除自己
        if (!task.getId().equals(id)) {
            throw exception(VIDEO_PATROL_TASK_CODE_DUPLICATE);
        }
    }

    /**
     * 转换时间段配置类型
     */
    private List<Map<String, Object>> convertTimeSlots(List<Map<String, String>> timeSlots) {
        if (timeSlots == null) {
            return null;
        }
        return timeSlots.stream()
                .map(map -> new java.util.HashMap<String, Object>(map))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 转换告警用户ID列表
     */
    private List<Long> convertAlertUserIds(String alertUserIds) {
        if (alertUserIds == null || alertUserIds.trim().isEmpty()) {
            return null;
        }
        return java.util.Arrays.stream(alertUserIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(java.util.stream.Collectors.toList());
    }

}
