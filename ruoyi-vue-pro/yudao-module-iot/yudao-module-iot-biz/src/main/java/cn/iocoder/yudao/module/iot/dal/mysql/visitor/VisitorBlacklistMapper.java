package cn.iocoder.yudao.module.iot.dal.mysql.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorBlacklistDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisitorBlacklistMapper extends BaseMapperX<VisitorBlacklistDO> {

    default PageResult<VisitorBlacklistDO> selectPage(cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.VisitorBlacklistPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VisitorBlacklistDO>()
                .likeIfPresent(VisitorBlacklistDO::getVisitorName, reqVO.getVisitorName())
                .eqIfPresent(VisitorBlacklistDO::getStatus, reqVO.getStatus())
                .orderByDesc(VisitorBlacklistDO::getId));
    }

}


























