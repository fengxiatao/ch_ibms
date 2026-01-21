package cn.iocoder.yudao.module.iot.dal.dataobject.video;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalTime;

/**
 * 视频定时轮巡计划 DO
 *
 * @author 芋道源码
 */
@TableName("iot_video_patrol_schedule")
@KeySequence("iot_video_patrol_schedule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoPatrolScheduleDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * 计划名称
     */
    private String name;

    /**
     * 轮巡计划ID
     */
    private Long patrolPlanId;

    /**
     * 计划类型
     * 
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.video.VideoPatrolScheduleTypeEnum}
     */
    private Integer scheduleType;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 周几执行（周计划）
     * 
     * 格式：1,2,3,4,5,6,7 逗号分隔
     */
    private String weekDays;

    /**
     * 状态
     * 
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
