package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 访客申请/预约 DO
 *
 * @author 芋道源码
 */
@TableName("iot_visitor_apply")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotVisitorApplyDO extends TenantBaseDO {

    /**
     * 申请ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请编号
     */
    private String applyCode;

    /**
     * 访客ID
     */
    private Long visitorId;

    /**
     * 访客姓名（冗余）
     */
    private String visitorName;

    /**
     * 访客电话（冗余）
     */
    private String visitorPhone;

    /**
     * 被访人ID（关联iot_access_person）
     */
    private Long visiteeId;

    /**
     * 被访人姓名（冗余）
     */
    private String visiteeName;

    /**
     * 被访人部门ID
     */
    private Long visiteeDeptId;

    /**
     * 被访人部门名称（冗余）
     */
    private String visiteeDeptName;

    /**
     * 来访事由
     */
    private String visitReason;

    /**
     * 来访状态：0-待访，1-在访，2-离访，3-已取消
     */
    private Integer visitStatus;

    /**
     * 计划来访时间
     */
    private LocalDateTime planVisitTime;

    /**
     * 计划离开时间
     */
    private LocalDateTime planLeaveTime;

    /**
     * 实际到访时间
     */
    private LocalDateTime actualVisitTime;

    /**
     * 实际离开时间
     */
    private LocalDateTime actualLeaveTime;

    /**
     * 申请时间
     */
    private LocalDateTime applyTime;

    /**
     * 审批状态：0-待审批，1-已通过，2-已拒绝
     */
    private Integer approveStatus;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批备注
     */
    private String approveRemark;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 备注
     */
    private String remark;

    // ========== 来访状态常量 ==========

    public static final int VISIT_STATUS_WAITING = 0;    // 待访
    public static final int VISIT_STATUS_VISITING = 1;   // 在访
    public static final int VISIT_STATUS_LEFT = 2;       // 离访
    public static final int VISIT_STATUS_CANCELLED = 3;  // 已取消

    // ========== 审批状态常量 ==========

    public static final int APPROVE_STATUS_PENDING = 0;   // 待审批
    public static final int APPROVE_STATUS_APPROVED = 1;  // 已通过
    public static final int APPROVE_STATUS_REJECTED = 2;  // 已拒绝

}
