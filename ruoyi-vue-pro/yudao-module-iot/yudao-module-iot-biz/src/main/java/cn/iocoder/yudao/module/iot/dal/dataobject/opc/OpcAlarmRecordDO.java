package cn.iocoder.yudao.module.iot.dal.dataobject.opc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * OPC报警记录 DO
 * <p>
 * 存储在TDengine时序数据库中
 *
 * @author 长辉信息科技有限公司
 */
@TableName("opc_alarm_record")
@Data
public class OpcAlarmRecordDO {

    /**
     * 时间戳（主键，TDengine时序数据库的时间列）
     */
    
    private LocalDateTime ts;

    /**
     * 账号（主机标识）
     */
    
    private Integer account;

    /**
     * 事件代码
     */
    private Integer eventCode;

    /**
     * 防区号
     */
    private Integer area;

    /**
     * 点位号
     */
    private Integer point;

    /**
     * 序列号
     */
    private Integer sequence;

    /**
     * 事件描述
     */
    private String eventDescription;

    /**
     * 事件级别（info/warning/error/critical）
     */
    private String level;

    /**
     * 事件类型（alarm/restore/status/test）
     */
    private String type;

    /**
     * 接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 远程地址
     */
    private String remoteAddress;

    /**
     * 远程端口
     */
    private Integer remotePort;

    /**
     * 原始消息
     */
    private String rawMessage;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 防区名称
     */
    private String areaName;

    /**
     * 点位名称
     */
    private String pointName;

    /**
     * 位置信息
     */
    private String location;

    /**
     * 是否已处理
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean handled;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理人
     */
    private String handleBy;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
