package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.service.access.dto.DispatchResult;

import java.util.List;

/**
 * 授权下发服务接口
 * 
 * 负责将人员的通行权限（凭证信息）同步到门禁设备
 * 支持权限组批量下发、单人下发、权限撤销、失败重试等功能
 * 
 * Requirements: 1.1, 2.1, 2.3, 7.2, 8.1, 8.2
 *
 * @author Kiro
 */
public interface IotAccessAuthDispatchService {

    /**
     * 执行权限组下发
     * 将权限组中的所有人员权限下发到关联的设备
     * 
     * @param groupId 权限组ID
     * @return 创建的任务ID
     */
    Long dispatchPermissionGroup(Long groupId);

    /**
     * 执行单人下发
     * 将单个人员的权限下发到其关联的所有设备
     * 
     * @param personId 人员ID
     * @return 创建的任务ID
     */
    Long dispatchPerson(Long personId);

    /**
     * 下发单个人员到单个设备
     * 
     * @param personId  人员ID
     * @param deviceId  设备ID
     * @param channelId 通道ID（可选）
     * @return 下发结果
     */
    DispatchResult dispatchPersonToDevice(Long personId, Long deviceId, Long channelId);

    /**
     * 撤销人员权限
     * 从所有设备删除该人员的权限
     * 
     * @param personId 人员ID
     * @return 创建的任务ID
     */
    Long revokePerson(Long personId);

    /**
     * 批量撤销人员权限
     * 从所有设备删除多个人员的权限
     * 
     * Requirements: 8.1, 8.2
     * 
     * @param personIds 人员ID列表
     * @return 创建的任务ID
     */
    Long revokePersons(List<Long> personIds);

    /**
     * 撤销人员在指定设备的权限
     * 
     * Requirements: 8.1, 8.2
     * 
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param channelId 通道ID（可选）
     * @return 撤销结果
     */
    DispatchResult revokePersonFromDevice(Long personId, Long deviceId, Long channelId);

    /**
     * 从权限组移除人员时撤销权限
     * 只撤销该权限组关联设备上的权限
     * 
     * Requirements: 8.1
     * 
     * @param personId 人员ID
     * @param groupId 权限组ID
     * @return 创建的任务ID
     */
    Long revokePersonFromGroup(Long personId, Long groupId);

    /**
     * 标记待撤销状态（设备离线时使用）
     * 设备上线后自动执行撤销
     * 
     * Requirements: 8.5
     * 
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param channelId 通道ID
     */
    void markPendingRevoke(Long personId, Long deviceId, Long channelId);

    /**
     * 执行待撤销的任务（设备上线时调用）
     * 
     * Requirements: 8.5
     * 
     * @param deviceId 设备ID
     */
    void executePendingRevokes(Long deviceId);

    /**
     * 重试失败的任务明细
     * 只重新下发失败的明细
     * 
     * @param taskId 任务ID
     * @return 新创建的重试任务ID
     */
    Long retryFailedDetails(Long taskId);

    /**
     * 异步执行任务
     * 在后台线程中执行任务的所有明细
     * 
     * @param taskId 任务ID
     */
    void executeTaskAsync(Long taskId);

    /**
     * 异步执行下发（事件驱动模式）
     * 由事件监听器调用，在事务提交后执行
     * 
     * @param taskId 任务ID
     */
    void executeDispatchAsync(Long taskId);

    /**
     * 异步执行撤销（事件驱动模式）
     * 由事件监听器调用，在事务提交后执行
     * 
     * @param taskId 任务ID
     */
    void executeRevokeDispatchAsync(Long taskId);

    /**
     * 取消正在执行的任务
     * 
     * @param taskId 任务ID
     * @return 是否成功取消
     */
    boolean cancelTask(Long taskId);

    // ========== 增量授权支持 (Requirements: 15.1, 15.2, 15.3, 15.4) ==========

    /**
     * 增量下发权限组
     * 只下发新增人员或凭证变更的人员
     * 
     * Requirements: 15.1, 15.2
     * 
     * @param groupId 权限组ID
     * @return 创建的任务ID
     */
    Long dispatchPermissionGroupIncremental(Long groupId);

    /**
     * 增量下发单人
     * 只下发变更的凭证类型
     * 
     * Requirements: 15.3
     * 
     * @param personId 人员ID
     * @return 创建的任务ID，如果无需下发则返回null
     */
    Long dispatchPersonIncremental(Long personId);

    /**
     * 检测人员凭证是否有变更
     * 
     * Requirements: 15.4
     * 
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @return true-有变更需要下发，false-无变更
     */
    boolean hasCredentialChanged(Long personId, Long deviceId, Long channelId);

    /**
     * 计算并更新人员在设备上的凭证哈希
     * 
     * Requirements: 15.3, 15.4
     * 
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @return 计算的哈希值
     */
    String updateCredentialHash(Long personId, Long deviceId, Long channelId);

    /**
     * 权限组添加新设备时的增量下发
     * 将所有人员权限下发到新设备
     * 
     * Requirements: 15.2
     * 
     * @param groupId 权限组ID
     * @param deviceId 新设备ID
     * @param channelId 通道ID
     * @return 创建的任务ID
     */
    Long dispatchGroupToNewDevice(Long groupId, Long deviceId, Long channelId);

    // ========== 权限组人员变更触发下发 (Requirements: 1.1, 1.2, 2.1, 2.2) ==========

    /**
     * 下发权限组中指定人员的权限
     * 当人员被添加到权限组时调用，将人员权限下发到权限组关联的所有设备
     * 
     * Requirements: 1.1, 1.2
     * 
     * @param groupId 权限组ID
     * @param personIds 人员ID列表
     * @return 创建的任务ID
     */
    Long dispatchPermissionGroupPersons(Long groupId, List<Long> personIds);

    /**
     * 撤销权限组中指定人员的权限
     * 当人员从权限组移除时调用，检查人员是否在其他权限组有相同设备权限
     * 只撤销无重叠的设备权限
     * 
     * Requirements: 2.1, 2.2, 2.4
     * 
     * @param groupId 权限组ID
     * @param personIds 人员ID列表
     * @return 创建的任务ID
     */
    Long revokePermissionGroupPersons(Long groupId, List<Long> personIds);

}
