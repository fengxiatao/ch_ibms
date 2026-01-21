package cn.iocoder.yudao.module.iot.controller.admin.subsystem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - IoT子系统 Response VO")
@Data
public class SubsystemRespVO {

    @Schema(description = "子系统ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "子系统代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "security.video")
    private String code;

    @Schema(description = "子系统名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "视频监控")
    private String name;

    @Schema(description = "父系统代码", example = "security")
    private String parentCode;

    @Schema(description = "层级", example = "2")
    private Integer level;

    @Schema(description = "关联的菜单ID", example = "1001")
    private Long menuId;

    @Schema(description = "菜单路径", example = "/iot/security/video")
    private String menuPath;

    @Schema(description = "图标", example = "ep:video-camera")
    private String icon;

    @Schema(description = "描述", example = "视频监控系统")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "产品数量（统计字段）", example = "10")
    private Integer productCount;

    @Schema(description = "设备数量（统计字段）", example = "100")
    private Integer deviceCount;

    @Schema(description = "子系统列表（树形结构）")
    private List<SubsystemRespVO> children;
}


















































