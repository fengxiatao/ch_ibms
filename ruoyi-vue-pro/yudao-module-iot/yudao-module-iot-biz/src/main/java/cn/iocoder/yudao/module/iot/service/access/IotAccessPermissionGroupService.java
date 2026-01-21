package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupPersonDO;

import java.util.List;

/**
 * 门禁权限组 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAccessPermissionGroupService {

    // ========== 权限组管理 ==========

    /**
     * 创建权限组
     *
     * @param group 权限组信息
     * @return 权限组ID
     */
    Long createPermissionGroup(IotAccessPermissionGroupDO group);

    /**
     * 更新权限组
     *
     * @param group 权限组信息
     */
    void updatePermissionGroup(IotAccessPermissionGroupDO group);

    /**
     * 删除权限组
     *
     * @param id 权限组ID
     */
    void deletePermissionGroup(Long id);

    /**
     * 获取权限组
     *
     * @param id 权限组ID
     * @return 权限组信息
     */
    IotAccessPermissionGroupDO getPermissionGroup(Long id);

    /**
     * 获取权限组分页
     *
     * @param groupName 权限组名称
     * @param status    状态
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @return 权限组分页
     */
    PageResult<IotAccessPermissionGroupDO> getPermissionGroupPage(String groupName, Integer status,
                                                                   Integer pageNo, Integer pageSize);

    /**
     * 获取所有启用的权限组
     *
     * @return 权限组列表
     */
    List<IotAccessPermissionGroupDO> getEnabledPermissionGroups();

    // ========== 设备关联 ==========

    /**
     * 设置权限组关联的设备
     *
     * @param groupId   权限组ID
     * @param deviceIds 设备ID列表
     */
    void setGroupDevices(Long groupId, List<Long> deviceIds);

    /**
     * 添加设备到权限组
     *
     * @param groupId   权限组ID
     * @param deviceId  设备ID
     * @param channelId 通道ID（可选）
     */
    void addDeviceToGroup(Long groupId, Long deviceId, Long channelId);

    /**
     * 从权限组移除设备
     *
     * @param groupId  权限组ID
     * @param deviceId 设备ID
     */
    void removeDeviceFromGroup(Long groupId, Long deviceId);

    /**
     * 获取权限组关联的设备
     *
     * @param groupId 权限组ID
     * @return 设备关联列表
     */
    List<IotAccessPermissionGroupDeviceDO> getGroupDevices(Long groupId);

    /**
     * 获取设备关联的权限组
     * 
     * 反向查询：根据设备ID查询所有关联该设备的权限组
     *
     * @param deviceId 设备ID
     * @return 权限组设备关联列表
     */
    List<IotAccessPermissionGroupDeviceDO> getDeviceGroups(Long deviceId);

    // ========== 人员关联 ==========

    /**
     * 添加人员到权限组
     *
     * @param groupId   权限组ID
     * @param personIds 人员ID列表
     */
    void addPersonsToGroup(Long groupId, List<Long> personIds);

    /**
     * 从权限组移除人员
     *
     * @param groupId   权限组ID
     * @param personIds 人员ID列表
     */
    void removePersonsFromGroup(Long groupId, List<Long> personIds);

    /**
     * 获取权限组关联的人员
     *
     * @param groupId 权限组ID
     * @return 人员关联列表
     */
    List<IotAccessPermissionGroupPersonDO> getGroupPersons(Long groupId);

    /**
     * 获取人员所属的权限组
     *
     * @param personId 人员ID
     * @return 权限组关联列表
     */
    List<IotAccessPermissionGroupPersonDO> getPersonGroups(Long personId);

    /**
     * 获取权限组人员数量
     *
     * @param groupId 权限组ID
     * @return 人员数量
     */
    Long getGroupPersonCount(Long groupId);

    // ========== 便捷方法 ==========

    /**
     * 添加设备到权限组（批量）
     *
     * @param groupId    权限组ID
     * @param deviceIds  设备ID列表
     * @param channelIds 通道ID列表（可选）
     */
    void addDevices(Long groupId, List<Long> deviceIds, List<Long> channelIds);

    /**
     * 更新权限组设备关联
     *
     * @param groupId    权限组ID
     * @param deviceIds  设备ID列表
     * @param channelIds 通道ID列表（可选）
     */
    void updateDevices(Long groupId, List<Long> deviceIds, List<Long> channelIds);

    /**
     * 获取权限组设备数量
     *
     * @param groupId 权限组ID
     * @return 设备数量
     */
    Integer getDeviceCount(Long groupId);

    /**
     * 获取权限组人员数量
     *
     * @param groupId 权限组ID
     * @return 人员数量
     */
    Integer getPersonCount(Long groupId);

    /**
     * 获取权限组列表
     *
     * @param groupName 权限组名称（可选）
     * @param status    状态（可选）
     * @return 权限组列表
     */
    List<IotAccessPermissionGroupDO> getPermissionGroupList(String groupName, Integer status);

    /**
     * 添加人员到权限组
     *
     * @param groupId   权限组ID
     * @param personIds 人员ID列表
     */
    void addPersons(Long groupId, List<Long> personIds);

    /**
     * 从权限组移除人员
     *
     * @param groupId   权限组ID
     * @param personIds 人员ID列表
     */
    void removePersons(Long groupId, List<Long> personIds);

    /**
     * 获取权限组关联的设备列表（带详细信息）
     *
     * @param groupId 权限组ID
     * @return 设备VO列表
     */
    List<cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup.IotAccessPermissionGroupDeviceRespVO> getGroupDevicesWithDetail(Long groupId);

    /**
     * 获取权限组关联的人员列表（带详细信息）
     *
     * @param groupId 权限组ID
     * @return 人员VO列表
     */
    List<cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup.IotAccessPermissionGroupPersonRespVO> getGroupPersonsWithDetail(Long groupId);

    // ========== 带授权下发的人员管理 (Requirements: 1.1, 2.1) ==========

    /**
     * 添加人员到权限组并触发授权下发
     * 保存人员关联后，自动将人员权限下发到权限组关联的所有设备
     *
     * Requirements: 1.1, 1.4
     *
     * @param groupId   权限组ID
     * @param personIds 人员ID列表
     * @return 授权任务ID，用于查询下发进度
     */
    Long addPersonsWithDispatch(Long groupId, List<Long> personIds);

    /**
     * 从权限组移除人员并触发权限撤销
     * 删除人员关联后，自动撤销人员在权限组关联设备上的权限
     * 如果人员在其他权限组仍有相同设备权限，则保留该权限
     *
     * Requirements: 2.1
     *
     * @param groupId   权限组ID
     * @param personIds 人员ID列表
     * @return 撤销任务ID，用于查询撤销进度
     */
    Long removePersonsWithRevoke(Long groupId, List<Long> personIds);

}
