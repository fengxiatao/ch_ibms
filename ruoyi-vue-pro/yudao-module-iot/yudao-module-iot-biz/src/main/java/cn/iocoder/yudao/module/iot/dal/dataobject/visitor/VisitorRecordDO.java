package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 访客记录 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_visitor_record", autoResultMap = true)
@KeySequence("iot_visitor_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorRecordDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 访客姓名
     */
    private String visitorName;

    /**
     * 访客证件类型
     * 1-身份证，2-护照，3-其他
     */
    private Integer idType;

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
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.visitor.VisitorTypeEnum}
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
     * 被访部门
     */
    private String visitedDept;

    /**
     * 来访事由
     */
    private String visitPurpose;

    /**
     * 预约开始时间
     */
    private LocalDateTime appointmentStartTime;

    /**
     * 预约结束时间
     */
    private LocalDateTime appointmentEndTime;

    /**
     * 实际到达时间
     */
    private LocalDateTime arrivalTime;

    /**
     * 实际离开时间
     */
    private LocalDateTime departureTime;

    /**
     * 访客照片URL
     */
    private String photoUrl;

    /**
     * 访客状态
     * 1-预约中，2-已到达，3-已离开，4-已过期
     */
    private Integer visitorStatus;

    /**
     * 备注
     */
    private String remark;

}


























