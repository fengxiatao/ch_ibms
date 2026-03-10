package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 访客预约单 DO（新原型：新访客管理）
 */
@TableName("iot_visitor_appointment")
@KeySequence("iot_visitor_appointment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorAppointmentDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 访客姓名 */
    private String name;
    /** 联系电话 */
    private String phone;
    /** 访客类型：business/vip/contractor/interview */
    private String type;
    /** 所属单位 */
    private String company;

    /** 被访人 */
    private String host;
    /** 被访人部门（可为空） */
    private String hostDept;

    /** 预约来访时间 */
    private LocalDateTime visitTime;
    /** 来访事由 */
    private String reason;
    /** 访问区域（JSON 字符串，存放数组） */
    private String areas;

    /** 身份证号（可为空） */
    private String idCard;
    /** 车牌号（可为空） */
    private String carNo;
    /** 备注 */
    private String remark;

    /**
     * 审批状态：pending/approved/rejected/cancelled
     */
    private String status;
    /** 审批意见 */
    private String approvalComment;
    /** 审批时间 */
    private LocalDateTime approvalTime;
    /** 审批人用户 ID（可为空） */
    private Long approverId;

    /** 签到时间 */
    private LocalDateTime signInTime;
    /** 签离时间 */
    private LocalDateTime signOutTime;
    /** 当前位置（可为空） */
    private String currentLocation;

    /** 评价分数（0-5，可为空） */
    private Double rating;
}

