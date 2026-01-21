package cn.iocoder.yudao.module.iot.controller.admin.access.vo.person;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁人员 Response VO
 */
@Schema(description = "管理后台 - 门禁人员 Response VO")
@Data
public class IotAccessPersonRespVO {

    @Schema(description = "人员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    @Schema(description = "人员类型（1员工 2访客 3临时）", example = "1")
    private Integer personType;

    @Schema(description = "部门ID", example = "1")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "人脸照片URL", example = "https://example.com/face.jpg")
    private String faceUrl;

    @Schema(description = "有效期开始时间")
    private LocalDateTime validStart;

    @Schema(description = "有效期结束时间")
    private LocalDateTime validEnd;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "指纹数量", example = "2")
    private Integer fingerprintCount;

    @Schema(description = "凭证列表")
    private List<CredentialVO> credentials;

    @Schema(description = "凭证信息")
    @Data
    public static class CredentialVO {
        @Schema(description = "凭证ID", example = "1")
        private Long id;

        @Schema(description = "凭证类型（password/card/fingerprint/face）", example = "card")
        private String credentialType;

        @Schema(description = "凭证数据（卡号等）", example = "12345678")
        private String credentialData;

        @Schema(description = "是否已同步到设备", example = "true")
        private Boolean deviceSynced;
    }

}
