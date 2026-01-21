package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 访客授权设备关联 DO
 *
 * @author 芋道源码
 */
@TableName("iot_visitor_auth_device")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotVisitorAuthDeviceDO extends TenantBaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 授权记录ID
     */
    private Long authId;

    /**
     * 申请ID
     */
    private Long applyId;

    /**
     * 访客ID
     */
    private Long visitorId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称（冗余）
     */
    private String deviceName;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 通道名称（冗余）
     */
    private String channelName;

    /**
     * 下发状态：0-待下发，1-下发中，2-成功，3-失败
     */
    private Integer dispatchStatus;

    /**
     * 下发时间
     */
    private LocalDateTime dispatchTime;

    /**
     * 下发结果
     */
    private String dispatchResult;

    /**
     * 回收状态：0-待回收，1-回收中，2-成功，3-失败
     */
    private Integer revokeStatus;

    /**
     * 回收时间
     */
    private LocalDateTime revokeTime;

    /**
     * 回收结果
     */
    private String revokeResult;

    // ========== 状态常量 ==========

    public static final int STATUS_PENDING = 0;     // 待处理
    public static final int STATUS_PROCESSING = 1;  // 处理中
    public static final int STATUS_SUCCESS = 2;     // 成功
    public static final int STATUS_FAILED = 3;      // 失败

}
