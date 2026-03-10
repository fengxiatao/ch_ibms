package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 新访客管理 - 异常事件 DO
 */
@TableName("iot_visitor_abnormal_event")
@KeySequence("iot_visitor_abnormal_event_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorAbnormalEventDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联预约ID（可为空；非法闯入等可能无预约） */
    private Long appointmentId;

    /** 访客姓名 */
    private String visitorName;
    /** 访客电话 */
    private String visitorPhone;

    /** 异常类型：overtime/unauthorized/noshow */
    private String abnormalType;
    /** 风险等级：high/medium/low */
    private String riskLevel;
    /** 详细描述 */
    private String details;

    /** 发生时间 */
    private LocalDateTime eventTime;

    /** 当前状态/位置（前端展示用） */
    private String currentStatus;

    /** 是否已处理 */
    private Boolean handled;
    /** 处理人 */
    private Long handlerId;
    /** 处理时间 */
    private LocalDateTime handleTime;
    /** 处理结果 */
    private String handleResult;
}

