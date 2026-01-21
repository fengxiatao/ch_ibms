package cn.iocoder.yudao.module.iot.core.gateway.dto;

import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessUserInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessCardInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessFaceInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessFingerprintInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 门禁设备控制命令
 * 
 * <p>用于 Biz → Gateway 的门禁设备控制请求</p>
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessControlDeviceCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求ID，用于关联响应
     */
    private String requestId;

    /**
     * 租户ID，用于多租户支持
     * Requirements: 8.4
     */
    private Long tenantId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 通道ID（门控制时使用）
     */
    private Long channelId;

    /**
     * 通道号（门控制时使用）
     */
    private Integer channelNo;

    /**
     * 命令类型
     * @see CommandType
     */
    private String commandType;

    /**
     * 设备IP地址
     */
    private String ipAddress;

    /**
     * 设备端口
     */
    private Integer port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 扩展参数
     */
    private Map<String, Object> params;

    // ========== 卡管理相关字段 ==========

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员编码（用于SDK用户ID）
     */
    private String personCode;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 有效开始时间
     */
    private String validStart;

    /**
     * 有效结束时间
     */
    private String validEnd;

    /**
     * 门权限（逗号分隔的通道索引）
     */
    private String doorPermissions;

    // ========== 凭证下发相关字段 ==========

    /**
     * 用户信息（用于DISPATCH_USER命令）
     */
    private NetAccessUserInfo userInfo;

    /**
     * 卡信息（用于DISPATCH_CARD命令）
     */
    private NetAccessCardInfo cardInfo;

    /**
     * 卡信息列表（用于批量DISPATCH_CARD命令）
     */
    private List<NetAccessCardInfo> cardInfoList;

    /**
     * 人脸信息（用于DISPATCH_FACE命令）
     */
    private NetAccessFaceInfo faceInfo;

    /**
     * 人脸信息列表（用于批量DISPATCH_FACE命令）
     */
    private List<NetAccessFaceInfo> faceInfoList;

    /**
     * 指纹信息（用于DISPATCH_FINGERPRINT命令）
     */
    private NetAccessFingerprintInfo fingerprintInfo;

    /**
     * 指纹信息列表（用于批量DISPATCH_FINGERPRINT命令）
     */
    private List<NetAccessFingerprintInfo> fingerprintInfoList;

    /**
     * 用户ID（用于撤销命令）
     */
    private String userId;

    // ========== 任务追踪字段（用于异步响应关联） ==========

    /**
     * 任务ID（用于关联授权任务）
     */
    private Long taskId;

    /**
     * 任务明细ID（用于关联授权任务明细）
     */
    private Long taskDetailId;

    /**
     * 命令类型枚举
     */
    public interface CommandType {
        /** 激活设备（登录） */
        String ACTIVATE = "ACTIVATE";
        /** 停用设备（登出） */
        String DEACTIVATE = "DEACTIVATE";
        /** 查询通道(使用标准SDK API) */
        String QUERY_CHANNELS = "QUERY_CHANNELS";
        /** 发现通道(已废弃,请使用QUERY_CHANNELS) */
        @Deprecated
        String DISCOVER_CHANNELS = "DISCOVER_CHANNELS";
        /** 开门 */
        String OPEN_DOOR = "OPEN_DOOR";
        /** 关门 */
        String CLOSE_DOOR = "CLOSE_DOOR";
        /** 常开 */
        String ALWAYS_OPEN = "ALWAYS_OPEN";
        /** 常闭 */
        String ALWAYS_CLOSED = "ALWAYS_CLOSED";
        /** 取消常开/常闭 */
        String CANCEL_ALWAYS = "CANCEL_ALWAYS";
        /** 查询状态 */
        String QUERY_STATUS = "QUERY_STATUS";
        
        // ========== 卡管理命令 ==========
        /** 添加卡 */
        String ADD_CARD = "ADD_CARD";
        /** 更新卡 */
        String UPDATE_CARD = "UPDATE_CARD";
        /** 删除卡 */
        String DELETE_CARD = "DELETE_CARD";
        /** 查询卡列表 */
        String LIST_CARDS = "LIST_CARDS";
        /** 清空所有卡 */
        String CLEAR_ALL_CARDS = "CLEAR_ALL_CARDS";
        
        // ========== 凭证下发命令（用于授权下发） ==========
        /** 下发用户信息 */
        String DISPATCH_USER = "DISPATCH_USER";
        /** 下发卡片 */
        String DISPATCH_CARD = "DISPATCH_CARD";
        /** 下发人脸 */
        String DISPATCH_FACE = "DISPATCH_FACE";
        /** 下发指纹 */
        String DISPATCH_FINGERPRINT = "DISPATCH_FINGERPRINT";
        
        // ========== 凭证撤销命令 ==========
        /** 撤销用户信息 */
        String REVOKE_USER = "REVOKE_USER";
        /** 撤销卡片 */
        String REVOKE_CARD = "REVOKE_CARD";
        /** 撤销人脸 */
        String REVOKE_FACE = "REVOKE_FACE";
        /** 撤销指纹 */
        String REVOKE_FINGERPRINT = "REVOKE_FINGERPRINT";
        
        // ========== 设备能力查询命令 ==========
        /** 查询设备能力 */
        String QUERY_DEVICE_CAPABILITY = "QUERY_DEVICE_CAPABILITY";
        /** 检查设备在线状态 */
        String CHECK_DEVICE_ONLINE = "CHECK_DEVICE_ONLINE";
        /** 获取登录句柄 */
        String GET_LOGIN_HANDLE = "GET_LOGIN_HANDLE";
        
        // ========== 设备激活管理命令 ==========
        /** 激活设备（登录设备，用于授权下发前自动激活） */
        String ACTIVATE_DEVICE = "ACTIVATE_DEVICE";
        /** 查询所有设备激活状态 */
        String QUERY_ALL_DEVICE_STATUS = "QUERY_ALL_DEVICE_STATUS";
        
        // ========== 设备人员同步命令 ==========
        /** 查询设备授权列表 */
        String QUERY_AUTH = "QUERY_AUTH";
        /** 清空所有授权 */
        String CLEAR_ALL_AUTH = "CLEAR_ALL_AUTH";
    }

}
