package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客申请创建 Request VO
 */
@Schema(description = "管理后台 - 访客申请创建 Request VO")
@Data
public class IotVisitorApplyCreateReqVO {

    // ========== 访客信息 ==========

    @Schema(description = "访客ID（已有访客时传入）")
    private Long visitorId;

    @Schema(description = "访客姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "访客姓名不能为空")
    private String visitorName;

    @Schema(description = "性别：0-未知，1-男，2-女", example = "1")
    private Integer gender;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "联系电话不能为空")
    private String visitorPhone;

    @Schema(description = "身份证号码", example = "110101199001011234")
    private String idCard;

    @Schema(description = "所属单位", example = "XX科技公司")
    private String company;

    // ========== 被访人信息 ==========

    @Schema(description = "被访人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "被访人不能为空")
    private Long visiteeId;

    // ========== 来访信息 ==========

    @Schema(description = "来访事由", requiredMode = Schema.RequiredMode.REQUIRED, example = "商务洽谈")
    @NotBlank(message = "来访事由不能为空")
    private String visitReason;

    @Schema(description = "计划来访时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划来访时间不能为空")
    private LocalDateTime planVisitTime;

    @Schema(description = "计划离开时间")
    private LocalDateTime planLeaveTime;

    // ========== 授权信息 ==========

    @Schema(description = "门禁卡号")
    private String cardNo;

    @Schema(description = "人脸照片URL")
    private String faceUrl;

    @Schema(description = "授权开始时间")
    private LocalDateTime authStartTime;

    @Schema(description = "授权结束时间")
    private LocalDateTime authEndTime;

    @Schema(description = "授权设备ID列表")
    private List<Long> deviceIds;

    @Schema(description = "授权通道ID列表")
    private List<Long> channelIds;

    @Schema(description = "备注")
    private String remark;

}
