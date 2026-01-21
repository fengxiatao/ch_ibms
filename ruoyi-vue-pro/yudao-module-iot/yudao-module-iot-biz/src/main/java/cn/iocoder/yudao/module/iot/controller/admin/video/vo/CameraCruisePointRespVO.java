package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 摄像头巡航点 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 摄像头巡航点 Response VO")
@Data
public class CameraCruisePointRespVO {

    @Schema(description = "巡航点ID", example = "1")
    private Long id;

    @Schema(description = "预设点ID", example = "1")
    private Long presetId;

    @Schema(description = "预设点编号", example = "1")
    private Integer presetNo;

    @Schema(description = "预设点名称", example = "预设点1")
    private String presetName;

    @Schema(description = "顺序", example = "1")
    private Integer sortOrder;

    @Schema(description = "停留时间（秒）", example = "10")
    private Integer dwellTime;

}
