package cn.iocoder.yudao.module.iot.dal.dataobject.epatrol;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 电子巡更 - 巡更计划 DO
 *
 * @author 长辉信息
 */
@TableName(value = "iot_epatrol_plan", autoResultMap = true)
@KeySequence("iot_epatrol_plan_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpatrolPlanDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 计划编号
     */
    private String planCode;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 巡更路线ID
     */
    private Long routeId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 星期选择（JSON数组，如[1,2,3,4,5]，1=周一）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> weekdays;

    /**
     * 计划状态：0-未开始，1-执行中，2-已过期
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
