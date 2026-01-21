package cn.iocoder.yudao.module.iot.controller.admin.device.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理后台 - IoT 设备视频配置 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备视频配置 Request VO")
@Data
public class DeviceVideoConfigReqVO {

    @Schema(description = "分辨率", example = "1920x1080")
    @Pattern(regexp = "^\\d+x\\d+$", message = "分辨率格式不正确，应为宽x高，如1920x1080")
    private String resolution;

    @Schema(description = "帧率", example = "25")
    @Min(value = 1, message = "帧率必须大于0")
    @Max(value = 60, message = "帧率不能超过60")
    private Integer frameRate;

    @Schema(description = "码率 (kbps)", example = "4096")
    @Min(value = 128, message = "码率必须大于128kbps")
    @Max(value = 16384, message = "码率不能超过16384kbps")
    private Integer bitrate;

    @Schema(description = "编码格式", example = "H.264")
    @Pattern(regexp = "^(H\\.264|H\\.265|MJPEG)$", message = "编码格式必须为H.264、H.265或MJPEG")
    private String codecType;

    @Schema(description = "图像质量 (1-100)", example = "80")
    @Min(value = 1, message = "图像质量必须在1-100之间")
    @Max(value = 100, message = "图像质量必须在1-100之间")
    private Integer quality;

    @Schema(description = "GOP长度", example = "50")
    @Min(value = 1, message = "GOP长度必须大于0")
    @Max(value = 300, message = "GOP长度不能超过300")
    private Integer gopLength;
}












