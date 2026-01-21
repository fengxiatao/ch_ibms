package cn.iocoder.yudao.module.iot.dal.mysql.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorRecordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisitorRecordMapper extends BaseMapperX<VisitorRecordDO> {

    default PageResult<VisitorRecordDO> selectPage(cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.VisitorRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VisitorRecordDO>()
                .likeIfPresent(VisitorRecordDO::getVisitorName, reqVO.getVisitorName())
                .eqIfPresent(VisitorRecordDO::getVisitorStatus, reqVO.getVisitorStatus())
                .betweenIfPresent(VisitorRecordDO::getArrivalTime, reqVO.getArrivalTime())
                .orderByDesc(VisitorRecordDO::getId));
    }

}


























