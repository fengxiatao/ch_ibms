package cn.iocoder.yudao.module.iot.controller.admin.access.vo.person;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁人员凭证响应 VO
 * SmartPSS功能对齐 - 认证Tab
 */
@Schema(description = "管理后台 - 门禁人员凭证响应 VO")
@Data
public class IotAccessPersonCredentialRespVO {

    @Schema(description = "凭证ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "人员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long personId;

    @Schema(description = "凭证类型：PASSWORD-密码，CARD-卡片，FINGERPRINT-指纹，FACE-人脸", requiredMode = Schema.RequiredMode.REQUIRED, example = "CARD")
    private String credentialType;

    @Schema(description = "凭证数据", example = "12345678")
    private String credentialData;

    @Schema(description = "卡号（仅卡片类型）", example = "12345678")
    private String cardNo;

    @Schema(description = "发卡时间", example = "2024-01-01 10:00:00")
    private LocalDateTime issueTime;

    @Schema(description = "换卡时间", example = "2024-06-01 10:00:00")
    private LocalDateTime replaceTime;

    @Schema(description = "卡状态：0-正常，1-挂失，2-注销", example = "0")
    private Integer cardStatus;

    @Schema(description = "旧卡号（换卡时记录）", example = "87654321")
    private String oldCardNo;

    @Schema(description = "指纹序号（仅指纹类型，0-9）", example = "0")
    private Integer fingerIndex;

    @Schema(description = "指纹名称", example = "右手食指")
    private String fingerName;

    @Schema(description = "是否已同步到设备", example = "true")
    private Boolean deviceSynced;

    @Schema(description = "同步时间", example = "2024-01-01 10:00:00")
    private LocalDateTime syncTime;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createTime;

}
