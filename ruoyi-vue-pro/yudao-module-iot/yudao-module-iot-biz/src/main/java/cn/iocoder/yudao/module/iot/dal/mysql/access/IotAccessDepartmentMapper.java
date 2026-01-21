package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessDepartmentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门禁组织架构 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessDepartmentMapper extends BaseMapperX<IotAccessDepartmentDO> {

    default List<IotAccessDepartmentDO> selectList(String deptName, Integer status) {
        return selectList(new LambdaQueryWrapperX<IotAccessDepartmentDO>()
                .likeIfPresent(IotAccessDepartmentDO::getDeptName, deptName)
                .eqIfPresent(IotAccessDepartmentDO::getStatus, status)
                .orderByAsc(IotAccessDepartmentDO::getSort)
                .orderByAsc(IotAccessDepartmentDO::getId));
    }

    default List<IotAccessDepartmentDO> selectListByParentId(Long parentId) {
        return selectList(IotAccessDepartmentDO::getParentId, parentId);
    }

    default IotAccessDepartmentDO selectByDeptCode(String deptCode) {
        return selectOne(IotAccessDepartmentDO::getDeptCode, deptCode);
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(IotAccessDepartmentDO::getParentId, parentId);
    }

    /**
     * 根据父部门ID和部门名称查询部门
     * 用于校验同级部门名称唯一性
     */
    default IotAccessDepartmentDO selectByParentIdAndDeptName(Long parentId, String deptName) {
        return selectOne(new LambdaQueryWrapperX<IotAccessDepartmentDO>()
                .eq(IotAccessDepartmentDO::getParentId, parentId)
                .eq(IotAccessDepartmentDO::getDeptName, deptName));
    }

}
