package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门禁事件日志 DO
 *
 * @author 芋道源码
 */
@TableName("iot_access_event_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessEventLogDO extends TenantBaseDO {

    /**
     * 事件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;
    private String deviceName;
    /**
     * 通道ID
     */
    private Long channelId;
    private String channelName;

    /**
     * 事件类型：CARD_SWIPE-刷卡，FACE_RECOGNIZE-人脸识别，FINGERPRINT-指纹，PASSWORD-密码，DOOR_OPEN-开门，DOOR_CLOSE-关门，ALARM-报警
     */
    private String eventType;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 人员证件号（身份证号等）
     * 需求补充：支持在门禁记录列表中展示人员证件号
     */
    private String idCard;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 出入方向：1-进门，2-出门
     * 需求补充：支持记录人员通行的进出方向
     */
    private Integer direction;

    /**
     * 验证方式
     */
    private String verifyMode;

    /**
     * 验证结果：0-失败，1-成功
     */
    private Integer verifyResult;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 抓拍图片URL
     */
    private String snapshotUrl;

    /**
     * 抓拍图片URL（别名）
     */
    private String captureUrl;

    /**
     * 人员编号
     */
    private String personCode;

    /**
     * 验证结果描述
     */
    private String verifyResultDesc;

    /**
     * 体温（℃）
     */
    private BigDecimal temperature;

    /**
     * 口罩状态：0-未佩戴，1-已佩戴
     */
    private Integer maskStatus;
    private String eventDesc;
    private String credentialType;
    private String credentialData;
    private Boolean success;
}
