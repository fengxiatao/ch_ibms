package cn.iocoder.yudao.module.iot.controller.admin.access.vo.person;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁人员批量创建 Request VO
 */
@Schema(description = "管理后台 - 门禁人员批量创建 Request VO")
@Data
public class IotAccessPersonBatchCreateReqVO {

    @Schema(description = "人员列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "人员列表不能为空")
    @Valid
    private List<PersonItem> persons;

    @Schema(description = "单个人员信息")
    @Data
    public static class PersonItem {

        @Schema(description = "人员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100001")
        @NotBlank(message = "人员编号不能为空")
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

        @Schema(description = "有效期开始时间")
        private LocalDateTime validStart;

        @Schema(description = "有效期结束时间")
        private LocalDateTime validEnd;

        @Schema(description = "卡号（可选）", example = "12345678")
        @Size(max = 32, message = "卡号长度不能超过32个字符")
        private String cardNo;
    }
}
