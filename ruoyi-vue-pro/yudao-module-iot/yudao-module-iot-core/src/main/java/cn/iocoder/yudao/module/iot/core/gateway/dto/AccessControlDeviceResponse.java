package cn.iocoder.yudao.module.iot.core.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 门禁设备控制响应
 * 
 * <p>用于 Gateway → Biz 的门禁设备控制响应</p>
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessControlDeviceResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求ID，与请求对应
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
     * 通道ID
     */
    private Long channelId;

    /**
     * 人员ID（用于下发响应关联）
     */
    private Long personId;

    /**
     * 任务ID（用于下发响应关联）
     */
    private Long taskId;

    /**
     * 任务明细ID（用于下发响应关联）
     */
    private Long taskDetailId;

    /**
     * 命令类型
     */
    private String commandType;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误码
     */
    private Integer errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 发现的通道列表（DISCOVER_CHANNELS命令使用）
     */
    private List<ChannelInfo> channels;

    /**
     * 扩展数据
     */
    private Map<String, Object> data;

    // ========== 凭证下发响应字段 ==========

    /**
     * 成功下发的凭证类型列表
     */
    private List<String> successCredentials;

    /**
     * 下发失败的凭证列表
     */
    private List<CredentialError> failedCredentials;

    /**
     * 设备能力信息（QUERY_DEVICE_CAPABILITY命令使用）
     */
    private DeviceCapability deviceCapability;

    /**
     * 设备是否在线（CHECK_DEVICE_ONLINE命令使用）
     */
    private Boolean isOnline;

    /**
     * 登录句柄（GET_LOGIN_HANDLE命令使用）
     */
    private Long loginHandle;

    /**
     * 设备激活状态列表（QUERY_ALL_DEVICE_STATUS命令使用）
     */
    private List<cn.iocoder.yudao.module.iot.core.gateway.dto.access.DeviceActivationStatus> deviceStatuses;

    /**
     * 通道信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** 通道索引(0-based,SDK原始值) */
        private Integer channelIndex;
        /** 通道号(1-based,显示用) */
        private Integer channelNo;
        /** 通道名称 */
        private String channelName;
        /** 通道类型 */
        private String channelType;
        /** 门状态 */
        private String doorStatus;
        /** 门状态码 */
        private Integer doorState;
        /** 锁状态 */
        private String lockStatus;
        /** 门模式 */
        private String doorMode;
    }

    /**
     * 凭证下发错误信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CredentialError implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** 凭证类型 */
        private String credentialType;
        /** 错误码 */
        private Integer errorCode;
        /** 错误信息 */
        private String errorMessage;
    }

    /**
     * 设备能力信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceCapability implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** 是否支持人脸服务 */
        private Boolean supFaceService;
        /** 是否支持卡服务 */
        private Boolean supCardService;
        /** 是否支持指纹服务 */
        private Boolean supFingerprintService;
        /** 是否支持密码服务 */
        private Boolean supPasswordService;
        /** 最大用户数 */
        private Integer maxUserCount;
        /** 最大卡数 */
        private Integer maxCardCount;
        /** 最大人脸数 */
        private Integer maxFaceCount;
        /** 最大指纹数 */
        private Integer maxFingerprintCount;
        /** 当前用户数 */
        private Integer currentUserCount;
        /** 当前卡数 */
        private Integer currentCardCount;
        /** 当前人脸数 */
        private Integer currentFaceCount;
        /** 当前指纹数 */
        private Integer currentFingerprintCount;
        
        // ========== 视频能力 ==========
        
        /** 是否支持视频预览 */
        private Boolean supportVideo;
        /** 视频通道数量 */
        private Integer videoChannelCount;
    }

    /**
     * 创建成功响应
     */
    public static AccessControlDeviceResponse success(String requestId, Long deviceId, String commandType) {
        return AccessControlDeviceResponse.builder()
                .requestId(requestId)
                .deviceId(deviceId)
                .commandType(commandType)
                .success(true)
                .build();
    }

    /**
     * 创建失败响应
     */
    public static AccessControlDeviceResponse fail(String requestId, Long deviceId, String commandType, 
                                                    Integer errorCode, String errorMessage) {
        return AccessControlDeviceResponse.builder()
                .requestId(requestId)
                .deviceId(deviceId)
                .commandType(commandType)
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

}
