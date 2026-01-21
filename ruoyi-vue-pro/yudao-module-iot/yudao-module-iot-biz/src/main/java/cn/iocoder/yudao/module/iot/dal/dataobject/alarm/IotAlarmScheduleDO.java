package cn.iocoder.yudao.module.iot.dal.dataobject.alarm;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalTime;

/**
 * 定时布防任务 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName("iot_alarm_schedule")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAlarmScheduleDO extends TenantBaseDO {

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报警主机ID
     */
    private Long hostId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 布防类型：ARM_ALL-全部布防, ARM_EMERGENCY-紧急布防, DISARM-撤防
     */
    private String armType;

    /**
     * 星期几执行，逗号分隔：1-7表示周一到周日
     */
    private String weekdays;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间（撤防时使用）
     */
    private LocalTime endTime;

    /**
     * 状态：0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
