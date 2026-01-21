package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupPersonDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门禁权限组人员关联 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessPermissionGroupPersonMapper extends BaseMapperX<IotAccessPermissionGroupPersonDO> {

    default List<IotAccessPermissionGroupPersonDO> selectListByGroupId(Long groupId) {
        return selectList(IotAccessPermissionGroupPersonDO::getGroupId, groupId);
    }

    default List<IotAccessPermissionGroupPersonDO> selectListByPersonId(Long personId) {
        return selectList(IotAccessPermissionGroupPersonDO::getPersonId, personId);
    }

    default void deleteByGroupId(Long groupId) {
        delete(IotAccessPermissionGroupPersonDO::getGroupId, groupId);
    }

    default void deleteByPersonId(Long personId) {
        delete(IotAccessPermissionGroupPersonDO::getPersonId, personId);
    }

    default IotAccessPermissionGroupPersonDO selectByGroupIdAndPersonId(Long groupId, Long personId) {
        return selectOne(new LambdaQueryWrapperX<IotAccessPermissionGroupPersonDO>()
                .eq(IotAccessPermissionGroupPersonDO::getGroupId, groupId)
                .eq(IotAccessPermissionGroupPersonDO::getPersonId, personId));
    }

    /**
     * 查询指定权限组和人员的关联记录（包括已软删除的记录）
     * 用于检查唯一约束冲突
     *
     * @param groupId 权限组ID
     * @param personId 人员ID
     * @return 关联记录（包括已软删除的）
     */
    @org.apache.ibatis.annotations.Select("SELECT * FROM iot_access_permission_group_person WHERE group_id = #{groupId} AND person_id = #{personId} LIMIT 1")
    IotAccessPermissionGroupPersonDO selectByGroupIdAndPersonIdIncludeDeleted(@org.apache.ibatis.annotations.Param("groupId") Long groupId, 
                                                                               @org.apache.ibatis.annotations.Param("personId") Long personId);

    default Long selectCountByGroupId(Long groupId) {
        return selectCount(IotAccessPermissionGroupPersonDO::getGroupId, groupId);
    }

    /**
     * 物理删除指定权限组和人员的关联记录
     * 关联表不需要保留软删除记录，使用物理删除避免唯一约束冲突
     *
     * @param groupId 权限组ID
     * @param personId 人员ID
     * @return 删除的记录数
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM iot_access_permission_group_person WHERE group_id = #{groupId} AND person_id = #{personId}")
    int physicalDeleteByGroupIdAndPersonId(@org.apache.ibatis.annotations.Param("groupId") Long groupId, 
                                           @org.apache.ibatis.annotations.Param("personId") Long personId);

}
