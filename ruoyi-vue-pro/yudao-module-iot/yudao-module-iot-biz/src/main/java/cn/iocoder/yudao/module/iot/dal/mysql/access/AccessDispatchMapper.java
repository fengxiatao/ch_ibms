package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dispatch.AccessDispatchPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.AccessDispatchDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccessDispatchMapper extends BaseMapperX<AccessDispatchDO> {

    default PageResult<AccessDispatchDO> selectPage(AccessDispatchPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AccessDispatchDO>()
                .eqIfPresent(AccessDispatchDO::getDispatchType, reqVO.getDispatchType())
                .eqIfPresent(AccessDispatchDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(AccessDispatchDO::getPersonId, reqVO.getPersonId())
                .eqIfPresent(AccessDispatchDO::getDispatchStatus, reqVO.getDispatchStatus())
                .betweenIfPresent(AccessDispatchDO::getDispatchTime, reqVO.getDispatchTime())
                .orderByDesc(AccessDispatchDO::getId));
    }

}


























