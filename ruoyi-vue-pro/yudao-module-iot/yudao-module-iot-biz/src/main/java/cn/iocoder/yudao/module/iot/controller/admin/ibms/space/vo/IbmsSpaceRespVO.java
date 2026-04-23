package cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IbmsSpaceRespVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "父空间ID（0 为根）")
    private Long parentId;

    @Schema(description = "空间编码（组合）：code[-subCode]，如 F01 / F01-LBY")
    private String spaceCode;

    @Schema(description = "区域码")
    private String code;

    @Schema(description = "子区域码")
    private String subCode;

    @Schema(description = "空间名称")
    private String name;

    @Schema(description = "空间类型：floor/area/room")
    private String type;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "扩展 JSON 字符串")
    private String extra;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

