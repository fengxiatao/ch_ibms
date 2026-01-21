package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 访客设备日志 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_visitor_device_log", autoResultMap = true)
@KeySequence("iot_visitor_device_log_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorDeviceLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 访客记录ID
     */
    private Long visitorRecordId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 操作类型
     * 1-人脸识别，2-身份证读取，3-访客登记，4-出入记录
     */
    private Integer operationType;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作结果
     * 1-成功，2-失败
     */
    private Integer operationResult;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 照片URL
     */
    private String photoUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}


























