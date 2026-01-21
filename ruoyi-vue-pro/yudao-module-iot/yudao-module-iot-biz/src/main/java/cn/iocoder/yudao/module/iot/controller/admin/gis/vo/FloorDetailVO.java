package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 楼层详情 VO（包含房间和设备）
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 楼层详情 VO")
@Data
public class FloorDetailVO {

    @Schema(description = "楼层信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private FloorVO floor;

    @Schema(description = "房间列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RoomVO> rooms;

}












