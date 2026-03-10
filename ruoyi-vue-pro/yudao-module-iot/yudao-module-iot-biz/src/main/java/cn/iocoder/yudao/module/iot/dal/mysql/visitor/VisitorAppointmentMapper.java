package cn.iocoder.yudao.module.iot.dal.mysql.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorAppointmentDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface VisitorAppointmentMapper extends BaseMapperX<VisitorAppointmentDO> {

    default PageResult<VisitorAppointmentDO> selectPage(VisitorAppointmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VisitorAppointmentDO>()
                .likeIfPresent(VisitorAppointmentDO::getName, reqVO.getName())
                .likeIfPresent(VisitorAppointmentDO::getPhone, reqVO.getPhone())
                .eqIfPresent(VisitorAppointmentDO::getType, reqVO.getType())
                .eqIfPresent(VisitorAppointmentDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(VisitorAppointmentDO::getVisitTime, reqVO.getVisitTime())
                .orderByDesc(VisitorAppointmentDO::getId));
    }

    default Long countByStatusAndVisitTimeRange(String status, LocalDateTime start, LocalDateTime end) {
        return selectCount(new LambdaQueryWrapperX<VisitorAppointmentDO>()
                .eqIfPresent(VisitorAppointmentDO::getStatus, status)
                .betweenIfPresent(VisitorAppointmentDO::getVisitTime, new LocalDateTime[]{start, end}));
    }

    default Long countCurrentVisiting(LocalDateTime dayStart, LocalDateTime dayEnd) {
        return selectCount(new LambdaQueryWrapperX<VisitorAppointmentDO>()
                .betweenIfPresent(VisitorAppointmentDO::getVisitTime, new LocalDateTime[]{dayStart, dayEnd})
                .isNotNull(VisitorAppointmentDO::getSignInTime)
                .isNull(VisitorAppointmentDO::getSignOutTime));
    }

    default Long countMonthlyTotal(LocalDateTime monthStart, LocalDateTime monthEnd) {
        return selectCount(new LambdaQueryWrapperX<VisitorAppointmentDO>()
                .betweenIfPresent(VisitorAppointmentDO::getVisitTime, new LocalDateTime[]{monthStart, monthEnd}));
    }

    /** 今日已处理数量：approvalTime 在当天且 status 为 approved 或 rejected */
    default Long countTodayProcessed(LocalDateTime todayStart, LocalDateTime todayEnd) {
        return selectCount(new LambdaQueryWrapperX<VisitorAppointmentDO>()
                .between(VisitorAppointmentDO::getApprovalTime, todayStart, todayEnd)
                .in(VisitorAppointmentDO::getStatus, "approved", "rejected"));
    }
}

