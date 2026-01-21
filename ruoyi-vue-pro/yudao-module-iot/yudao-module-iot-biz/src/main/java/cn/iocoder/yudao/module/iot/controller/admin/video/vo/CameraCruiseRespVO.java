package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - 摄像头巡航路线 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 摄像头巡航路线 Response VO")
@Data
public class CameraCruiseRespVO {

    @Schema(description = "巡航路线ID", example = "1")
    private Long id;

    @Schema(description = "通道ID", example = "123")
    private Long channelId;

    @Schema(description = "巡航路线名称", example = "巡航路线1")
    private String cruiseName;

    @Schema(description = "描述", example = "这是一条巡航路线")
    private String description;

    @Schema(description = "状态（0:停止 1:运行中）", example = "0")
    private Integer status;

    @Schema(description = "每个预设点停留时间（秒）", example = "5")
    private Integer dwellTime;

    @Schema(description = "是否循环", example = "true")
    private Boolean loopEnabled;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "巡航点列表")
    private List<CameraCruisePointRespVO> points;

}
