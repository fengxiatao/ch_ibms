package cn.iocoder.yudao.module.iot.dal.dataobject.channel;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IoT 设备通道历史记录 DO
 *
 * @author IBMS Team
 */
@TableName("iot_device_channel_history")
@Data
public class IotDeviceChannelHistoryDO {

    /**
     * 历史记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 所属设备ID
     */
    private Long deviceId;

    /**
     * 通道号
     */
    private Integer channelNo;

    /**
     * 操作类型（INSERT、UPDATE、DELETE）
     */
    private String operation;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 通道数据快照（JSON格式）
     */
    private String channelData;

    /**
     * 变更字段（JSON格式）
     */
    private String changedFields;

    /**
     * 旧值（JSON格式）
     */
    private String oldValues;

    /**
     * 新值（JSON格式）
     */
    private String newValues;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 操作IP
     */
    private String operateIp;

    /**
     * 同步来源
     */
    private String syncSource;

    /**
     * 同步批次ID
     */
    private String syncBatchId;

    /**
     * 备注
     */
    private String remark;
}
