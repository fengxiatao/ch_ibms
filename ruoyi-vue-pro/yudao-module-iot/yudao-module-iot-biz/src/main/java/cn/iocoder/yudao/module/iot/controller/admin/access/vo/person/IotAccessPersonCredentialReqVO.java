package cn.iocoder.yudao.module.iot.controller.admin.access.vo.person;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门禁人员凭证 Request VO
 */
@Schema(description = "管理后台 - 门禁人员凭证 Request VO")
@Data
public class IotAccessPersonCredentialReqVO {

    @Schema(description = "人员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "人员ID不能为空")
    private Long personId;

    @Schema(description = "凭证类型（password/card/fingerprint/face）", example = "card")
    private String credentialType;

    @Schema(description = "凭证数据（密码/卡号等）", example = "12345678")
    private String credentialData;

    @Schema(description = "手指索引（指纹时使用，0-9）", example = "0")
    private Integer fingerIndex;

}
