package cn.iocoder.yudao.module.iot.dal.dataobject.epatrol;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 电子巡更 - 巡更任务 DO
 *
 * @author 长辉信息
 */
@TableName(value = "iot_epatrol_task", autoResultMap = true)
@KeySequence("iot_epatrol_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpatrolTaskDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 任务编号
     */
    private String taskCode;

    /**
     * 关联计划ID
     */
    private Long planId;

    /**
     * 关联时段ID
     */
    private Long periodId;

    /**
     * 巡更路线ID
     */
    private Long routeId;

    /**
     * 任务日期
     */
    private LocalDate taskDate;

    /**
     * 计划开始时间
     */
    private LocalDateTime plannedStartTime;

    /**
     * 计划结束时间
     */
    private LocalDateTime plannedEndTime;

    /**
     * 巡更人员ID数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> personIds;

    /**
     * 任务状态：0-未巡，1-已巡
     */
    private Integer status;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 提交人ID
     */
    private Long submitterId;

    /**
     * 备注
     */
    private String remark;

}
