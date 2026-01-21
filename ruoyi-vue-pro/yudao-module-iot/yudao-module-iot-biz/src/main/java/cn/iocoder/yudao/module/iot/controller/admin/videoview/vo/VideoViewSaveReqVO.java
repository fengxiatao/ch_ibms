package cn.iocoder.yudao.module.iot.controller.admin.videoview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 视频监控 - 实时预览视图保存 Request VO
 * 用于创建和更新
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 视频监控视图保存 Request VO")
@Data
public class VideoViewSaveReqVO {

    @Schema(description = "视图ID（更新时必填）", example = "1")
    private Long id;

    @Schema(description = "视图名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大厅监控")
    @NotBlank(message = "视图名称不能为空")
    private String name;

    @Schema(description = "分组ID列表", example = "[1,2,3]")
    private List<Long> groupIds;

    @Schema(description = "分屏格式(1/4/6/9/16)", requiredMode = Schema.RequiredMode.REQUIRED, example = "9")
    @NotNull(message = "分屏格式不能为空")
    private Integer gridLayout;

    @Schema(description = "视图描述", example = "大厅区域9分屏监控")
    private String description;

    @Schema(description = "是否默认视图", example = "false")
    private Boolean isDefault;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "窗格配置列表")
    private List<VideoViewPaneVO> panes;

}
