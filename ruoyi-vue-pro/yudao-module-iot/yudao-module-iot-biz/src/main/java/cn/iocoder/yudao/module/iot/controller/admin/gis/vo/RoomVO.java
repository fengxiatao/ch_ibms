package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房间信息 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 房间信息 VO")
@Data
public class RoomVO {

    @Schema(description = "房间ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long id;

    @Schema(description = "楼层ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long floorId;

    @Schema(description = "建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "房间名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "A栋1层01室")
    private String name;

    @Schema(description = "房间编码", example = "101")
    private String code;

    @Schema(description = "房间号", example = "101")
    private String roomNumber;

    @Schema(description = "房间类型", example = "办公室")
    private String roomType;

    @Schema(description = "房间面积（平方米）", example = "480")
    private BigDecimal roomArea;

    @Schema(description = "层高（米）", example = "3.0")
    private BigDecimal ceilingHeight;

    @Schema(description = "主要用途", example = "办公")
    private String primaryUse;

    @Schema(description = "容纳人数", example = "20")
    private Integer capacity;

    @Schema(description = "是否公共区域", example = "false")
    private Boolean isPublic;

    @Schema(description = "访问权限等级", example = "普通")
    private String accessLevel;

    @Schema(description = "占用状态", example = "已占用")
    private String occupancyStatus;

    @Schema(description = "几何数据（WKT格式）", example = "POLYGON((0 0, 16 0, 16 30, 0 30, 0 0))")
    private String geom;

    @Schema(description = "设备列表")
    private List<DeviceVO> devices;

    @Schema(description = "设备数量", example = "6")
    private Integer deviceCount;

    @Schema(description = "备注", example = "财务部办公室")
    private String remark;

}












