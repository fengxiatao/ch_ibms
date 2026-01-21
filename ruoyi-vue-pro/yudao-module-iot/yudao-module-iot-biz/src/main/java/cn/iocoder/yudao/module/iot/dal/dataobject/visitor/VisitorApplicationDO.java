package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 访客申请 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_visitor_application", autoResultMap = true)
@KeySequence("iot_visitor_application_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorApplicationDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请编号
     */
    private String applicationCode;

    /**
     * 访客姓名
     */
    private String visitorName;

    /**
     * 访客证件号
     */
    private String idNumber;

    /**
     * 访客手机号
     */
    private String phone;

    /**
     * 访客类型
     */
    private Integer visitorType;

    /**
     * 被访人ID
     */
    private Long visitedPersonId;

    /**
     * 被访人姓名
     */
    private String visitedPersonName;

    /**
     * 来访事由
     */
    private String visitPurpose;

    /**
     * 访问开始时间
     */
    private LocalDateTime visitStartTime;

    /**
     * 访问结束时间
     */
    private LocalDateTime visitEndTime;

    /**
     * 申请状态
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.visitor.VisitorApplicationStatusEnum}
     * 0-待审核，1-已通过，2-已拒绝，3-已取消，4-已过期
     */
    private Integer applicationStatus;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    private String approvalRemark;

    /**
     * 备注
     */
    private String remark;

}


























