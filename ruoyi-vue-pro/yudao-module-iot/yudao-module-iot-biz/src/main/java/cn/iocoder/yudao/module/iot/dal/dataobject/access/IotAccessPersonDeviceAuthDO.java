package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 人员设备授权状态 DO
 * 
 * 记录人员在各设备的授权状态，用于：
 * 1. 查看人员在各设备的授权状态
 * 2. 增量下发时检测凭证变更
 * 3. 状态同步和一致性检查
 *
 * @author 芋道源码
 */
@TableName("iot_access_person_device_auth")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessPersonDeviceAuthDO extends TenantBaseDO {

    /**
     * 授权状态：未授权
     */
    public static final int AUTH_STATUS_UNAUTHORIZED = 0;
    /**
     * 授权状态：已授权
     */
    public static final int AUTH_STATUS_AUTHORIZED = 1;
    /**
     * 授权状态：授权中
     */
    public static final int AUTH_STATUS_AUTHORIZING = 2;
    /**
     * 授权状态：授权失败
     */
    public static final int AUTH_STATUS_FAILED = 3;
    /**
     * 授权状态：待撤销（设备离线时标记，上线后自动执行）
     * Requirements: 8.5
     */
    public static final int AUTH_STATUS_PENDING_REVOKE = 4;
    /**
     * 授权状态：撤销中
     */
    public static final int AUTH_STATUS_REVOKING = 5;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 授权状态：0-未授权，1-已授权，2-授权中，3-授权失败
     */
    private Integer authStatus;

    /**
     * 最后下发时间
     */
    private LocalDateTime lastDispatchTime;

    /**
     * 最后下发结果描述
     */
    private String lastDispatchResult;

    /**
     * 凭证哈希（用于增量检测）
     * 计算方式：MD5(人脸URL + 卡号 + 密码 + 指纹数据)
     */
    private String credentialHash;

    /**
     * 乐观锁版本号
     * 用于并发更新时的冲突检测
     */
    @Version
    private Integer version;

}
