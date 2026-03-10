package cn.iocoder.yudao.module.iot.service.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentApproveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorAppointmentDO;
import jakarta.validation.Valid;

/**
 * 新访客管理 - 访客预约 Service
 */
public interface VisitorAppointmentService {

    Long create(@Valid VisitorAppointmentSaveReqVO reqVO);

    void update(@Valid VisitorAppointmentSaveReqVO reqVO);

    void delete(Long id);

    VisitorAppointmentDO get(Long id);

    PageResult<VisitorAppointmentDO> page(@Valid VisitorAppointmentPageReqVO reqVO);

    void approve(@Valid VisitorAppointmentApproveReqVO reqVO);

    void signOut(Long id);
}

