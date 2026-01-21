package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 管理后台 - 摄像头巡航点保存 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 摄像头巡航点保存 Request VO")
@Data
public class CameraCruisePointSaveReqVO {

    @Schema(description = "预设点ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "预设点ID不能为空")
    private Long presetId;

    @Schema(description = "顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "顺序不能为空")
    @Min(value = 1, message = "顺序最小为1")
    private Integer sortOrder;

    @Schema(description = "停留时间（秒，为空则使用路线默认值）", example = "10")
    @Min(value = 1, message = "停留时间最小为1秒")
    @Max(value = 300, message = "停留时间最大为300秒")
    private Integer dwellTime;

}
