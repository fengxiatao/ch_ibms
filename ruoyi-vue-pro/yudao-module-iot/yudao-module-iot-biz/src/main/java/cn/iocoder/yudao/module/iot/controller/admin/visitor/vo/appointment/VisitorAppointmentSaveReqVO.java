package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 访客预约创建/更新 Request VO")
@Data
public class VisitorAppointmentSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "访客姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "访客姓名不能为空")
    private String name;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800000000")
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @Schema(description = "访客类型：business/vip/contractor/interview", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "访客类型不能为空")
    private String type;

    @Schema(description = "所属单位", example = "北京科技有限公司")
    private String company;

    @Schema(description = "被访人", requiredMode = Schema.RequiredMode.REQUIRED, example = "王经理")
    @NotBlank(message = "被访人不能为空")
    private String host;

    @Schema(description = "被访人部门", example = "技术部")
    private String hostDept;

    @Schema(description = "预约来访时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预约来访时间不能为空")
    private LocalDateTime visitTime;

    @Schema(description = "来访事由", requiredMode = Schema.RequiredMode.REQUIRED, example = "商务洽谈")
    @NotBlank(message = "来访事由不能为空")
    private String reason;

    @Schema(description = "访问区域（枚举值列表，如 lobby/meeting/office/cafeteria）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "访问区域不能为空")
    private List<String> areas;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "车牌号")
    private String carNo;

    @Schema(description = "备注说明")
    private String remark;
}

