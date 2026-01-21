package cn.iocoder.yudao.module.iot.dal.mysql.task;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.task.ScheduledTaskConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 定时任务配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ScheduledTaskConfigMapper extends BaseMapperX<ScheduledTaskConfigDO> {

    /**
     * 查询实体的任务配置列表
     */
    default List<ScheduledTaskConfigDO> selectListByEntity(String entityType, Long entityId) {
        return selectList(new LambdaQueryWrapperX<ScheduledTaskConfigDO>()
                .eq(ScheduledTaskConfigDO::getEntityType, entityType)
                .eq(ScheduledTaskConfigDO::getEntityId, entityId)
                .orderByDesc(ScheduledTaskConfigDO::getCreateTime));
    }

    /**
     * 查询所有启用的任务配置
     */
    default List<ScheduledTaskConfigDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<ScheduledTaskConfigDO>()
                .eq(ScheduledTaskConfigDO::getEnabled, true)
                .orderByAsc(ScheduledTaskConfigDO::getPriority)
                .orderByAsc(ScheduledTaskConfigDO::getNextExecutionTime));
    }

    /**
     * 查询待执行的任务（下次执行时间已到）
     */
    default List<ScheduledTaskConfigDO> selectPendingTasks() {
        return selectList(new LambdaQueryWrapperX<ScheduledTaskConfigDO>()
                .eq(ScheduledTaskConfigDO::getEnabled, true)
                .le(ScheduledTaskConfigDO::getNextExecutionTime, java.time.LocalDateTime.now())
                .orderByAsc(ScheduledTaskConfigDO::getPriority));
    }

    /**
     * 按任务类型统计
     */
    default Long countByJobType(String jobType) {
        return selectCount(new LambdaQueryWrapperX<ScheduledTaskConfigDO>()
                .eq(ScheduledTaskConfigDO::getJobType, jobType));
    }

    /**
     * 按实体类型统计
     */
    default Long countByEntityType(String entityType) {
        return selectCount(new LambdaQueryWrapperX<ScheduledTaskConfigDO>()
                .eq(ScheduledTaskConfigDO::getEntityType, entityType));
    }

    /**
     * 统计启用的任务数
     */
    default Long countEnabled() {
        return selectCount(new LambdaQueryWrapperX<ScheduledTaskConfigDO>()
                .eq(ScheduledTaskConfigDO::getEnabled, true));
    }

    /**
     * 统计禁用的任务数
     */
    default Long countDisabled() {
        return selectCount(new LambdaQueryWrapperX<ScheduledTaskConfigDO>()
                .eq(ScheduledTaskConfigDO::getEnabled, false));
    }

}




