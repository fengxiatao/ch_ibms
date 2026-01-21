package cn.iocoder.yudao.module.iot.controller.admin.device.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 管理后台 - IoT 设备事件配置 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备事件配置 Request VO")
@Data
public class DeviceEventConfigReqVO {

    @Schema(description = "运动检测启用", example = "true")
    private Boolean motionDetectionEnabled;

    @Schema(description = "运动检测灵敏度 (1-100)", example = "50")
    @Min(value = 1, message = "运动检测灵敏度必须在1-100之间")
    @Max(value = 100, message = "运动检测灵敏度必须在1-100之间")
    private Integer motionSensitivity;

    @Schema(description = "视频丢失检测启用", example = "true")
    private Boolean videoLossEnabled;

    @Schema(description = "视频遮挡检测启用", example = "true")
    private Boolean tamperDetectionEnabled;

    @Schema(description = "音频检测启用", example = "false")
    private Boolean audioDetectionEnabled;

    @Schema(description = "音频检测阈值 (1-100)", example = "30")
    @Min(value = 1, message = "音频检测阈值必须在1-100之间")
    @Max(value = 100, message = "音频检测阈值必须在1-100之间")
    private Integer audioThreshold;
}












