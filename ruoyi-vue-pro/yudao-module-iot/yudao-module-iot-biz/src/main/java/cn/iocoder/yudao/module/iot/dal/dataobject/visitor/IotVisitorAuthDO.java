package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 访客授权记录 DO
 *
 * @author 芋道源码
 */
@TableName("iot_visitor_auth")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotVisitorAuthDO extends TenantBaseDO {

    /**
     * 授权ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请ID
     */
    private Long applyId;

    /**
     * 访客ID
     */
    private Long visitorId;

    /**
     * 访客编号
     */
    private String visitorCode;

    /**
     * 访客姓名（冗余）
     */
    private String visitorName;

    /**
     * 门禁卡号
     */
    private String cardNo;

    /**
     * 人脸照片URL
     */
    private String faceUrl;

    /**
     * 时间模板ID（可选，关联iot_access_time_template）
     */
    private Long timeTemplateId;

    /**
     * 授权类型：1-按时间段，2-按次数，3-时间+次数
     */
    private Integer authType;

    /**
     * 最大通行次数（授权类型为2或3时有效）
     */
    private Integer maxAccessCount;

    /**
     * 已使用通行次数
     */
    private Integer usedAccessCount;

    /**
     * 每日通行次数限制
     */
    private Integer dailyAccessLimit;

    /**
     * 当日已使用次数
     */
    private Integer dailyUsedCount;

    /**
     * 上次通行日期（用于重置每日计数）
     */
    private java.time.LocalDate lastAccessDate;

    /**
     * 授权开始时间
     */
    private LocalDateTime authStartTime;

    /**
     * 授权结束时间
     */
    private LocalDateTime authEndTime;

    /**
     * 授权状态：0-待下发，1-下发中，2-已下发，3-已回收，4-下发失败
     */
    private Integer authStatus;

    /**
     * 下发任务ID（关联iot_access_auth_task）
     */
    private Long dispatchTaskId;

    /**
     * 回收任务ID
     */
    private Long revokeTaskId;

    /**
     * 下发时间
     */
    private LocalDateTime dispatchTime;

    /**
     * 回收时间
     */
    private LocalDateTime revokeTime;

    /**
     * 下发结果描述
     */
    private String dispatchResult;

    /**
     * 回收结果描述
     */
    private String revokeResult;

    /**
     * 备注
     */
    private String remark;

    // ========== 授权状态常量 ==========

    public static final int AUTH_STATUS_PENDING = 0;      // 待下发
    public static final int AUTH_STATUS_DISPATCHING = 1;  // 下发中
    public static final int AUTH_STATUS_DISPATCHED = 2;   // 已下发
    public static final int AUTH_STATUS_REVOKED = 3;      // 已回收
    public static final int AUTH_STATUS_FAILED = 4;       // 下发失败

    // ========== 授权类型常量 ==========

    public static final int AUTH_TYPE_TIME_RANGE = 1;     // 按时间段
    public static final int AUTH_TYPE_COUNT = 2;          // 按次数
    public static final int AUTH_TYPE_TIME_AND_COUNT = 3; // 时间+次数

}
