package cn.iocoder.yudao.module.iot.dal.dataobject.alarm;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 入侵报警记录 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName("iot_alarm_record")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAlarmRecordDO extends TenantBaseDO {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 触发的规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 报警主机ID
     */
    private Long hostId;

    /**
     * 防区ID
     */
    private Long zoneId;

    /**
     * 报警类型：IVS-智能报警, NORMAL-普通报警
     */
    private String alarmType;

    /**
     * 事件码（1400-1499）
     */
    private Integer eventCode;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 报警级别：1-低, 2-中, 3-高, 4-紧急
     */
    private Integer alarmLevel;

    /**
     * 防区号
     */
    private Integer area;

    /**
     * 防区名称
     */
    private String areaName;

    /**
     * 点位号
     */
    private Integer point;

    /**
     * 点位名称
     */
    private String pointName;

    /**
     * 序列号
     */
    private Integer sequence;

    /**
     * 报警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 报警快照URL
     */
    private String snapshotUrl;

    /**
     * 联动录像URL
     */
    private String videoUrl;

    /**
     * 录像开始时间
     */
    private LocalDateTime videoStartTime;

    /**
     * 录像结束时间
     */
    private LocalDateTime videoEndTime;

    /**
     * 原始报警数据（JSON格式）
     */
    private String rawData;

    /**
     * 处理状态：0-未处理, 1-已处理, 2-已忽略
     */
    private Integer processStatus;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 处理人
     */
    private String processUser;

    /**
     * 处理备注
     */
    private String processRemark;
}
