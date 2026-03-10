package cn.iocoder.yudao.module.iot.dal.mysql.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.abnormal.VisitorAbnormalEventPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorAbnormalEventDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisitorAbnormalEventMapper extends BaseMapperX<VisitorAbnormalEventDO> {

    default PageResult<VisitorAbnormalEventDO> selectPage(VisitorAbnormalEventPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VisitorAbnormalEventDO>()
                .eqIfPresent(VisitorAbnormalEventDO::getRiskLevel, reqVO.getRiskLevel())
                .eqIfPresent(VisitorAbnormalEventDO::getAbnormalType, reqVO.getAbnormalType())
                .eqIfPresent(VisitorAbnormalEventDO::getHandled, reqVO.getHandled())
                .betweenIfPresent(VisitorAbnormalEventDO::getEventTime, reqVO.getEventTime())
                .orderByDesc(VisitorAbnormalEventDO::getId));
    }

    default Long selectCount() {
        return selectCount(new LambdaQueryWrapperX<>());
    }
}

