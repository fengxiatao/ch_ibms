package cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * IoT 轮巡任务 DO
 *
 * @author 长辉信息
 */
@TableName(value = "iot_video_patrol_task", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotVideoPatrolTaskDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属计划ID
     */
    private Long planId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务编码
     */
    private String taskCode;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务执行顺序（在计划中的顺序）
     */
    private Integer taskOrder;

    /**
     * 任务总时长（秒）
     */
    private Integer duration;

    /**
     * 排班类型
     * 1: 每天
     * 2: 工作日
     * 3: 周末
     * 4: 自定义
     */
    private Integer scheduleType;

    /**
     * 排班配置（JSON格式，自定义时存储具体日期）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> scheduleConfig;

    /**
     * 时间段配置（JSON数组）
     * 示例: [{"startTime":"08:00","endTime":"18:00"}]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> timeSlots;

    /**
     * 轮巡模式
     * 1: 顺序轮巡
     * 2: 随机轮巡
     */
    private Integer loopMode;

    /**
     * 场景切换间隔(分钟)
     */
    private Integer intervalMinutes;

    /**
     * 自动抓拍
     * 0: 否
     * 1: 是
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean autoSnapshot;

    /**
     * 自动录像
     * 0: 否
     * 1: 是
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean autoRecording;

    /**
     * 录像时长(秒)
     */
    private Integer recordingDuration;

    /**
     * 启用AI分析
     * 0: 否
     * 1: 是
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean aiAnalysis;

    /**
     * 异常报警
     * 0: 否
     * 1: 是
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean alertOnAbnormal;

    /**
     * 报警接收人ID列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> alertUserIds;

    /**
     * 状态
     * 0: 停用
     * 1: 启用
     */
    private Integer status;

    /**
     * 运行状态
     * stopped: 未启动
     * running: 运行中
     * paused: 已暂停
     */
    private String runningStatus;

    /**
     * 当前正在播放的场景ID
     */
    private Long currentSceneId;

    /**
     * 最后运行时间
     */
    private LocalDateTime lastRunTime;

    /**
     * 排序
     */
    private Integer sort;

}
