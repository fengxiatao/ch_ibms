package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - 设备位置信息新增/修改 Request VO")
@Data
public class DeviceLocationSaveReqVO {

    @Schema(description = "ID（修改时必填）", example = "1")
    private Long id;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "所属楼层ID", example = "10")
    private Long floorId;

    @Schema(description = "所属建筑ID", example = "5")
    private Long buildingId;

    @Schema(description = "所属区域ID", example = "100")
    private Long areaId;

    @Schema(description = "本地X坐标（米）", requiredMode = Schema.RequiredMode.REQUIRED, example = "15.5")
    @NotNull(message = "本地X坐标不能为空")
    private BigDecimal localX;

    @Schema(description = "本地Y坐标（米）", requiredMode = Schema.RequiredMode.REQUIRED, example = "20.3")
    @NotNull(message = "本地Y坐标不能为空")
    private BigDecimal localY;

    @Schema(description = "本地Z坐标（米）", example = "1.5")
    private BigDecimal localZ;

    @Schema(description = "安装日期", example = "2024-01-15")
    private LocalDate installDate;

    @Schema(description = "安装人员", example = "张三")
    private String installer;

    @Schema(description = "备注", example = "安装在天花板上")
    private String remark;

}


















