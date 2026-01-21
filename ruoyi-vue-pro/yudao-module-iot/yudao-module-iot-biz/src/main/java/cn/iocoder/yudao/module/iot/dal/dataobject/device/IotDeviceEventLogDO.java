package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 设备事件日志 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName(value = "iot_device_event_log", autoResultMap = true)
@KeySequence("iot_device_event_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceEventLogDO extends BaseDO {

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;

    /**
     * 设备编号
     */
    private Long deviceId;

    /**
     * 产品编号
     */
    private Long productId;

    /**
     * 产品标识
     */
    private String productKey;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 事件标识符（对应物模型）
     */
    private String eventIdentifier;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 事件类型（info, alert, error）
     */
    private String eventType;

    /**
     * 事件数据（JSON格式）
     */
    private String eventData;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * ONVIF原始Topic（可选）
     */
    private String onvifTopic;

    /**
     * 是否已处理
     */
    private Boolean processed;

    /**
     * 触发的场景规则ID列表（JSON数组，例如: [1,2,3]）
     */
    private String triggeredSceneRuleIds;

    /**
     * 生成的告警记录ID列表（JSON数组，例如: [1,2,3]）
     */
    private String generatedAlertRecordIds;
}












