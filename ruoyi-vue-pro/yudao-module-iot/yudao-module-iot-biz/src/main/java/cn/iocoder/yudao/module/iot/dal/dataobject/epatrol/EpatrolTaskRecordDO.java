package cn.iocoder.yudao.module.iot.dal.dataobject.epatrol;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 电子巡更 - 任务打卡记录 DO
 *
 * @author 长辉信息
 */
@TableName("iot_epatrol_task_record")
@KeySequence("iot_epatrol_task_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpatrolTaskRecordDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 点位ID
     */
    private Long pointId;

    /**
     * 点位编号
     */
    private String pointNo;

    /**
     * 点位名称
     */
    private String pointName;

    /**
     * 巡更人员ID
     */
    private Long personId;

    /**
     * 巡更人员姓名
     */
    private String personName;

    /**
     * 预期顺序
     */
    private Integer expectedSort;

    /**
     * 实际顺序
     */
    private Integer actualSort;

    /**
     * 计划到达时间
     */
    private LocalDateTime plannedTime;

    /**
     * 实际到达时间
     */
    private LocalDateTime actualTime;

    /**
     * 巡更状态：1-准时，2-早到，3-晚到，4-未到，5-顺序错
     */
    private Integer patrolStatus;

    /**
     * 时间差（秒，正数为晚到，负数为早到）
     */
    private Integer timeDiffSeconds;

    /**
     * 备注
     */
    private String remark;

}
