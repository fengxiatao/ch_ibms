package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessDepartmentDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessDepartmentMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁组织架构 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IotAccessDepartmentServiceImpl implements IotAccessDepartmentService {

    @Resource
    private IotAccessDepartmentMapper departmentMapper;

    @Resource
    private IotAccessPersonMapper personMapper;

    @Override
    public Long createDepartment(IotAccessDepartmentDO department) {
        // 自动生成唯一部门编码（如果未提供或为空）
        if (department.getDeptCode() == null || department.getDeptCode().trim().isEmpty()) {
            department.setDeptCode(generateUniqueDeptCode());
        } else {
            // 如果用户提供了编码，校验唯一性
            if (!isDeptCodeUnique(department.getDeptCode(), null)) {
                throw exception(ACCESS_DEPARTMENT_CODE_DUPLICATE);
            }
        }
        // 校验父部门存在
        Long parentId = department.getParentId() != null ? department.getParentId() : 0L;
        if (parentId > 0) {
            IotAccessDepartmentDO parent = departmentMapper.selectById(parentId);
            if (parent == null) {
                throw exception(ACCESS_DEPARTMENT_PARENT_NOT_EXISTS);
            }
        }
        // 校验同级部门名称唯一性
        if (!isDeptNameUniqueInSameLevel(parentId, department.getDeptName(), null)) {
            throw exception(ACCESS_DEPARTMENT_NAME_DUPLICATE);
        }
        departmentMapper.insert(department);
        return department.getId();
    }

    /**
     * 生成唯一的部门编码
     * 规则：DEPT + 时间戳(毫秒后6位) + 4位随机数
     * 示例：DEPT1234567890
     */
    private String generateUniqueDeptCode() {
        String code;
        int maxAttempts = 10;
        int attempts = 0;
        Random random = new Random();
        do {
            // 取时间戳后6位 + 4位随机数
            long timestamp = System.currentTimeMillis() % 1000000;
            int randomNum = random.nextInt(10000);
            code = String.format("DEPT%06d%04d", timestamp, randomNum);
            attempts++;
        } while (!isDeptCodeUnique(code, null) && attempts < maxAttempts);
        
        if (attempts >= maxAttempts) {
            // 如果多次尝试仍然重复，使用完整时间戳
            code = "DEPT" + System.currentTimeMillis();
        }
        return code;
    }

    @Override
    public void updateDepartment(IotAccessDepartmentDO department) {
        // 校验存在
        IotAccessDepartmentDO existingDept = departmentMapper.selectById(department.getId());
        if (existingDept == null) {
            throw exception(ACCESS_DEPARTMENT_NOT_EXISTS);
        }
        // 校验部门编码唯一性
        if (department.getDeptCode() != null && !isDeptCodeUnique(department.getDeptCode(), department.getId())) {
            throw exception(ACCESS_DEPARTMENT_CODE_DUPLICATE);
        }
        // 校验父部门存在且不能是自己
        Long parentId = department.getParentId() != null ? department.getParentId() : existingDept.getParentId();
        if (parentId != null) {
            if (parentId.equals(department.getId())) {
                throw exception(ACCESS_DEPARTMENT_PARENT_ERROR);
            }
            if (parentId > 0) {
                IotAccessDepartmentDO parent = departmentMapper.selectById(parentId);
                if (parent == null) {
                    throw exception(ACCESS_DEPARTMENT_PARENT_NOT_EXISTS);
                }
            }
        }
        // 校验同级部门名称唯一性（如果名称有变化）
        String deptName = department.getDeptName() != null ? department.getDeptName() : existingDept.getDeptName();
        Long effectiveParentId = parentId != null ? parentId : 0L;
        if (!isDeptNameUniqueInSameLevel(effectiveParentId, deptName, department.getId())) {
            throw exception(ACCESS_DEPARTMENT_NAME_DUPLICATE);
        }
        departmentMapper.updateById(department);
    }


    @Override
    public void deleteDepartment(Long id) {
        // 校验存在
        validateDepartmentExists(id);
        // 校验是否有子部门
        if (hasChildDepartment(id)) {
            throw exception(ACCESS_DEPARTMENT_HAS_CHILDREN);
        }
        // 校验是否有人员
        if (hasPersonInDepartment(id)) {
            throw exception(ACCESS_DEPARTMENT_HAS_PERSON);
        }
        departmentMapper.deleteById(id);
    }

    @Override
    public IotAccessDepartmentDO getDepartment(Long id) {
        return departmentMapper.selectById(id);
    }

    @Override
    public List<IotAccessDepartmentDO> getDepartmentList(String deptName, Integer status) {
        return departmentMapper.selectList(deptName, status);
    }

    @Override
    public List<IotAccessDepartmentDO> getChildDepartments(Long parentId) {
        return departmentMapper.selectListByParentId(parentId);
    }

    @Override
    public List<IotAccessDepartmentDO> getDepartmentTree() {
        // 获取所有部门
        List<IotAccessDepartmentDO> allDepts = departmentMapper.selectList((String) null, (Integer) null);
        // 构建树形结构
        return buildTree(allDepts, 0L);
    }

    @Override
    public boolean isDeptCodeUnique(String deptCode, Long excludeId) {
        IotAccessDepartmentDO dept = departmentMapper.selectByDeptCode(deptCode);
        if (dept == null) {
            return true;
        }
        return excludeId != null && excludeId.equals(dept.getId());
    }

    /**
     * 校验同级部门名称唯一性
     * @param parentId 父部门ID
     * @param deptName 部门名称
     * @param excludeId 排除的部门ID（用于更新时排除自己）
     * @return 是否唯一
     */
    private boolean isDeptNameUniqueInSameLevel(Long parentId, String deptName, Long excludeId) {
        IotAccessDepartmentDO dept = departmentMapper.selectByParentIdAndDeptName(parentId, deptName);
        if (dept == null) {
            return true;
        }
        return excludeId != null && excludeId.equals(dept.getId());
    }

    @Override
    public boolean hasChildDepartment(Long id) {
        Long count = departmentMapper.selectCountByParentId(id);
        return count != null && count > 0;
    }

    @Override
    public boolean hasPersonInDepartment(Long id) {
        Long count = personMapper.selectCountByDeptId(id);
        return count != null && count > 0;
    }

    private void validateDepartmentExists(Long id) {
        if (departmentMapper.selectById(id) == null) {
            throw exception(ACCESS_DEPARTMENT_NOT_EXISTS);
        }
    }

    @Override
    public List<Long> getDeptAndChildrenIds(Long deptId) {
        List<Long> result = new ArrayList<>();
        if (deptId == null) {
            return result;
        }
        result.add(deptId);
        // 递归获取所有子孙部门ID
        collectChildDeptIds(deptId, result);
        return result;
    }

    /**
     * 递归收集子部门ID
     */
    private void collectChildDeptIds(Long parentId, List<Long> result) {
        List<IotAccessDepartmentDO> children = departmentMapper.selectListByParentId(parentId);
        if (children != null && !children.isEmpty()) {
            for (IotAccessDepartmentDO child : children) {
                result.add(child.getId());
                collectChildDeptIds(child.getId(), result);
            }
        }
    }

    /**
     * 构建部门树
     */
    private List<IotAccessDepartmentDO> buildTree(List<IotAccessDepartmentDO> allDepts, Long parentId) {
        List<IotAccessDepartmentDO> result = new ArrayList<>();
        for (IotAccessDepartmentDO dept : allDepts) {
            if (parentId.equals(dept.getParentId())) {
                // 递归构建子节点
                List<IotAccessDepartmentDO> children = buildTree(allDepts, dept.getId());
                dept.setChildren(children.isEmpty() ? null : children);
                // 统计人员数量
                Long personCount = personMapper.selectCountByDeptId(dept.getId());
                dept.setPersonCount(personCount != null ? personCount.intValue() : 0);
                result.add(dept);
            }
        }
        return result;
    }

}
