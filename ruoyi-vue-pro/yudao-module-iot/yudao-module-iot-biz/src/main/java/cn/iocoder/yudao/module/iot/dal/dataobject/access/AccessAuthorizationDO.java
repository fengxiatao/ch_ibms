package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门禁授权 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_access_authorization", autoResultMap = true)
@KeySequence("iot_access_authorization_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessAuthorizationDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 授权类型
     * 
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.access.AccessAuthTypeEnum}
     * 1-物管授权，2-租户授权
     */
    private Integer authType;

    /**
     * 组织ID
     */
    private Long orgId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 门组ID
     */
    private Long doorGroupId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 星期限制（1234567表示周一到周日）
     */
    private String weekDays;

    /**
     * 时间段限制（JSON格式）
     * 
     * 示例：[{"start":"08:00","end":"18:00"},{"start":"19:00","end":"22:00"}]
     */
    private String timeSlots;

    /**
     * 授权状态
     * 1-启用，2-停用
     */
    private Integer authStatus;

    /**
     * 备注
     */
    private String remark;

}


























