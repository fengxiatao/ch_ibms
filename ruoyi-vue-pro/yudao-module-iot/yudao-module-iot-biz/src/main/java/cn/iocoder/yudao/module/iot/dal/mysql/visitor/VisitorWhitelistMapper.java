package cn.iocoder.yudao.module.iot.dal.mysql.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorWhitelistDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisitorWhitelistMapper extends BaseMapperX<VisitorWhitelistDO> {

    default PageResult<VisitorWhitelistDO> selectPage(cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.VisitorWhitelistPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VisitorWhitelistDO>()
                .likeIfPresent(VisitorWhitelistDO::getVisitorName, reqVO.getVisitorName())
                .eqIfPresent(VisitorWhitelistDO::getStatus, reqVO.getStatus())
                .orderByDesc(VisitorWhitelistDO::getId));
    }

}


























