package cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * IoT 轮巡计划 DO
 *
 * @author 长辉信息
 */
@TableName(value = "iot_video_patrol_plan")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotVideoPatrolPlanDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 计划编码
     */
    private String planCode;

    /**
     * 计划描述
     */
    private String description;

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
     * 循环模式
     * 1: 循环执行
     * 2: 执行一次
     */
    private Integer loopMode;

    /**
     * 负责人ID（用户ID）
     */
    private Long executor;

    /**
     * 负责人姓名（冗余字段）
     */
    private String executorName;

    /**
     * 计划开始日期
     */
    private LocalDate startDate;

    /**
     * 计划结束日期（NULL表示长期有效）
     */
    private LocalDate endDate;

    /**
     * 排序
     */
    private Integer sort;

}
