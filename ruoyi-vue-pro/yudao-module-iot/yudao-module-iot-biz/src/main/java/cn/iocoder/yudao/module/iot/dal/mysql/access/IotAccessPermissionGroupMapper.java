package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门禁权限组 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessPermissionGroupMapper extends BaseMapperX<IotAccessPermissionGroupDO> {

    default PageResult<IotAccessPermissionGroupDO> selectPage(String groupName, Integer status,
                                                               Integer pageNo, Integer pageSize) {
        cn.iocoder.yudao.framework.common.pojo.PageParam pageParam = new cn.iocoder.yudao.framework.common.pojo.PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam,
                new LambdaQueryWrapperX<IotAccessPermissionGroupDO>()
                .likeIfPresent(IotAccessPermissionGroupDO::getGroupName, groupName)
                .eqIfPresent(IotAccessPermissionGroupDO::getStatus, status)
                .orderByDesc(IotAccessPermissionGroupDO::getId));
    }

    default List<IotAccessPermissionGroupDO> selectListByStatus(Integer status) {
        return selectList(IotAccessPermissionGroupDO::getStatus, status);
    }

}
