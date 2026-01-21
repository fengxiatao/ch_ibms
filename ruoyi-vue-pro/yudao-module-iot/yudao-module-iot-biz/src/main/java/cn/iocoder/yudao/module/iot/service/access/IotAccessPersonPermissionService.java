package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.personpermission.*;

import java.util.List;

/**
 * 门禁人员权限配置 Service 接口
 *
 * @author 智能化系统
 */
public interface IotAccessPersonPermissionService {

    /**
     * 按权限组分配权限
     *
     * @param personId 人员ID
     * @param groupIds 权限组ID列表
     */
    void assignByGroup(Long personId, List<Long> groupIds);

    /**
     * 按权限组分配权限并触发授权下发
     * 会向权限组关联的所有设备下发该人员的凭证
     *
     * @param personId 人员ID
     * @param groupIds 权限组ID列表
     * @return 授权任务ID
     */
    Long assignByGroupWithDispatch(Long personId, List<Long> groupIds);

    /**
     * 按设备分配权限
     *
     * @param personId  人员ID
     * @param deviceIds 设备ID列表
     */
    void assignByDevice(Long personId, List<Long> deviceIds);

    /**
     * 批量按权限组分配权限
     *
     * @param personIds 人员ID列表
     * @param groupIds  权限组ID列表
     */
    void batchAssignByGroup(List<Long> personIds, List<Long> groupIds);

    /**
     * 批量按设备分配权限
     *
     * @param personIds 人员ID列表
     * @param deviceIds 设备ID列表
     */
    void batchAssignByDevice(List<Long> personIds, List<Long> deviceIds);

    /**
     * 获取人员权限信息
     *
     * @param personId 人员ID
     * @return 人员权限信息
     */
    IotAccessPersonPermissionRespVO getPersonPermission(Long personId);

    /**
     * 移除人员权限组
     *
     * @param personId 人员ID
     * @param groupIds 权限组ID列表
     */
    void removeGroups(Long personId, List<Long> groupIds);

    /**
     * 移除人员权限组并触发权限撤销
     * 会从权限组关联的所有设备撤销该人员的凭证
     *
     * @param personId 人员ID
     * @param groupIds 权限组ID列表
     * @return 撤销任务ID
     */
    Long removeGroupsWithRevoke(Long personId, List<Long> groupIds);

    /**
     * 移除人员设备权限
     *
     * @param personId  人员ID
     * @param deviceIds 设备ID列表
     */
    void removeDevices(Long personId, List<Long> deviceIds);

    /**
     * 触发权限下发任务
     *
     * @param personId 人员ID
     */
    void triggerAuthTask(Long personId);

}
