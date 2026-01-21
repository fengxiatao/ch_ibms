package cn.iocoder.yudao.module.iot.controller.admin.device.vo.device;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotLocationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Schema(description = "管理后台 - IoT 设备新增/修改 Request VO")
@Data
public class IotDeviceSaveReqVO {

    @Schema(description = "设备编号", example = "177")
    private Long id;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.AUTO, example = "王五")
    private String deviceName;

    @Schema(description = "设备账号（用于协议认证）", example = "alarm001")
    private String account;

    @Schema(description = "备注名称", example = "张三")
    private String nickname;

    @Schema(description = "设备序列号", example = "123456")
    private String serialNumber;

    @Schema(description = "DXF实体唯一标识（用于关联DXF图纸中的设备）", example = "1A3F")
    private String dxfEntityId;

    @Schema(description = "设备图片", example = "https://changhui-tech.com/1.png")
    private String picUrl;

    @Schema(description = "设备分组编号数组", example = "1,2")
    private Set<Long> groupIds;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26202")
    private Long productId;

    @Schema(description = "关联的菜单ID列表（JSON数组）", example = "[1001, 1002, 1003]")
    private String menuIds;

    @Schema(description = "主要菜单ID", example = "1001")
    private Long primaryMenuId;

    @Schema(description = "是否手动覆盖菜单配置", example = "false")
    private Boolean menuOverride;

    @Schema(description = "网关设备 ID", example = "16380")
    private Long gatewayId;

    @Schema(description = "设备配置", example = "{\"abc\": \"efg\"}")
    private String config;

    @Schema(description = "定位类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(value = IotLocationTypeEnum.class, message = "定位方式必须是 {value}")
    private Integer locationType;

    @Schema(description = "设备位置的纬度", example = "16380")
    private BigDecimal latitude;

    @Schema(description = "设备位置的经度", example = "16380")
    private BigDecimal longitude;

    // ========== 室内空间定位字段 ==========

    @Schema(description = "所属园区ID", example = "1")
    private Long campusId;

    @Schema(description = "所属建筑ID", example = "2")
    private Long buildingId;

    @Schema(description = "所属楼层ID", example = "3")
    private Long floorId;

    @Schema(description = "所属区域ID（房间）", example = "4")
    private Long roomId;

    @Schema(description = "X坐标（东西方向，米）", example = "5.5")
    private BigDecimal localX;

    @Schema(description = "Y坐标（南北方向，米）", example = "3.2")
    private BigDecimal localY;

    @Schema(description = "Z坐标（安装高度，米）", example = "2.8")
    private BigDecimal localZ;

    @Schema(description = "安装位置描述", example = "天花板中央")
    private String installLocation;

    @Schema(description = "安装高度类型", example = "ceiling")
    private String installHeightType;

}