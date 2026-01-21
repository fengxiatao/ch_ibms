package cn.iocoder.yudao.module.iot.service.patrolplan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.plan.PatrolPlanPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.plan.PatrolPlanSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolPlanDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolTaskDO;
import cn.iocoder.yudao.module.iot.dal.mysql.patrolplan.IotVideoPatrolPlanMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.patrolplan.IotVideoPatrolTaskMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 轮巡计划 Service 实现类
 *
 * @author 长辉信息
 */
@Service
@Validated
public class PatrolPlanServiceImpl implements PatrolPlanService {

    @Resource
    private IotVideoPatrolPlanMapper patrolPlanMapper;

    @Resource
    private IotVideoPatrolTaskMapper patrolTaskMapper;

    @Override
    public Long createPatrolPlan(PatrolPlanSaveReqVO createReqVO) {
        // 校验计划编码唯一性（同一租户下）
        validatePlanCodeUnique(null, createReqVO.getPlanCode());
        
        // 插入
        IotVideoPatrolPlanDO plan = IotVideoPatrolPlanDO.builder()
                .planName(createReqVO.getPlanName())
                .planCode(createReqVO.getPlanCode())
                .description(createReqVO.getDescription())
                .status(createReqVO.getStatus() != null ? createReqVO.getStatus() : 1)
                .runningStatus("stopped")
                .loopMode(createReqVO.getLoopMode() != null ? createReqVO.getLoopMode() : 1)
                .startDate(createReqVO.getStartDate())
                .endDate(createReqVO.getEndDate())
                .sort(createReqVO.getSort() != null ? createReqVO.getSort() : 0)
                .build();
        
        patrolPlanMapper.insert(plan);
        
        // 返回
        return plan.getId();
    }

    @Override
    public void updatePatrolPlan(PatrolPlanSaveReqVO updateReqVO) {
        // 校验存在
        validatePatrolPlanExists(updateReqVO.getId());
        
        // 校验计划编码唯一性（同一租户下）
        validatePlanCodeUnique(updateReqVO.getId(), updateReqVO.getPlanCode());
        
        // 更新
        IotVideoPatrolPlanDO updateObj = IotVideoPatrolPlanDO.builder()
                .id(updateReqVO.getId())
                .planName(updateReqVO.getPlanName())
                .planCode(updateReqVO.getPlanCode())
                .description(updateReqVO.getDescription())
                .status(updateReqVO.getStatus())
                .loopMode(updateReqVO.getLoopMode())
                .startDate(updateReqVO.getStartDate())
                .endDate(updateReqVO.getEndDate())
                .sort(updateReqVO.getSort())
                .build();
        
        patrolPlanMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePatrolPlan(Long id) {
        // 校验存在
        validatePatrolPlanExists(id);
        
        // 检查是否有关联的任务（自动过滤租户）
        List<IotVideoPatrolTaskDO> tasks = patrolTaskMapper.selectListByPlanId(id);
        if (!tasks.isEmpty()) {
            throw exception(VIDEO_PATROL_PLAN_HAS_TASKS);
        }
        
        // 删除
        patrolPlanMapper.deleteById(id);
    }

    @Override
    public IotVideoPatrolPlanDO getPatrolPlan(Long id) {
        return patrolPlanMapper.selectById(id);
    }

    @Override
    public PageResult<IotVideoPatrolPlanDO> getPatrolPlanPage(PatrolPlanPageReqVO pageReqVO) {
        // MyBatis-Plus 会自动根据 TenantId 过滤数据，无需手动处理多租户
        return patrolPlanMapper.selectPage(pageReqVO);
    }

    @Override
    public void startPatrolPlan(Long id) {
        // 校验存在
        IotVideoPatrolPlanDO plan = validatePatrolPlanExists(id);
        
        // 校验状态
        if (plan.getStatus() != 1) {
            throw exception(VIDEO_PATROL_PLAN_NOT_ENABLED);
        }
        
        if ("running".equals(plan.getRunningStatus())) {
            throw exception(VIDEO_PATROL_PLAN_ALREADY_RUNNING);
        }
        
        // 更新运行状态
        IotVideoPatrolPlanDO updateObj = IotVideoPatrolPlanDO.builder()
                .id(id)
                .runningStatus("running")
                .build();
        
        patrolPlanMapper.updateById(updateObj);
        
        // TODO: 启动轮巡执行引擎
    }

    @Override
    public void pausePatrolPlan(Long id) {
        // 校验存在
        IotVideoPatrolPlanDO plan = validatePatrolPlanExists(id);
        
        // 校验状态
        if (!"running".equals(plan.getRunningStatus())) {
            throw exception(VIDEO_PATROL_PLAN_NOT_RUNNING);
        }
        
        // 更新运行状态
        IotVideoPatrolPlanDO updateObj = IotVideoPatrolPlanDO.builder()
                .id(id)
                .runningStatus("paused")
                .build();
        
        patrolPlanMapper.updateById(updateObj);
        
        // TODO: 暂停轮巡执行引擎
    }

    @Override
    public void stopPatrolPlan(Long id) {
        // 校验存在
        IotVideoPatrolPlanDO plan = validatePatrolPlanExists(id);
        
        // 更新运行状态
        IotVideoPatrolPlanDO updateObj = IotVideoPatrolPlanDO.builder()
                .id(id)
                .runningStatus("stopped")
                .build();
        
        patrolPlanMapper.updateById(updateObj);
        
        // TODO: 停止轮巡执行引擎
    }

    // ==================== 私有方法 ====================

    /**
     * 校验轮巡计划是否存在
     */
    private IotVideoPatrolPlanDO validatePatrolPlanExists(Long id) {
        IotVideoPatrolPlanDO plan = patrolPlanMapper.selectById(id);
        if (plan == null) {
            throw exception(VIDEO_PATROL_PLAN_NOT_EXISTS);
        }
        return plan;
    }

    /**
     * 校验计划编码唯一性（同一租户下）
     * 
     * @param id 计划ID（更新时传入，新建时传null）
     * @param planCode 计划编码
     */
    private void validatePlanCodeUnique(Long id, String planCode) {
        IotVideoPatrolPlanDO plan = patrolPlanMapper.selectOne("plan_code", planCode);
        if (plan == null) {
            return;
        }
        
        // 如果 id 为空，说明是新建，存在即重复
        if (id == null) {
            throw exception(VIDEO_PATROL_PLAN_CODE_DUPLICATE);
        }
        
        // 如果 id 不为空，说明是更新，需要排除自己
        if (!plan.getId().equals(id)) {
            throw exception(VIDEO_PATROL_PLAN_CODE_DUPLICATE);
        }
    }

}
