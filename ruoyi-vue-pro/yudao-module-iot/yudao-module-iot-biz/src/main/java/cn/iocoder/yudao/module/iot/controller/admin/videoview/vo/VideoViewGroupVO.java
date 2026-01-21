package cn.iocoder.yudao.module.iot.controller.admin.videoview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频监控 - 实时预览视图分组 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 视频监控视图分组 VO")
@Data
public class VideoViewGroupVO {

    @Schema(description = "分组ID", example = "1")
    private Long id;

    @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认分组")
    private String name;

    @Schema(description = "图标", example = "ep:folder")
    private String icon;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
