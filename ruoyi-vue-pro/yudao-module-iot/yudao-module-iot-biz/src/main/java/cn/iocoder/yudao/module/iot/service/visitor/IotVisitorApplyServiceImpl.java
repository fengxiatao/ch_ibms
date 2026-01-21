package cn.iocoder.yudao.module.iot.service.visitor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessDepartmentDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorApplyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorAuthDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorAuthDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorDO;
import cn.iocoder.yudao.module.iot.dal.mysql.visitor.IotVisitorApplyMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.visitor.IotVisitorAuthDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.visitor.IotVisitorAuthMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.visitor.IotVisitorMapper;
import cn.iocoder.yudao.module.iot.service.access.IotAccessDepartmentService;
import cn.iocoder.yudao.module.iot.service.access.IotAccessPersonService;
import cn.iocoder.yudao.module.iot.service.visitor.event.VisitorAuthDispatchEvent;
import cn.iocoder.yudao.module.iot.service.visitor.event.VisitorAuthRevokeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 访客申请 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IotVisitorApplyServiceImpl implements IotVisitorApplyService {

    @Resource
    private IotVisitorMapper visitorMapper;

    @Resource
    private IotVisitorApplyMapper applyMapper;

    @Resource
    private IotVisitorAuthMapper authMapper;

    @Resource
    private IotVisitorAuthDeviceMapper authDeviceMapper;

    @Resource
    private IotAccessPersonService accessPersonService;

    @Resource
    private IotAccessDepartmentService accessDepartmentService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    /** 访客编号前缀 */
    private static final String VISITOR_CODE_PREFIX = "VIS";
    /** 申请编号前缀 */
    private static final String APPLY_CODE_PREFIX = "VA";

    // ========== 申请管理 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApply(IotVisitorApplyCreateReqVO reqVO) {
        // 1. 获取或创建访客
        Long visitorId = reqVO.getVisitorId();
        IotVisitorDO visitor;
        if (visitorId != null) {
            visitor = visitorMapper.selectById(visitorId);
            if (visitor == null) {
                throw exception(VISITOR_RECORD_NOT_EXISTS);
            }
        } else {
            // 根据手机号查找已有访客
            visitor = visitorMapper.selectByPhone(reqVO.getVisitorPhone());
            if (visitor == null) {
                // 创建新访客
                visitor = IotVisitorDO.builder()
                        .visitorCode(generateVisitorCode())
                        .visitorName(reqVO.getVisitorName())
                        .gender(reqVO.getGender())
                        .phone(reqVO.getVisitorPhone())
                        .idCard(reqVO.getIdCard())
                        .company(reqVO.getCompany())
                        .faceUrl(reqVO.getFaceUrl())
                        .build();
                visitorMapper.insert(visitor);
            }
            visitorId = visitor.getId();
        }

        // 2. 获取被访人信息
        IotAccessPersonDO visitee = accessPersonService.getPerson(reqVO.getVisiteeId());
        if (visitee == null) {
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
        String visiteeDeptName = null;
        if (visitee.getDeptId() != null) {
            IotAccessDepartmentDO dept = accessDepartmentService.getDepartment(visitee.getDeptId());
            if (dept != null) {
                visiteeDeptName = dept.getDeptName();
            }
        }

        // 3. 创建申请
        IotVisitorApplyDO apply = IotVisitorApplyDO.builder()
                .applyCode(generateApplyCode())
                .visitorId(visitorId)
                .visitorName(visitor.getVisitorName())
                .visitorPhone(visitor.getPhone())
                .visiteeId(visitee.getId())
                .visiteeName(visitee.getPersonName())
                .visiteeDeptId(visitee.getDeptId())
                .visiteeDeptName(visiteeDeptName)
                .visitReason(reqVO.getVisitReason())
                .visitStatus(IotVisitorApplyDO.VISIT_STATUS_WAITING)
                .planVisitTime(reqVO.getPlanVisitTime())
                .planLeaveTime(reqVO.getPlanLeaveTime())
                .applyTime(LocalDateTime.now())
                .approveStatus(IotVisitorApplyDO.APPROVE_STATUS_PENDING)
                .remark(reqVO.getRemark())
                .build();
        applyMapper.insert(apply);

        // 4. 如果提供了授权信息，创建授权记录
        if (CollUtil.isNotEmpty(reqVO.getDeviceIds()) || CollUtil.isNotEmpty(reqVO.getChannelIds())) {
            createAuthRecord(apply, visitor, reqVO);
        }

        log.info("[createApply] 创建访客申请成功, applyId={}, applyCode={}, visitorName={}",
                apply.getId(), apply.getApplyCode(), visitor.getVisitorName());
        return apply.getId();
    }

    /**
     * 创建授权记录
     */
    private void createAuthRecord(IotVisitorApplyDO apply, IotVisitorDO visitor, IotVisitorApplyCreateReqVO reqVO) {
        // 授权时间默认为来访时间范围
        LocalDateTime authStartTime = reqVO.getAuthStartTime() != null ? reqVO.getAuthStartTime() : reqVO.getPlanVisitTime();
        LocalDateTime authEndTime = reqVO.getAuthEndTime() != null ? reqVO.getAuthEndTime() :
                (reqVO.getPlanLeaveTime() != null ? reqVO.getPlanLeaveTime() : authStartTime.plusHours(8));

        IotVisitorAuthDO auth = IotVisitorAuthDO.builder()
                .applyId(apply.getId())
                .visitorId(visitor.getId())
                .visitorCode(visitor.getVisitorCode())
                .visitorName(visitor.getVisitorName())
                .cardNo(reqVO.getCardNo())
                .faceUrl(reqVO.getFaceUrl() != null ? reqVO.getFaceUrl() : visitor.getFaceUrl())
                .authStartTime(authStartTime)
                .authEndTime(authEndTime)
                .authStatus(IotVisitorAuthDO.AUTH_STATUS_PENDING)
                .build();
        authMapper.insert(auth);

        // 创建授权设备关联
        if (CollUtil.isNotEmpty(reqVO.getDeviceIds())) {
            for (Long deviceId : reqVO.getDeviceIds()) {
                IotVisitorAuthDeviceDO authDevice = IotVisitorAuthDeviceDO.builder()
                        .authId(auth.getId())
                        .applyId(apply.getId())
                        .visitorId(visitor.getId())
                        .deviceId(deviceId)
                        .dispatchStatus(IotVisitorAuthDeviceDO.STATUS_PENDING)
                        .build();
                authDeviceMapper.insert(authDevice);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApply(Long id, IotVisitorApplyCreateReqVO reqVO) {
        // 校验存在
        IotVisitorApplyDO apply = validateApplyExists(id);
        // 只有待审批状态才能修改
        if (apply.getApproveStatus() != IotVisitorApplyDO.APPROVE_STATUS_PENDING) {
            throw exception(VISITOR_APPLICATION_ALREADY_APPROVED);
        }

        // 更新申请信息
        apply.setVisitReason(reqVO.getVisitReason());
        apply.setPlanVisitTime(reqVO.getPlanVisitTime());
        apply.setPlanLeaveTime(reqVO.getPlanLeaveTime());
        apply.setRemark(reqVO.getRemark());
        applyMapper.updateById(apply);

        log.info("[updateApply] 更新访客申请成功, applyId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApply(Long id) {
        // 校验存在
        validateApplyExists(id);
        // 删除授权设备关联
        authDeviceMapper.deleteByApplyId(id);
        // 删除授权记录
        IotVisitorAuthDO auth = authMapper.selectByApplyId(id);
        if (auth != null) {
            authMapper.deleteById(auth.getId());
        }
        // 删除申请
        applyMapper.deleteById(id);

        log.info("[deleteApply] 删除访客申请成功, applyId={}", id);
    }

    @Override
    public IotVisitorApplyDO getApply(Long id) {
        return applyMapper.selectById(id);
    }

    @Override
    public IotVisitorApplyRespVO getApplyDetail(Long id) {
        IotVisitorApplyDO apply = validateApplyExists(id);
        return convertToRespVO(apply);
    }

    @Override
    public PageResult<IotVisitorApplyRespVO> getApplyPage(IotVisitorApplyPageReqVO reqVO) {
        PageResult<IotVisitorApplyDO> pageResult = applyMapper.selectPage(
                reqVO.getVisitorName(),
                reqVO.getVisiteeName(),
                reqVO.getVisitReason(),
                reqVO.getVisitStatus(),
                reqVO.getApproveStatus(),
                reqVO.getVisitTimeStart(),
                reqVO.getVisitTimeEnd(),
                reqVO.getPageNo(),
                reqVO.getPageSize()
        );
        // 转换为 RespVO
        List<IotVisitorApplyRespVO> list = new ArrayList<>();
        for (IotVisitorApplyDO apply : pageResult.getList()) {
            list.add(convertToRespVO(apply));
        }
        return new PageResult<>(list, pageResult.getTotal());
    }

    // ========== 审批操作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, String remark) {
        IotVisitorApplyDO apply = validateApplyExists(id);
        if (apply.getApproveStatus() != IotVisitorApplyDO.APPROVE_STATUS_PENDING) {
            throw exception(VISITOR_APPLICATION_ALREADY_APPROVED);
        }

        apply.setApproveStatus(IotVisitorApplyDO.APPROVE_STATUS_APPROVED);
        apply.setApproveTime(LocalDateTime.now());
        apply.setApproverId(SecurityFrameworkUtils.getLoginUserId());
        apply.setApproverName(SecurityFrameworkUtils.getLoginUserNickname());
        apply.setApproveRemark(remark);
        applyMapper.updateById(apply);

        log.info("[approve] 访客申请审批通过, applyId={}, approver={}", id, apply.getApproverName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String remark) {
        IotVisitorApplyDO apply = validateApplyExists(id);
        if (apply.getApproveStatus() != IotVisitorApplyDO.APPROVE_STATUS_PENDING) {
            throw exception(VISITOR_APPLICATION_ALREADY_APPROVED);
        }

        apply.setApproveStatus(IotVisitorApplyDO.APPROVE_STATUS_REJECTED);
        apply.setApproveTime(LocalDateTime.now());
        apply.setApproverId(SecurityFrameworkUtils.getLoginUserId());
        apply.setApproverName(SecurityFrameworkUtils.getLoginUserNickname());
        apply.setApproveRemark(remark);
        applyMapper.updateById(apply);

        log.info("[reject] 访客申请已拒绝, applyId={}, reason={}", id, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, String reason) {
        IotVisitorApplyDO apply = validateApplyExists(id);

        apply.setVisitStatus(IotVisitorApplyDO.VISIT_STATUS_CANCELLED);
        apply.setCancelReason(reason);
        applyMapper.updateById(apply);

        // 如果已下发权限，触发回收
        IotVisitorAuthDO auth = authMapper.selectByApplyId(id);
        if (auth != null && auth.getAuthStatus() == IotVisitorAuthDO.AUTH_STATUS_DISPATCHED) {
            revokeAuth(id);
        }

        log.info("[cancel] 访客申请已取消, applyId={}, reason={}", id, reason);
    }

    // ========== 签到签离 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long id) {
        IotVisitorApplyDO apply = validateApplyExists(id);

        apply.setVisitStatus(IotVisitorApplyDO.VISIT_STATUS_VISITING);
        apply.setActualVisitTime(LocalDateTime.now());
        applyMapper.updateById(apply);

        log.info("[checkIn] 访客签到成功, applyId={}, visitorName={}", id, apply.getVisitorName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOut(Long id) {
        IotVisitorApplyDO apply = validateApplyExists(id);

        apply.setVisitStatus(IotVisitorApplyDO.VISIT_STATUS_LEFT);
        apply.setActualLeaveTime(LocalDateTime.now());
        applyMapper.updateById(apply);

        // 触发权限回收
        IotVisitorAuthDO auth = authMapper.selectByApplyId(id);
        if (auth != null && auth.getAuthStatus() == IotVisitorAuthDO.AUTH_STATUS_DISPATCHED) {
            revokeAuth(id);
        }

        log.info("[checkOut] 访客签离成功, applyId={}, visitorName={}", id, apply.getVisitorName());
    }

    // ========== 权限下发/回收 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long dispatchAuth(IotVisitorAuthDispatchReqVO reqVO) {
        IotVisitorApplyDO apply = validateApplyExists(reqVO.getApplyId());

        // 获取或创建授权记录
        IotVisitorAuthDO auth = authMapper.selectByApplyId(reqVO.getApplyId());
        IotVisitorDO visitor = visitorMapper.selectById(apply.getVisitorId());

        if (auth == null) {
            // 创建授权记录
            LocalDateTime authStartTime = reqVO.getAuthStartTime() != null ? reqVO.getAuthStartTime() : LocalDateTime.now();
            LocalDateTime authEndTime = reqVO.getAuthEndTime() != null ? reqVO.getAuthEndTime() : authStartTime.plusHours(8);

            auth = IotVisitorAuthDO.builder()
                    .applyId(apply.getId())
                    .visitorId(apply.getVisitorId())
                    .visitorCode(visitor.getVisitorCode())
                    .visitorName(visitor.getVisitorName())
                    .cardNo(reqVO.getCardNo())
                    .faceUrl(reqVO.getFaceUrl())
                    .timeTemplateId(reqVO.getTimeTemplateId())
                    .authType(reqVO.getAuthType() != null ? reqVO.getAuthType() : IotVisitorAuthDO.AUTH_TYPE_TIME_RANGE)
                    .authStartTime(authStartTime)
                    .authEndTime(authEndTime)
                    .maxAccessCount(reqVO.getMaxAccessCount())
                    .usedAccessCount(0)
                    .dailyAccessLimit(reqVO.getDailyAccessLimit())
                    .dailyUsedCount(0)
                    .authStatus(IotVisitorAuthDO.AUTH_STATUS_DISPATCHING)
                    .dispatchTime(LocalDateTime.now())
                    .build();
            authMapper.insert(auth);
        } else {
            // 更新授权记录
            if (reqVO.getCardNo() != null) {
                auth.setCardNo(reqVO.getCardNo());
            }
            if (reqVO.getFaceUrl() != null) {
                auth.setFaceUrl(reqVO.getFaceUrl());
            }
            if (reqVO.getTimeTemplateId() != null) {
                auth.setTimeTemplateId(reqVO.getTimeTemplateId());
            }
            if (reqVO.getAuthType() != null) {
                auth.setAuthType(reqVO.getAuthType());
            }
            if (reqVO.getAuthStartTime() != null) {
                auth.setAuthStartTime(reqVO.getAuthStartTime());
            }
            if (reqVO.getAuthEndTime() != null) {
                auth.setAuthEndTime(reqVO.getAuthEndTime());
            }
            if (reqVO.getMaxAccessCount() != null) {
                auth.setMaxAccessCount(reqVO.getMaxAccessCount());
            }
            if (reqVO.getDailyAccessLimit() != null) {
                auth.setDailyAccessLimit(reqVO.getDailyAccessLimit());
            }
            auth.setAuthStatus(IotVisitorAuthDO.AUTH_STATUS_DISPATCHING);
            auth.setDispatchTime(LocalDateTime.now());
            authMapper.updateById(auth);
        }

        // 删除旧的设备关联，创建新的
        authDeviceMapper.deleteByAuthId(auth.getId());
        for (Long deviceId : reqVO.getDeviceIds()) {
            IotVisitorAuthDeviceDO authDevice = IotVisitorAuthDeviceDO.builder()
                    .authId(auth.getId())
                    .applyId(apply.getId())
                    .visitorId(apply.getVisitorId())
                    .deviceId(deviceId)
                    .dispatchStatus(IotVisitorAuthDeviceDO.STATUS_PROCESSING)
                    .dispatchTime(LocalDateTime.now())
                    .build();
            authDeviceMapper.insert(authDevice);
        }

        // 发布事件，触发异步下发到门禁设备（与现有门禁授权下发服务集成）
        eventPublisher.publishEvent(new VisitorAuthDispatchEvent(this, auth.getId(), apply.getId()));

        log.info("[dispatchAuth] 访客权限开始下发, applyId={}, authId={}, deviceCount={}",
                apply.getId(), auth.getId(), reqVO.getDeviceIds().size());

        return auth.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long revokeAuth(Long applyId) {
        validateApplyExists(applyId);
        IotVisitorAuthDO auth = authMapper.selectByApplyId(applyId);

        if (auth == null) {
            log.warn("[revokeAuth] 访客授权记录不存在, applyId={}", applyId);
            return null;
        }

        auth.setAuthStatus(IotVisitorAuthDO.AUTH_STATUS_REVOKED);
        auth.setRevokeTime(LocalDateTime.now());
        authMapper.updateById(auth);

        // 更新设备回收状态
        List<IotVisitorAuthDeviceDO> authDevices = authDeviceMapper.selectListByAuthId(auth.getId());
        for (IotVisitorAuthDeviceDO authDevice : authDevices) {
            authDevice.setRevokeStatus(IotVisitorAuthDeviceDO.STATUS_PROCESSING);
            authDevice.setRevokeTime(LocalDateTime.now());
            authDeviceMapper.updateById(authDevice);
        }

        // 发布事件，触发异步回收（与现有门禁授权撤销服务集成）
        eventPublisher.publishEvent(new VisitorAuthRevokeEvent(this, auth.getId(), applyId));

        log.info("[revokeAuth] 访客权限开始回收, applyId={}, authId={}", applyId, auth.getId());

        return auth.getId();
    }

    // ========== 统计 ==========

    @Override
    public IotVisitorStatisticsRespVO getStatistics() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        return IotVisitorStatisticsRespVO.builder()
                .currentVisitingCount(applyMapper.countByVisitStatus(IotVisitorApplyDO.VISIT_STATUS_VISITING))
                .todayVisitorCount(applyMapper.countByVisitTimeRange(todayStart, todayEnd))
                .totalVisitorCount(visitorMapper.selectCount())
                .pendingApproveCount(applyMapper.countByVisitStatus(IotVisitorApplyDO.APPROVE_STATUS_PENDING))
                .pendingDispatchCount(authMapper.selectCount(IotVisitorAuthDO::getAuthStatus, IotVisitorAuthDO.AUTH_STATUS_PENDING))
                .build();
    }

    // ========== 私有方法 ==========

    private IotVisitorApplyDO validateApplyExists(Long id) {
        IotVisitorApplyDO apply = applyMapper.selectById(id);
        if (apply == null) {
            throw exception(VISITOR_APPLICATION_NOT_EXISTS);
        }
        return apply;
    }

    /**
     * 生成访客编号
     * 格式：VIS + 日期 + 4位流水号，如 VIS202401080001
     */
    private synchronized String generateVisitorCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = VISITOR_CODE_PREFIX + dateStr;
        // 简化实现，使用雪花算法后4位
        return prefix + String.format("%04d", IdUtil.getSnowflakeNextId() % 10000);
    }

    /**
     * 生成申请编号
     * 格式：VA + 日期 + 4位流水号，如 VA202401080001
     */
    private synchronized String generateApplyCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = APPLY_CODE_PREFIX + dateStr;
        return prefix + String.format("%04d", IdUtil.getSnowflakeNextId() % 10000);
    }

    /**
     * 转换为响应VO
     */
    private IotVisitorApplyRespVO convertToRespVO(IotVisitorApplyDO apply) {
        IotVisitorApplyRespVO vo = new IotVisitorApplyRespVO();
        vo.setId(apply.getId());
        vo.setApplyCode(apply.getApplyCode());
        vo.setVisitorId(apply.getVisitorId());
        vo.setVisitorName(apply.getVisitorName());
        vo.setVisitorPhone(apply.getVisitorPhone());
        vo.setVisiteeId(apply.getVisiteeId());
        vo.setVisiteeName(apply.getVisiteeName());
        vo.setVisiteeDeptId(apply.getVisiteeDeptId());
        vo.setVisiteeDeptName(apply.getVisiteeDeptName());
        vo.setVisitReason(apply.getVisitReason());
        vo.setVisitStatus(apply.getVisitStatus());
        vo.setVisitStatusName(getVisitStatusName(apply.getVisitStatus()));
        vo.setPlanVisitTime(apply.getPlanVisitTime());
        vo.setPlanLeaveTime(apply.getPlanLeaveTime());
        vo.setActualVisitTime(apply.getActualVisitTime());
        vo.setActualLeaveTime(apply.getActualLeaveTime());
        vo.setApplyTime(apply.getApplyTime());
        vo.setApproveStatus(apply.getApproveStatus());
        vo.setApproveStatusName(getApproveStatusName(apply.getApproveStatus()));
        vo.setApproveTime(apply.getApproveTime());
        vo.setApproverName(apply.getApproverName());
        vo.setApproveRemark(apply.getApproveRemark());
        vo.setRemark(apply.getRemark());
        vo.setCreateTime(apply.getCreateTime());

        // 获取访客详细信息
        IotVisitorDO visitor = visitorMapper.selectById(apply.getVisitorId());
        if (visitor != null) {
            vo.setVisitorCode(visitor.getVisitorCode());
            vo.setIdCard(visitor.getIdCard());
            vo.setCompany(visitor.getCompany());
            vo.setFaceUrl(visitor.getFaceUrl());
        }

        // 获取授权信息
        IotVisitorAuthDO auth = authMapper.selectByApplyId(apply.getId());
        if (auth != null) {
            vo.setAuthStatus(auth.getAuthStatus());
            vo.setAuthStatusName(getAuthStatusName(auth.getAuthStatus()));
            vo.setCardNo(auth.getCardNo());
            vo.setTimeTemplateId(auth.getTimeTemplateId());
            vo.setAuthType(auth.getAuthType());
            vo.setAuthTypeName(getAuthTypeName(auth.getAuthType()));
            vo.setAuthStartTime(auth.getAuthStartTime());
            vo.setAuthEndTime(auth.getAuthEndTime());
            vo.setMaxAccessCount(auth.getMaxAccessCount());
            vo.setUsedAccessCount(auth.getUsedAccessCount());
            vo.setDailyAccessLimit(auth.getDailyAccessLimit());
            vo.setDailyUsedCount(auth.getDailyUsedCount());

            // 获取授权设备列表
            List<IotVisitorAuthDeviceDO> authDevices = authDeviceMapper.selectListByAuthId(auth.getId());
            if (CollUtil.isNotEmpty(authDevices)) {
                List<IotVisitorApplyRespVO.AuthDeviceVO> deviceVOs = new ArrayList<>();
                for (IotVisitorAuthDeviceDO authDevice : authDevices) {
                    IotVisitorApplyRespVO.AuthDeviceVO deviceVO = new IotVisitorApplyRespVO.AuthDeviceVO();
                    deviceVO.setDeviceId(authDevice.getDeviceId());
                    deviceVO.setDeviceName(authDevice.getDeviceName());
                    deviceVO.setChannelId(authDevice.getChannelId());
                    deviceVO.setChannelName(authDevice.getChannelName());
                    deviceVO.setDispatchStatus(authDevice.getDispatchStatus());
                    deviceVO.setDispatchResult(authDevice.getDispatchResult());
                    deviceVOs.add(deviceVO);
                }
                vo.setAuthDevices(deviceVOs);
            }
        }

        return vo;
    }

    private String getVisitStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待访";
            case 1 -> "在访";
            case 2 -> "离访";
            case 3 -> "已取消";
            default -> "未知";
        };
    }

    private String getApproveStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待审批";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            default -> "未知";
        };
    }

    private String getAuthStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待下发";
            case 1 -> "下发中";
            case 2 -> "已下发";
            case 3 -> "已回收";
            case 4 -> "下发失败";
            default -> "未知";
        };
    }

    private String getAuthTypeName(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 1 -> "按时间段";
            case 2 -> "按次数";
            case 3 -> "时间+次数";
            default -> "未知";
        };
    }

}
