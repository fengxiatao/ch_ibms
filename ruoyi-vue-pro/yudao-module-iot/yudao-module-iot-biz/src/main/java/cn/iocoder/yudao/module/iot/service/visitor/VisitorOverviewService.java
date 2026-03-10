package cn.iocoder.yudao.module.iot.service.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.abnormal.VisitorAbnormalEventPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.overview.VisitorDashboardRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.overview.VisitorOverviewStatsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorAbnormalEventDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorAppointmentDO;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

public interface VisitorOverviewService {

    VisitorOverviewStatsRespVO getStats();

    VisitorDashboardRespVO getDashboard(LocalDate dateFrom, LocalDate dateTo);

    PageResult<VisitorAppointmentDO> pageTodayVisiting(@Valid VisitorAppointmentPageReqVO reqVO);

    PageResult<VisitorAppointmentDO> pageHistory(@Valid VisitorAppointmentPageReqVO reqVO);

    PageResult<VisitorAbnormalEventDO> pageAbnormal(@Valid VisitorAbnormalEventPageReqVO reqVO);
}

