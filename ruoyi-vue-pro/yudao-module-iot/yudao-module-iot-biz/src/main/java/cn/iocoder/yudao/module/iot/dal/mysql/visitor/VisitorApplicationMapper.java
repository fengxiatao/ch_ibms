package cn.iocoder.yudao.module.iot.dal.mysql.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorApplicationDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisitorApplicationMapper extends BaseMapperX<VisitorApplicationDO> {

    default PageResult<VisitorApplicationDO> selectPage(cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.VisitorApplicationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VisitorApplicationDO>()
                .likeIfPresent(VisitorApplicationDO::getVisitorName, reqVO.getVisitorName())
                .eqIfPresent(VisitorApplicationDO::getApplicationStatus, reqVO.getApplicationStatus())
                .betweenIfPresent(VisitorApplicationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(VisitorApplicationDO::getId));
    }

}


























