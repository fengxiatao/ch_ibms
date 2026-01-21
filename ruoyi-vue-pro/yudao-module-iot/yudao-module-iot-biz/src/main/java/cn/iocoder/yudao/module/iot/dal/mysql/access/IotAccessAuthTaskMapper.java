package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.authtask.IotAccessAuthTaskPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessAuthTaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门禁授权任务 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessAuthTaskMapper extends BaseMapperX<IotAccessAuthTaskDO> {

    /**
     * 分页查询授权任务
     */
    default PageResult<IotAccessAuthTaskDO> selectPage(IotAccessAuthTaskPageReqVO reqVO) {
        return selectPage(reqVO,
                new LambdaQueryWrapperX<IotAccessAuthTaskDO>()
                .eqIfPresent(IotAccessAuthTaskDO::getTaskType, reqVO.getTaskType())
                .eqIfPresent(IotAccessAuthTaskDO::getGroupId, reqVO.getGroupId())
                .eqIfPresent(IotAccessAuthTaskDO::getPersonId, reqVO.getPersonId())
                .eqIfPresent(IotAccessAuthTaskDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotAccessAuthTaskDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotAccessAuthTaskDO::getId));
    }

    /**
     * 根据状态查询任务列表
     */
    default List<IotAccessAuthTaskDO> selectListByStatus(Integer status) {
        return selectList(IotAccessAuthTaskDO::getStatus, status);
    }

    /**
     * 根据权限组ID查询任务列表
     */
    default List<IotAccessAuthTaskDO> selectListByGroupId(Long groupId) {
        return selectList(IotAccessAuthTaskDO::getGroupId, groupId);
    }

    /**
     * 根据人员ID查询任务列表
     */
    default List<IotAccessAuthTaskDO> selectListByPersonId(Long personId) {
        return selectList(IotAccessAuthTaskDO::getPersonId, personId);
    }

    /**
     * 查询执行中的任务
     */
    default List<IotAccessAuthTaskDO> selectRunningTasks() {
        return selectList(IotAccessAuthTaskDO::getStatus, IotAccessAuthTaskDO.STATUS_RUNNING);
    }

    /**
     * 查询待执行的任务
     */
    default List<IotAccessAuthTaskDO> selectPendingTasks() {
        return selectList(IotAccessAuthTaskDO::getStatus, IotAccessAuthTaskDO.STATUS_PENDING);
    }

}
