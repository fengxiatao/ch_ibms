package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.authorization.AccessAuthorizationPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.AccessAuthorizationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccessAuthorizationMapper extends BaseMapperX<AccessAuthorizationDO> {

    default PageResult<AccessAuthorizationDO> selectPage(AccessAuthorizationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AccessAuthorizationDO>()
                .eqIfPresent(AccessAuthorizationDO::getAuthType, reqVO.getAuthType())
                .eqIfPresent(AccessAuthorizationDO::getOrgId, reqVO.getOrgId())
                .eqIfPresent(AccessAuthorizationDO::getPersonId, reqVO.getPersonId())
                .eqIfPresent(AccessAuthorizationDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(AccessAuthorizationDO::getDoorGroupId, reqVO.getDoorGroupId())
                .eqIfPresent(AccessAuthorizationDO::getAuthStatus, reqVO.getAuthStatus())
                .betweenIfPresent(AccessAuthorizationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AccessAuthorizationDO::getId));
    }

    default List<AccessAuthorizationDO> selectListByPersonId(Long personId) {
        return selectList(AccessAuthorizationDO::getPersonId, personId);
    }

}







