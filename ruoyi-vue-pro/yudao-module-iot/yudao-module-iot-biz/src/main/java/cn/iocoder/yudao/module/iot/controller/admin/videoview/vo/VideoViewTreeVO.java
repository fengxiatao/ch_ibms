package cn.iocoder.yudao.module.iot.controller.admin.videoview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 视频监控 - 实时预览视图树 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 视频监控视图树 VO")
@Data
public class VideoViewTreeVO {

    @Schema(description = "节点ID", example = "1")
    private Long id;

    @Schema(description = "节点名称", example = "默认分组")
    private String name;

    @Schema(description = "图标", example = "ep:folder")
    private String icon;

    @Schema(description = "节点类型(group/view)", example = "group")
    private String type;

    @Schema(description = "分屏格式(仅视图节点)", example = "9")
    private Integer gridLayout;

    @Schema(description = "窗格数量(仅视图节点)", example = "5")
    private Integer paneCount;

    @Schema(description = "子节点列表")
    private List<VideoViewTreeVO> children;

}
