package cn.iocoder.yudao.module.iot.service.task;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.task.vo.log.TaskLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.task.vo.log.TaskLogRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.task.TaskExecutionLogDO;
import cn.iocoder.yudao.module.iot.dal.mysql.task.TaskExecutionLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 任务执行日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class TaskLogServiceImpl implements TaskLogService {

    @Resource
    private TaskExecutionLogMapper taskExecutionLogMapper;

    @Override
    public PageResult<TaskLogRespVO> getLogPage(TaskLogPageReqVO pageReqVO) {
        PageResult<TaskExecutionLogDO> pageResult = taskExecutionLogMapper.selectPage(
                pageReqVO,  // PageParam
                pageReqVO.getTaskConfigId(),
                pageReqVO.getEntityType(),
                pageReqVO.getEntityId(),
                pageReqVO.getEntityName(),
                pageReqVO.getJobType(),
                pageReqVO.getExecutionStatus(),
                pageReqVO.getStartTime()
        );
        
        return new PageResult<>(
                BeanUtil.copyToList(pageResult.getList(), TaskLogRespVO.class),
                pageResult.getTotal()
        );
    }

    @Override
    public TaskLogRespVO getLogDetail(Long id) {
        TaskExecutionLogDO logDO = taskExecutionLogMapper.selectById(id);
        return BeanUtil.copyProperties(logDO, TaskLogRespVO.class);
    }

}

