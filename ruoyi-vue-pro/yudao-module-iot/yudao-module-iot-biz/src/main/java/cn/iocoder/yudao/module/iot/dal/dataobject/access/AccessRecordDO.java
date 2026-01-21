package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门禁通行记录 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_access_record", autoResultMap = true)
@KeySequence("iot_access_record_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRecordDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 人员部门
     */
    private String personDept;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 照片URL
     */
    private String photoUrl;

    /**
     * 开门类型
     * 
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.access.AccessOpenTypeEnum}
     * 1-刷卡，2-人脸，3-密码，4-远程
     */
    private Integer openType;

    /**
     * 开门结果
     * 1-成功，2-失败
     */
    private Integer openResult;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 通行时间
     */
    private LocalDateTime accessTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}


























