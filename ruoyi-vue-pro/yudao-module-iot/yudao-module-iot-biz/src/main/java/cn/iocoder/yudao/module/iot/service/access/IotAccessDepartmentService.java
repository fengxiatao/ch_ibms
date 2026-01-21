package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessDepartmentDO;

import java.util.List;

/**
 * 门禁组织架构 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAccessDepartmentService {

    /**
     * 创建部门
     *
     * @param department 部门信息
     * @return 部门ID
     */
    Long createDepartment(IotAccessDepartmentDO department);

    /**
     * 更新部门
     *
     * @param department 部门信息
     */
    void updateDepartment(IotAccessDepartmentDO department);

    /**
     * 删除部门
     *
     * @param id 部门ID
     */
    void deleteDepartment(Long id);

    /**
     * 获取部门
     *
     * @param id 部门ID
     * @return 部门信息
     */
    IotAccessDepartmentDO getDepartment(Long id);

    /**
     * 获取部门列表
     *
     * @param deptName 部门名称（模糊查询）
     * @param status   状态
     * @return 部门列表
     */
    List<IotAccessDepartmentDO> getDepartmentList(String deptName, Integer status);

    /**
     * 获取子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<IotAccessDepartmentDO> getChildDepartments(Long parentId);

    /**
     * 获取部门树
     *
     * @return 部门树（根节点列表）
     */
    List<IotAccessDepartmentDO> getDepartmentTree();

    /**
     * 校验部门编码是否唯一
     *
     * @param deptCode 部门编码
     * @param excludeId 排除的部门ID（用于更新时）
     * @return 是否唯一
     */
    boolean isDeptCodeUnique(String deptCode, Long excludeId);

    /**
     * 校验部门是否有子部门
     *
     * @param id 部门ID
     * @return 是否有子部门
     */
    boolean hasChildDepartment(Long id);

    /**
     * 校验部门是否有人员
     *
     * @param id 部门ID
     * @return 是否有人员
     */
    boolean hasPersonInDepartment(Long id);

    /**
     * 获取部门及其所有子孙部门的ID列表
     *
     * @param deptId 部门ID
     * @return 部门ID列表（包含自身和所有子孙部门）
     */
    List<Long> getDeptAndChildrenIds(Long deptId);

}
