package cn.iocoder.yudao.module.iot.controller.admin.access.vo.person;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁人员创建 Request VO
 */
@Schema(description = "管理后台 - 门禁人员创建 Request VO")
@Data
public class IotAccessPersonCreateReqVO {

    @Schema(description = "人员编号（不填则自动生成YG+6位流水号）", example = "YG000001")
    @Size(max = 50, message = "人员编号长度不能超过50个字符")
    private String personCode;

    @Schema(description = "人员姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "人员姓名不能为空")
    @Size(max = 50, message = "人员姓名长度不能超过50个字符")
    private String personName;

    @Schema(description = "人员类型（1员工 2访客 3临时）", example = "1")
    private Integer personType;

    @Schema(description = "部门ID", example = "1")
    private Long deptId;

    @Schema(description = "身份证号", example = "110101199001011234")
    @Size(max = 20, message = "身份证号长度不能超过20个字符")
    private String idCard;

    @Schema(description = "手机号", example = "13800138000")
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Schema(description = "人脸照片URL", example = "https://example.com/face.jpg")
    private String faceUrl;

    @Schema(description = "有效期开始时间")
    private LocalDateTime validStart;

    @Schema(description = "有效期结束时间")
    private LocalDateTime validEnd;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Integer status;

}
