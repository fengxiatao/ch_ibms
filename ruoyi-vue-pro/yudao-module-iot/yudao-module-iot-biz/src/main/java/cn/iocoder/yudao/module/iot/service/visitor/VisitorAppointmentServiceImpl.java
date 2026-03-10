package cn.iocoder.yudao.module.iot.service.visitor;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentApproveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorAppointmentDO;
import cn.iocoder.yudao.module.iot.dal.mysql.visitor.VisitorAppointmentMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

@Service
@Validated
public class VisitorAppointmentServiceImpl implements VisitorAppointmentService {

    @Resource
    private VisitorAppointmentMapper visitorAppointmentMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public Long create(VisitorAppointmentSaveReqVO reqVO) {
        VisitorAppointmentDO appointment = VisitorAppointmentDO.builder()
                .name(reqVO.getName())
                .phone(reqVO.getPhone())
                .type(reqVO.getType())
                .company(reqVO.getCompany())
                .host(reqVO.getHost())
                .hostDept(reqVO.getHostDept())
                .visitTime(reqVO.getVisitTime())
                .reason(reqVO.getReason())
                .areas(toJson(reqVO.getAreas()))
                .idCard(reqVO.getIdCard())
                .carNo(reqVO.getCarNo())
                .remark(reqVO.getRemark())
                .status("pending")
                .build();
        // 多租户默认：若未设置租户，使用当前上下文
        if (appointment.getTenantId() == null) {
            appointment.setTenantId(TenantContextHolder.getTenantId());
        }
        visitorAppointmentMapper.insert(appointment);
        return appointment.getId();
    }

    @Override
    public void update(VisitorAppointmentSaveReqVO reqVO) {
        VisitorAppointmentDO db = visitorAppointmentMapper.selectById(reqVO.getId());
        if (db == null) {
            throw ServiceExceptionUtil.exception(BAD_REQUEST, "预约记录不存在");
        }
        // 已审批的不允许修改基础信息（避免审批过程数据漂移）
        if (!"pending".equals(db.getStatus())) {
            throw ServiceExceptionUtil.exception(BAD_REQUEST, "仅待审批状态允许修改");
        }
        VisitorAppointmentDO update = VisitorAppointmentDO.builder()
                .id(reqVO.getId())
                .name(reqVO.getName())
                .phone(reqVO.getPhone())
                .type(reqVO.getType())
                .company(reqVO.getCompany())
                .host(reqVO.getHost())
                .hostDept(reqVO.getHostDept())
                .visitTime(reqVO.getVisitTime())
                .reason(reqVO.getReason())
                .areas(toJson(reqVO.getAreas()))
                .idCard(reqVO.getIdCard())
                .carNo(reqVO.getCarNo())
                .remark(reqVO.getRemark())
                .build();
        visitorAppointmentMapper.updateById(update);
    }

    @Override
    public void delete(Long id) {
        visitorAppointmentMapper.deleteById(id);
    }

    @Override
    public VisitorAppointmentDO get(Long id) {
        return visitorAppointmentMapper.selectById(id);
    }

    @Override
    public PageResult<VisitorAppointmentDO> page(VisitorAppointmentPageReqVO reqVO) {
        return visitorAppointmentMapper.selectPage(reqVO);
    }

    @Override
    public void approve(VisitorAppointmentApproveReqVO reqVO) {
        VisitorAppointmentDO db = visitorAppointmentMapper.selectById(reqVO.getId());
        if (db == null) {
            throw ServiceExceptionUtil.exception(BAD_REQUEST, "预约记录不存在");
        }
        if (!"pending".equals(db.getStatus())) {
            throw ServiceExceptionUtil.exception(BAD_REQUEST, "该预约已处理，无法重复审批");
        }
        String newStatus;
        if ("approve".equalsIgnoreCase(reqVO.getAction())) {
            newStatus = "approved";
        } else if ("reject".equalsIgnoreCase(reqVO.getAction())) {
            newStatus = "rejected";
        } else {
            throw ServiceExceptionUtil.exception(BAD_REQUEST, "非法审批动作");
        }
        LocalDateTime now = LocalDateTime.now();
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        visitorAppointmentMapper.update(null, new LambdaUpdateWrapper<VisitorAppointmentDO>()
                .eq(VisitorAppointmentDO::getId, reqVO.getId())
                .set(VisitorAppointmentDO::getStatus, newStatus)
                .set(VisitorAppointmentDO::getApprovalComment, reqVO.getComment())
                .set(VisitorAppointmentDO::getApprovalTime, now)
                .set(VisitorAppointmentDO::getApproverId, userId));
    }

    @Override
    public void signOut(Long id) {
        LocalDateTime now = LocalDateTime.now();
        visitorAppointmentMapper.update(null, new LambdaUpdateWrapper<VisitorAppointmentDO>()
                .eq(VisitorAppointmentDO::getId, id)
                .isNull(VisitorAppointmentDO::getSignOutTime)
                .set(VisitorAppointmentDO::getSignOutTime, now));
    }

    private String toJson(List<String> areas) {
        if (CollUtil.isEmpty(areas)) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(areas);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("areas 序列化失败", e);
        }
    }
}

