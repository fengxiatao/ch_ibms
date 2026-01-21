package cn.iocoder.yudao.module.iot.dal.dataobject.changhui;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 长辉设备报警记录 DO
 * 
 * <p>存储设备上报的各类报警信息，包括过力矩、过电流、过电压、低电压、水位超限、闸位超限等
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@TableName("changhui_alarm")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChanghuiAlarmDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 测站编码
     */
    private String stationCode;

    /**
     * 报警类型
     * 
     * <p>OVER_TORQUE-过力矩, OVER_CURRENT-过电流, OVER_VOLTAGE-过电压,
     * LOW_VOLTAGE-低电压, WATER_LEVEL-水位超限, GATE_POSITION-闸位超限
     * @see cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiAlarmTypeEnum
     */
    private String alarmType;

    /**
     * 报警值
     */
    private String alarmValue;

    /**
     * 报警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 状态：0-未确认,1-已确认
     */
    private Integer status;

    /**
     * 确认时间
     */
    private LocalDateTime ackTime;

    /**
     * 确认人
     */
    private String ackUser;

}
