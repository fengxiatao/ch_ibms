package cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class IbmsSpaceTreeNodeRespVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "父空间ID（0 为根）")
    private Long parentId;

    @Schema(description = "空间名称")
    private String name;

    @Schema(description = "空间编码（组合）：code[-subCode]，如 F01 / F01-LBY")
    private String spaceCode;

    @Schema(description = "空间类型：floor/area/room")
    private String type;

    @Schema(description = "子节点")
    private List<IbmsSpaceTreeNodeRespVO> children;
}

