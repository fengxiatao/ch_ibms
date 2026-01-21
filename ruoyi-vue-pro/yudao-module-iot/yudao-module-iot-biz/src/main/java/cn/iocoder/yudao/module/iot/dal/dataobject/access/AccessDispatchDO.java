package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门禁下发记录 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_access_dispatch", autoResultMap = true)
@KeySequence("iot_access_dispatch_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessDispatchDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 下发类型
     * 
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.access.AccessDispatchTypeEnum}
     * 1-人员下发，2-卡片下发，3-人脸下发
     */
    private Integer dispatchType;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 人员编号
     */
    private String personCode;

    /**
     * 人员类型
     */
    private String personType;

    /**
     * 组织部门
     */
    private String orgDept;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 人脸照片
     */
    private String facePhoto;

    /**
     * 操作类型
     * 1-新增，2-修改，3-删除
     */
    private Integer operationType;

    /**
     * 下发状态
     * 
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.access.AccessDispatchStatusEnum}
     * 0-待下发，1-下发中，2-成功，3-失败
     */
    private Integer dispatchStatus;

    /**
     * 错误类型
     */
    private String errorType;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 下发时间
     */
    private LocalDateTime dispatchTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}


























