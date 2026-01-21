package cn.iocoder.yudao.module.iot.dal.dataobject.epatrol;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

/**
 * 电子巡更 - 计划时段 DO
 *
 * @author 长辉信息
 */
@TableName(value = "iot_epatrol_plan_period", autoResultMap = true)
@KeySequence("iot_epatrol_plan_period_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpatrolPlanPeriodDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 计划ID
     */
    private Long planId;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 巡更时长（分钟，应大于路线总用时）
     */
    private Integer durationMinutes;

    /**
     * 巡更人员ID数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> personIds;

}
