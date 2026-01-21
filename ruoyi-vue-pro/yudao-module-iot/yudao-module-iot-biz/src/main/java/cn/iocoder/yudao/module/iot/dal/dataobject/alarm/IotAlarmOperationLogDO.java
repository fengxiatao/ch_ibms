package cn.iocoder.yudao.module.iot.dal.dataobject.alarm;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 报警主机操作记录 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName("iot_alarm_operation_log")
@KeySequence("iot_alarm_operation_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAlarmOperationLogDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 报警主机ID
     */
    private Long hostId;

    /**
     * 分区ID
     */
    private Long partitionId;

    /**
     * 防区ID
     */
    private Long zoneId;

    /**
     * 操作类型
     * ARM_ALL-外出布防
     * ARM_EMERGENCY-居家布防
     * DISARM-撤防
     * CLEAR_ALARM-消警
     * BYPASS-旁路
     * UNBYPASS-撤销旁路
     * QUERY-查询
     * REFRESH-刷新
     * RENAME-重命名
     */
    private String operationType;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作结果：SUCCESS-成功, FAILED-失败
     */
    private String result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 请求ID
     */
    private String requestId;

}
