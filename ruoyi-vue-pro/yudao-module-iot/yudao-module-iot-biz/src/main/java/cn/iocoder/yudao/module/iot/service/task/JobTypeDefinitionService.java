package cn.iocoder.yudao.module.iot.service.task;

import cn.iocoder.yudao.module.iot.dal.dataobject.task.JobTypeDefinitionDO;

import java.util.List;

/**
 * 定时任务类型定义 Service 接口
 *
 * @author 芋道源码
 */
public interface JobTypeDefinitionService {

    /**
     * 获取适用于指定实体类型的任务类型列表
     *
     * @param entityType 实体类型
     * @return 任务类型列表
     */
    List<JobTypeDefinitionDO> getApplicableJobTypes(String entityType);

    /**
     * 获取任务类型详情
     *
     * @param id 任务类型ID
     * @return 任务类型
     */
    JobTypeDefinitionDO getJobTypeDefinition(Long id);

    /**
     * 根据code获取任务类型
     *
     * @param code 任务编码
     * @return 任务类型
     */
    JobTypeDefinitionDO getJobTypeByCode(String code);

}




