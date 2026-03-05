package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 照明定时任务 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_lighting_schedule")
@KeySequence("ibms_lighting_schedule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsLightingScheduleDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String scheduleName;

    /**
     * 执行时间(HH:mm)
     */
    private String executeTime;

    /**
     * 执行星期(逗号分隔，0-6表示周日到周六)
     */
    private String weekdays;

    /**
     * 执行场景ID
     */
    private Long sceneId;

    /**
     * 执行场景名称
     */
    private String sceneName;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 备注
     */
    private String remark;

}
