package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.operationlog.IotAccessOperationLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessOperationLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 门禁操作日志 Mapper
 * 
 * 支持两类操作日志查询：
 * 1. 门控操作日志：开门、关门、常开、常闭等
 * 2. 授权操作日志：授权下发、授权撤销等（Requirements: 12.4）
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessOperationLogMapper extends BaseMapperX<IotAccessOperationLogDO> {

    // 授权操作类型列表
    List<String> AUTH_OPERATION_TYPES = Arrays.asList(
            IotAccessOperationLogDO.OP_TYPE_AUTH_DISPATCH_GROUP,
            IotAccessOperationLogDO.OP_TYPE_AUTH_DISPATCH_PERSON,
            IotAccessOperationLogDO.OP_TYPE_AUTH_REVOKE,
            IotAccessOperationLogDO.OP_TYPE_AUTH_RETRY,
            IotAccessOperationLogDO.OP_TYPE_ADD_USER,
            IotAccessOperationLogDO.OP_TYPE_DELETE_USER,
            IotAccessOperationLogDO.OP_TYPE_ADD_CARD,
            IotAccessOperationLogDO.OP_TYPE_DELETE_CARD,
            IotAccessOperationLogDO.OP_TYPE_ADD_FACE,
            IotAccessOperationLogDO.OP_TYPE_DELETE_FACE,
            IotAccessOperationLogDO.OP_TYPE_ADD_FINGERPRINT,
            IotAccessOperationLogDO.OP_TYPE_DELETE_FINGERPRINT
    );

    default PageResult<IotAccessOperationLogDO> selectPage(Long deviceId, Long channelId,
                                                            String operationType, Long operatorId,
                                                            LocalDateTime startTime, LocalDateTime endTime,
                                                            Integer pageNo, Integer pageSize) {
        cn.iocoder.yudao.framework.common.pojo.PageParam pageParam = new cn.iocoder.yudao.framework.common.pojo.PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam,
                new LambdaQueryWrapperX<IotAccessOperationLogDO>()
                .eqIfPresent(IotAccessOperationLogDO::getDeviceId, deviceId)
                .eqIfPresent(IotAccessOperationLogDO::getChannelId, channelId)
                .eqIfPresent(IotAccessOperationLogDO::getOperationType, operationType)
                .eqIfPresent(IotAccessOperationLogDO::getOperatorId, operatorId)
                .geIfPresent(IotAccessOperationLogDO::getOperationTime, startTime)
                .leIfPresent(IotAccessOperationLogDO::getOperationTime, endTime)
                .orderByDesc(IotAccessOperationLogDO::getOperationTime));
    }

    default List<IotAccessOperationLogDO> selectListByDeviceId(Long deviceId) {
        return selectList(IotAccessOperationLogDO::getDeviceId, deviceId);
    }

    default List<IotAccessOperationLogDO> selectListByChannelId(Long channelId) {
        return selectList(IotAccessOperationLogDO::getChannelId, channelId);
    }
    
    // ========== 授权操作日志查询方法（Requirements: 12.4） ==========
    
    /**
     * 分页查询授权操作日志
     * 支持按人员、设备、时间范围筛选
     */
    default PageResult<IotAccessOperationLogDO> selectAuthLogPage(IotAccessOperationLogPageReqVO pageReqVO) {
        return selectPage(pageReqVO,
                new LambdaQueryWrapperX<IotAccessOperationLogDO>()
                        // 只查询授权相关的操作类型
                        .in(IotAccessOperationLogDO::getOperationType, AUTH_OPERATION_TYPES)
                        // 按设备筛选
                        .eqIfPresent(IotAccessOperationLogDO::getDeviceId, pageReqVO.getDeviceId())
                        .eqIfPresent(IotAccessOperationLogDO::getChannelId, pageReqVO.getChannelId())
                        // 按人员筛选
                        .eqIfPresent(IotAccessOperationLogDO::getTargetPersonId, pageReqVO.getTargetPersonId())
                        // 按操作类型筛选（如果指定了具体类型）
                        .eqIfPresent(IotAccessOperationLogDO::getOperationType, pageReqVO.getOperationType())
                        // 按操作人筛选
                        .eqIfPresent(IotAccessOperationLogDO::getOperatorId, pageReqVO.getOperatorId())
                        // 按结果筛选
                        .eqIfPresent(IotAccessOperationLogDO::getResult, pageReqVO.getResult())
                        // 按时间范围筛选
                        .geIfPresent(IotAccessOperationLogDO::getOperationTime, pageReqVO.getStartTime())
                        .leIfPresent(IotAccessOperationLogDO::getOperationTime, pageReqVO.getEndTime())
                        // 按任务ID筛选
                        .eqIfPresent(IotAccessOperationLogDO::getAuthTaskId, pageReqVO.getAuthTaskId())
                        // 按权限组筛选
                        .eqIfPresent(IotAccessOperationLogDO::getPermissionGroupId, pageReqVO.getPermissionGroupId())
                        // 按时间倒序
                        .orderByDesc(IotAccessOperationLogDO::getOperationTime));
    }
    
    /**
     * 查询人员的授权操作日志
     */
    default List<IotAccessOperationLogDO> selectAuthLogsByPerson(Long personId, LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<IotAccessOperationLogDO>()
                .in(IotAccessOperationLogDO::getOperationType, AUTH_OPERATION_TYPES)
                .eq(IotAccessOperationLogDO::getTargetPersonId, personId)
                .geIfPresent(IotAccessOperationLogDO::getOperationTime, startTime)
                .leIfPresent(IotAccessOperationLogDO::getOperationTime, endTime)
                .orderByDesc(IotAccessOperationLogDO::getOperationTime));
    }
    
    /**
     * 查询设备的授权操作日志
     */
    default List<IotAccessOperationLogDO> selectAuthLogsByDevice(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<IotAccessOperationLogDO>()
                .in(IotAccessOperationLogDO::getOperationType, AUTH_OPERATION_TYPES)
                .eq(IotAccessOperationLogDO::getDeviceId, deviceId)
                .geIfPresent(IotAccessOperationLogDO::getOperationTime, startTime)
                .leIfPresent(IotAccessOperationLogDO::getOperationTime, endTime)
                .orderByDesc(IotAccessOperationLogDO::getOperationTime));
    }
    
    /**
     * 查询授权任务的操作日志
     */
    default List<IotAccessOperationLogDO> selectAuthLogsByTaskId(Long authTaskId) {
        return selectList(new LambdaQueryWrapperX<IotAccessOperationLogDO>()
                .eq(IotAccessOperationLogDO::getAuthTaskId, authTaskId)
                .orderByDesc(IotAccessOperationLogDO::getOperationTime));
    }

}
