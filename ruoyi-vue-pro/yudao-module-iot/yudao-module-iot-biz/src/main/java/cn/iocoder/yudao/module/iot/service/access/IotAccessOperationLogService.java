package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.operationlog.IotAccessOperationLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessOperationLogDO;
import cn.iocoder.yudao.module.iot.service.access.dto.DispatchResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁操作日志 Service 接口
 * 
 * 支持两类操作日志：
 * 1. 门控操作日志：开门、关门、常开、常闭等
 * 2. 授权操作日志：授权下发、授权撤销等（Requirements: 12.1, 12.2, 12.3）
 *
 * @author 芋道源码
 */
public interface IotAccessOperationLogService {

    /**
     * 记录操作日志
     *
     * @param operationLog 操作日志
     * @return 日志ID
     */
    Long saveOperationLog(IotAccessOperationLogDO operationLog);

    /**
     * 记录操作日志（简化方法）
     *
     * @param deviceId      设备ID
     * @param channelId     通道ID
     * @param operationType 操作类型
     * @param operatorId    操作人ID
     * @param operatorName  操作人姓名
     * @param result        结果（0-失败，1-成功）
     * @param errorMessage  错误信息
     * @return 日志ID
     */
    Long logOperation(Long deviceId, Long channelId, String operationType,
                      Long operatorId, String operatorName, Integer result, String errorMessage);

    /**
     * 记录操作日志（包含设备名称和通道名称）
     *
     * @param deviceId      设备ID
     * @param deviceName    设备名称
     * @param channelId     通道ID
     * @param channelName   通道名称
     * @param operationType 操作类型
     * @param operatorId    操作人ID
     * @param operatorName  操作人姓名
     * @param result        结果（0-失败，1-成功）
     * @param errorMessage  错误信息
     * @return 日志ID
     */
    Long logOperation(Long deviceId, String deviceName, Long channelId, String channelName,
                      String operationType, Long operatorId, String operatorName, 
                      Integer result, String errorMessage);

    /**
     * 获取操作日志
     *
     * @param id 日志ID
     * @return 操作日志
     */
    IotAccessOperationLogDO getOperationLog(Long id);

    /**
     * 获取操作日志分页
     *
     * @param deviceId      设备ID
     * @param channelId     通道ID
     * @param operationType 操作类型
     * @param operatorId    操作人ID
     * @param result        结果
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param pageNo        页码
     * @param pageSize      每页大小
     * @return 操作日志分页
     */
    PageResult<IotAccessOperationLogDO> getOperationLogPage(Long deviceId, Long channelId, String operationType,
                                                             Long operatorId, Integer result,
                                                             LocalDateTime startTime, LocalDateTime endTime,
                                                             Integer pageNo, Integer pageSize);
    
    // ========== 授权操作日志方法（Requirements: 12.1, 12.2, 12.3） ==========
    
    /**
     * 记录授权下发操作日志
     * 
     * Requirements: 12.1, 12.2
     *
     * @param operationType      操作类型（auth_dispatch_group/auth_dispatch_person）
     * @param authTaskId         授权任务ID
     * @param targetPersonId     目标人员ID
     * @param targetPersonCode   目标人员编号
     * @param targetPersonName   目标人员姓名
     * @param deviceId           设备ID
     * @param deviceName         设备名称
     * @param channelId          通道ID
     * @param permissionGroupId  权限组ID（可空）
     * @param permissionGroupName 权限组名称（可空）
     * @param dispatchResult     下发结果
     * @return 日志ID
     */
    Long logAuthDispatch(String operationType, Long authTaskId,
                         Long targetPersonId, String targetPersonCode, String targetPersonName,
                         Long deviceId, String deviceName, Long channelId,
                         Long permissionGroupId, String permissionGroupName,
                         DispatchResult dispatchResult);
    
    /**
     * 记录授权撤销操作日志
     * 
     * Requirements: 12.1, 12.3
     *
     * @param authTaskId         授权任务ID
     * @param targetPersonId     目标人员ID
     * @param targetPersonCode   目标人员编号
     * @param targetPersonName   目标人员姓名
     * @param deviceId           设备ID
     * @param deviceName         设备名称
     * @param channelId          通道ID
     * @param dispatchResult     撤销结果
     * @return 日志ID
     */
    Long logAuthRevoke(Long authTaskId,
                       Long targetPersonId, String targetPersonCode, String targetPersonName,
                       Long deviceId, String deviceName, Long channelId,
                       DispatchResult dispatchResult);
    
    /**
     * 记录授权重试操作日志
     *
     * @param authTaskId         授权任务ID
     * @param targetPersonId     目标人员ID
     * @param targetPersonCode   目标人员编号
     * @param targetPersonName   目标人员姓名
     * @param deviceId           设备ID
     * @param deviceName         设备名称
     * @param channelId          通道ID
     * @param dispatchResult     重试结果
     * @return 日志ID
     */
    Long logAuthRetry(Long authTaskId,
                      Long targetPersonId, String targetPersonCode, String targetPersonName,
                      Long deviceId, String deviceName, Long channelId,
                      DispatchResult dispatchResult);
    
    /**
     * 获取授权操作日志分页（支持按人员、设备、时间范围筛选）
     * 
     * Requirements: 12.4
     *
     * @param pageReqVO 分页查询参数
     * @return 操作日志分页
     */
    PageResult<IotAccessOperationLogDO> getAuthOperationLogPage(IotAccessOperationLogPageReqVO pageReqVO);
    
    /**
     * 获取人员的授权操作日志
     * 
     * Requirements: 12.4
     *
     * @param personId  人员ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 操作日志列表
     */
    List<IotAccessOperationLogDO> getAuthLogsByPerson(Long personId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取设备的授权操作日志
     * 
     * Requirements: 12.4
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 操作日志列表
     */
    List<IotAccessOperationLogDO> getAuthLogsByDevice(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取授权任务的操作日志
     *
     * @param authTaskId 授权任务ID
     * @return 操作日志列表
     */
    List<IotAccessOperationLogDO> getAuthLogsByTaskId(Long authTaskId);

}
