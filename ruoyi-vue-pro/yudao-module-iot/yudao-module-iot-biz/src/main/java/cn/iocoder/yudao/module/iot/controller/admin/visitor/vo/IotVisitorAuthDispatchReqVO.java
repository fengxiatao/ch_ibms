package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客权限下发 Request VO
 */
@Schema(description = "管理后台 - 访客权限下发 Request VO")
@Data
public class IotVisitorAuthDispatchReqVO {

    @Schema(description = "申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "申请ID不能为空")
    private Long applyId;

    @Schema(description = "门禁卡号")
    private String cardNo;

    @Schema(description = "人脸照片URL")
    private String faceUrl;

    @Schema(description = "时间模板ID")
    private Long timeTemplateId;

    @Schema(description = "授权类型：1-按时间段，2-按次数，3-时间+次数", example = "1")
    private Integer authType;

    @Schema(description = "授权开始时间")
    private LocalDateTime authStartTime;

    @Schema(description = "授权结束时间")
    private LocalDateTime authEndTime;

    @Schema(description = "最大通行次数")
    private Integer maxAccessCount;

    @Schema(description = "每日通行次数限制")
    private Integer dailyAccessLimit;

    @Schema(description = "授权设备ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "授权设备不能为空")
    private List<Long> deviceIds;

    @Schema(description = "授权通道ID列表")
    private List<Long> channelIds;

}
