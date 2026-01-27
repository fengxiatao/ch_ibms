package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - 摄像头巡航路线保存 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 摄像头巡航路线保存 Request VO")
@Data
public class CameraCruiseSaveReqVO {

    @Schema(description = "巡航路线ID", example = "1")
    private Long id;

    @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    @NotNull(message = "通道ID不能为空")
    private Long channelId;

    @Schema(description = "巡航路线名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "巡航路线1")
    @NotBlank(message = "巡航路线名称不能为空")
    @Size(max = 100, message = "巡航路线名称长度不能超过100个字符")
    private String cruiseName;

    @Schema(description = "描述", example = "这是一条巡航路线")
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    @Schema(description = "每个预设点停留时间（秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "停留时间不能为空")
    @Min(value = 1, message = "停留时间最小为1秒")
    @Max(value = 300, message = "停留时间最大为300秒")
    private Integer dwellTime;

    @Schema(description = "是否循环", example = "true")
    private Boolean loopEnabled;

    @Schema(description = "巡航点列表（创建时可为空，同步到设备时校验）")
    private List<CameraCruisePointSaveReqVO> points;

}
