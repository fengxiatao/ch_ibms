package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门禁操作日志 DO
 * 
 * 支持两类操作日志：
 * 1. 门控操作日志：开门、关门、常开、常闭等
 * 2. 授权操作日志：授权下发、授权撤销等
 *
 * @author 芋道源码
 */
@TableName("iot_access_operation_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessOperationLogDO extends TenantBaseDO {

    // ========== 操作类型常量 - 门控操作 ==========
    public static final String OP_TYPE_OPEN_DOOR = "open_door";
    public static final String OP_TYPE_CLOSE_DOOR = "close_door";
    public static final String OP_TYPE_ALWAYS_OPEN = "always_open";
    public static final String OP_TYPE_ALWAYS_CLOSED = "always_closed";
    public static final String OP_TYPE_CANCEL_ALWAYS = "cancel_always";
    
    // ========== 操作类型常量 - 授权操作 ==========
    /** 授权下发 - 权限组 */
    public static final String OP_TYPE_AUTH_DISPATCH_GROUP = "auth_dispatch_group";
    /** 授权下发 - 单人 */
    public static final String OP_TYPE_AUTH_DISPATCH_PERSON = "auth_dispatch_person";
    /** 授权撤销 */
    public static final String OP_TYPE_AUTH_REVOKE = "auth_revoke";
    /** 授权重试 */
    public static final String OP_TYPE_AUTH_RETRY = "auth_retry";
    /** 添加用户 */
    public static final String OP_TYPE_ADD_USER = "add_user";
    /** 删除用户 */
    public static final String OP_TYPE_DELETE_USER = "delete_user";
    /** 添加卡片 */
    public static final String OP_TYPE_ADD_CARD = "add_card";
    /** 删除卡片 */
    public static final String OP_TYPE_DELETE_CARD = "delete_card";
    /** 录入人脸 */
    public static final String OP_TYPE_ADD_FACE = "add_face";
    /** 删除人脸 */
    public static final String OP_TYPE_DELETE_FACE = "delete_face";
    /** 录入指纹 */
    public static final String OP_TYPE_ADD_FINGERPRINT = "add_fingerprint";
    /** 删除指纹 */
    public static final String OP_TYPE_DELETE_FINGERPRINT = "delete_fingerprint";

    // ========== 结果常量 ==========
    public static final int RESULT_FAILURE = 0;
    public static final int RESULT_SUCCESS = 1;

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 通道ID
     */
    private Long channelId;
    
    /**
     * 通道名称（门通道名称）
     */
    private String channelName;

    /**
     * 操作类型：OPEN_DOOR-开门，CLOSE_DOOR-关门，ALWAYS_OPEN-常开，ALWAYS_CLOSED-常闭，CANCEL_ALWAYS-取消常开常闭
     * 授权操作：auth_dispatch_group-权限组下发，auth_dispatch_person-单人下发，auth_revoke-撤销
     */
    private String operationType;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 结果：0-失败，1-成功
     */
    private Integer result;

    /**
     * 结果描述
     */
    private String resultDesc;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;
    
    // ========== 授权操作扩展字段 ==========
    
    /**
     * 目标人员ID（授权操作时使用）
     */
    private Long targetPersonId;
    
    /**
     * 目标人员编号（授权操作时使用）
     */
    private String targetPersonCode;
    
    /**
     * 目标人员姓名（授权操作时使用）
     */
    private String targetPersonName;
    
    /**
     * 权限组ID（授权操作时使用）
     */
    private Long permissionGroupId;
    
    /**
     * 权限组名称（授权操作时使用）
     */
    private String permissionGroupName;
    
    /**
     * 授权任务ID（授权操作时使用）
     */
    private Long authTaskId;
    
    /**
     * 凭证类型列表（逗号分隔，如：FACE,CARD,FINGERPRINT）
     */
    private String credentialTypes;
    
    /**
     * 成功的凭证类型数量
     */
    private Integer successCredentialCount;
    
    /**
     * 失败的凭证类型数量
     */
    private Integer failedCredentialCount;
    
    /**
     * SDK错误码
     */
    private Integer sdkErrorCode;

}
