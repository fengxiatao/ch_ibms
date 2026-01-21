package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 批量更新设备位置 Request VO")
@Data
public class DeviceLocationBatchUpdateReqVO {

    @Schema(description = "设备位置更新列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "设备列表不能为空")
    @Valid
    private List<DevicePositionUpdateDTO> devices;

    @Schema(description = "设备位置更新信息")
    @Data
    public static class DevicePositionUpdateDTO {

        @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
        @NotNull(message = "设备ID不能为空")
        private Long deviceId;

        @Schema(description = "本地X坐标（米）", requiredMode = Schema.RequiredMode.REQUIRED, example = "15.5")
        @NotNull(message = "本地X坐标不能为空")
        private BigDecimal localX;

        @Schema(description = "本地Y坐标（米）", requiredMode = Schema.RequiredMode.REQUIRED, example = "20.3")
        @NotNull(message = "本地Y坐标不能为空")
        private BigDecimal localY;

        @Schema(description = "本地Z坐标（米）", example = "1.5")
        private BigDecimal localZ;

        @Schema(description = "所属区域ID", example = "100")
        private Long areaId;

    }

}


















