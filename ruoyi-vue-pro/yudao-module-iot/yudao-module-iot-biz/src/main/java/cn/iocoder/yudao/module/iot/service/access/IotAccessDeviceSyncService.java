package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.service.access.dto.DeviceSyncCheckResult;

import java.util.List;

/**
 * 门禁设备人员同步服务
 * 
 * 提供设备人员对账、清理、同步等功能，确保系统与设备数据一致性
 *
 * @author 长辉信息科技
 */
public interface IotAccessDeviceSyncService {

    /**
     * 对账检查 - 对比单个设备的人员数据
     * 
     * 查询设备上的用户列表，与系统中该设备关联的权限组人员对比，
     * 返回差异报告
     *
     * @param deviceId 设备ID
     * @return 对账结果
     */
    DeviceSyncCheckResult checkDevice(Long deviceId);

    /**
     * 批量对账检查 - 对比多个设备的人员数据
     *
     * @param deviceIds 设备ID列表
     * @return 对账结果列表
     */
    List<DeviceSyncCheckResult> checkDevices(List<Long> deviceIds);

    /**
     * 对账权限组关联的所有设备
     *
     * @param groupId 权限组ID
     * @return 对账结果列表
     */
    List<DeviceSyncCheckResult> checkByPermissionGroup(Long groupId);

    /**
     * 清理设备多余用户
     * 
     * 删除设备上存在但系统权限组中不存在的"野生用户"
     *
     * @param deviceId 设备ID
     * @return 清理结果，包含清理数量和失败信息
     */
    DeviceSyncCheckResult cleanDeviceExtraUsers(Long deviceId);

    /**
     * 补发缺失用户
     * 
     * 将系统权限组中存在但设备上不存在的人员下发到设备
     *
     * @param deviceId 设备ID
     * @return 补发结果
     */
    DeviceSyncCheckResult repairMissingUsers(Long deviceId);

    /**
     * 全量同步
     * 
     * 清空设备上的所有用户，然后重新下发权限组中的所有人员
     *
     * @param deviceId 设备ID
     * @return 同步结果
     */
    DeviceSyncCheckResult fullSync(Long deviceId);

    /**
     * 清理指定用户
     * 
     * 从设备上删除指定的用户
     *
     * @param deviceId 设备ID
     * @param userIds 要删除的用户ID列表（设备上的userId）
     * @return 清理结果
     */
    DeviceSyncCheckResult cleanSpecificUsers(Long deviceId, List<String> userIds);

    /**
     * 获取设备上的用户列表
     * 
     * 直接查询设备存储的用户信息
     *
     * @param deviceId 设备ID
     * @return 设备用户列表
     */
    List<DeviceSyncCheckResult.DeviceUserInfo> queryDeviceUsers(Long deviceId);

    /**
     * 获取系统应授权人员列表
     * 
     * 根据设备关联的权限组，获取应该下发到该设备的人员列表
     *
     * @param deviceId 设备ID
     * @return 系统人员列表
     */
    List<DeviceSyncCheckResult.PersonInfo> getSystemUsers(Long deviceId);

}
