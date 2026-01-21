package cn.iocoder.yudao.module.iot.dal.mysql.task;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.task.JobTypeDefinitionDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 定时任务类型定义 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface JobTypeDefinitionMapper extends BaseMapperX<JobTypeDefinitionDO> {

    /**
     * 获取适用于指定实体类型的任务类型列表
     *
     * @param entityType 实体类型
     * @return 任务类型列表
     */
    default List<JobTypeDefinitionDO> selectApplicableJobTypes(String entityType) {
        return selectList(new LambdaQueryWrapper<JobTypeDefinitionDO>()
                .apply("FIND_IN_SET({0}, applicable_entities) > 0", entityType)
                .eq(JobTypeDefinitionDO::getStatus, 1)
                .orderByAsc(JobTypeDefinitionDO::getId));
    }

    /**
     * 根据code获取任务类型
     *
     * @param code 任务编码
     * @return 任务类型
     */
    default JobTypeDefinitionDO selectByCode(String code) {
        return selectOne(JobTypeDefinitionDO::getCode, code);
    }

}




