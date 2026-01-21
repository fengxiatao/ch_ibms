package cn.iocoder.yudao.module.iot.service.task;

import cn.iocoder.yudao.module.iot.dal.dataobject.task.JobTypeDefinitionDO;
import cn.iocoder.yudao.module.iot.dal.mysql.task.JobTypeDefinitionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.JOB_TYPE_DEFINITION_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 定时任务类型定义 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class JobTypeDefinitionServiceImpl implements JobTypeDefinitionService {

    @Resource
    private JobTypeDefinitionMapper jobTypeDefinitionMapper;

    @Override
    public List<JobTypeDefinitionDO> getApplicableJobTypes(String entityType) {
        return jobTypeDefinitionMapper.selectApplicableJobTypes(entityType);
    }

    @Override
    public JobTypeDefinitionDO getJobTypeDefinition(Long id) {
        JobTypeDefinitionDO jobType = jobTypeDefinitionMapper.selectById(id);
        if (jobType == null) {
            throw exception(JOB_TYPE_DEFINITION_NOT_EXISTS);
        }
        return jobType;
    }

    @Override
    public JobTypeDefinitionDO getJobTypeByCode(String code) {
        return jobTypeDefinitionMapper.selectByCode(code);
    }

}




