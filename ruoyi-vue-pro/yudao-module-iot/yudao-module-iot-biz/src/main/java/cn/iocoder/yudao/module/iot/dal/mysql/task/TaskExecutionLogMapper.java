package cn.iocoder.yudao.module.iot.dal.mysql.task;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.task.TaskExecutionLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * 任务执行日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface TaskExecutionLogMapper extends BaseMapperX<TaskExecutionLogDO> {

    default PageResult<TaskExecutionLogDO> selectPage(
            PageParam pageParam,
            Long taskConfigId,
            String entityType,
            Long entityId,
            String entityName,
            String jobType,
            String executionStatus,
            LocalDateTime[] startTime) {
        
        return selectPage(pageParam, new LambdaQueryWrapperX<TaskExecutionLogDO>()
                .eqIfPresent(TaskExecutionLogDO::getTaskConfigId, taskConfigId)
                .eqIfPresent(TaskExecutionLogDO::getEntityType, entityType)
                .eqIfPresent(TaskExecutionLogDO::getEntityId, entityId)
                .likeIfPresent(TaskExecutionLogDO::getEntityName, entityName)
                .eqIfPresent(TaskExecutionLogDO::getJobType, jobType)
                .eqIfPresent(TaskExecutionLogDO::getExecutionStatus, executionStatus)
                .betweenIfPresent(TaskExecutionLogDO::getStartTime, startTime)
                .orderByDesc(TaskExecutionLogDO::getId));
    }
}

