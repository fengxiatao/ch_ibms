package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessAuthTaskDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门禁授权任务明细 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessAuthTaskDetailMapper extends BaseMapperX<IotAccessAuthTaskDetailDO> {

    /**
     * 根据任务ID查询明细列表
     */
    default List<IotAccessAuthTaskDetailDO> selectListByTaskId(Long taskId) {
        return selectList(IotAccessAuthTaskDetailDO::getTaskId, taskId);
    }

    /**
     * 根据任务ID和状态查询明细列表
     */
    default List<IotAccessAuthTaskDetailDO> selectListByTaskIdAndStatus(Long taskId, Integer status) {
        return selectList(new LambdaQueryWrapperX<IotAccessAuthTaskDetailDO>()
                .eq(IotAccessAuthTaskDetailDO::getTaskId, taskId)
                .eq(IotAccessAuthTaskDetailDO::getStatus, status));
    }

    /**
     * 根据任务ID和状态统计数量
     */
    default Long selectCountByTaskIdAndStatus(Long taskId, Integer status) {
        return selectCount(new LambdaQueryWrapperX<IotAccessAuthTaskDetailDO>()
                .eq(IotAccessAuthTaskDetailDO::getTaskId, taskId)
                .eq(IotAccessAuthTaskDetailDO::getStatus, status));
    }

    /**
     * 根据任务ID统计总数
     */
    default Long selectCountByTaskId(Long taskId) {
        return selectCount(IotAccessAuthTaskDetailDO::getTaskId, taskId);
    }

    /**
     * 根据任务ID删除明细
     */
    default int deleteByTaskId(Long taskId) {
        return delete(IotAccessAuthTaskDetailDO::getTaskId, taskId);
    }

    /**
     * 分页查询任务明细
     */
    default PageResult<IotAccessAuthTaskDetailDO> selectPage(Long taskId, Integer status,
                                                              Integer pageNo, Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam,
                new LambdaQueryWrapperX<IotAccessAuthTaskDetailDO>()
                .eqIfPresent(IotAccessAuthTaskDetailDO::getTaskId, taskId)
                .eqIfPresent(IotAccessAuthTaskDetailDO::getStatus, status)
                .orderByDesc(IotAccessAuthTaskDetailDO::getId));
    }

    /**
     * 查询失败的明细列表
     */
    default List<IotAccessAuthTaskDetailDO> selectFailedByTaskId(Long taskId) {
        return selectListByTaskIdAndStatus(taskId, IotAccessAuthTaskDetailDO.STATUS_FAILED);
    }

    /**
     * 查询待执行的明细列表
     */
    default List<IotAccessAuthTaskDetailDO> selectPendingByTaskId(Long taskId) {
        return selectListByTaskIdAndStatus(taskId, IotAccessAuthTaskDetailDO.STATUS_PENDING);
    }

}
