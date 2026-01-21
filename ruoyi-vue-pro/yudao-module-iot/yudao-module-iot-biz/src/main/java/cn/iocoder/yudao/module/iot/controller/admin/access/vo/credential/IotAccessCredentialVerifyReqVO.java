package cn.iocoder.yudao.module.iot.controller.admin.access.vo.credential;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 凭证验证开门请求 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 凭证验证开门请求 VO")
@Data
public class IotAccessCredentialVerifyReqVO {

    @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "通道ID不能为空")
    private Long channelId;

    @Schema(description = "凭证类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "CARD")
    @NotEmpty(message = "凭证类型不能为空")
    private String credentialType;

    @Schema(description = "凭证数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotEmpty(message = "凭证数据不能为空")
    private String credentialData;

    @Schema(description = "是否验证权限", example = "true")
    private Boolean verifyPermission = true;

    /**
     * 凭证类型枚举
     */
    public static class CredentialType {
        /** 卡号 */
        public static final String CARD = "CARD";
        /** 密码 */
        public static final String PASSWORD = "PASSWORD";
        /** 人脸 */
        public static final String FACE = "FACE";
        /** 指纹 */
        public static final String FINGERPRINT = "FINGERPRINT";
    }

}
