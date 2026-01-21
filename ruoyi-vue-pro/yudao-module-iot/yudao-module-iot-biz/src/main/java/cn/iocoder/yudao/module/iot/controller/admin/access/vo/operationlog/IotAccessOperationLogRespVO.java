package cn.iocoder.yudao.module.iot.controller.admin.access.vo.operationlog;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁操作日志 Response VO
 * 
 * 支持两类操作日志：
 * 1. 门控操作日志：开门、关门、常开、常闭等
 * 2. 授权操作日志：授权下发、授权撤销等（Requirements: 12.1, 12.2, 12.3）
 */
@Schema(description = "管理后台 - 门禁操作日志 Response VO")
@Data
public class IotAccessOperationLogRespVO {

    @Schema(description = "日志ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "操作类型（open_door/close_door/always_open/always_closed/cancel_always/auth_dispatch_group/auth_dispatch_person/auth_revoke等）", example = "open_door")
    private String operationType;

    @Schema(description = "操作类型名称", example = "远程开门")
    private String operationTypeName;

    @Schema(description = "设备ID", example = "110")
    private Long deviceId;

    @Schema(description = "设备名称", example = "大门门禁")
    private String deviceName;

    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    @Schema(description = "通道名称", example = "1号门")
    private String channelName;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "管理员")
    private String operatorName;

    @Schema(description = "操作结果（0失败 1成功）", example = "1")
    private Integer result;

    @Schema(description = "结果描述", example = "操作成功")
    private String resultDesc;
    
    @Schema(description = "错误信息", example = "设备离线")
    private String errorMessage;

    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    // ========== 授权操作扩展字段（Requirements: 12.1, 12.2, 12.3） ==========
    
    @Schema(description = "目标人员ID（授权操作时使用）", example = "1001")
    private Long targetPersonId;
    
    @Schema(description = "目标人员编号（授权操作时使用）", example = "EMP001")
    private String targetPersonCode;
    
    @Schema(description = "目标人员姓名（授权操作时使用）", example = "张三")
    private String targetPersonName;
    
    @Schema(description = "权限组ID（授权操作时使用）", example = "3001")
    private Long permissionGroupId;
    
    @Schema(description = "权限组名称（授权操作时使用）", example = "办公区权限组")
    private String permissionGroupName;
    
    @Schema(description = "授权任务ID（授权操作时使用）", example = "2001")
    private Long authTaskId;
    
    @Schema(description = "凭证类型列表（逗号分隔，如：FACE,CARD,FINGERPRINT）", example = "FACE,CARD")
    private String credentialTypes;
    
    @Schema(description = "成功的凭证类型数量", example = "2")
    private Integer successCredentialCount;
    
    @Schema(description = "失败的凭证类型数量", example = "0")
    private Integer failedCredentialCount;
    
    @Schema(description = "SDK错误码", example = "0")
    private Integer sdkErrorCode;

}
