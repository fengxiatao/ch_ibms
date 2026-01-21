package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 长辉升级任务 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉升级任务 Response VO")
@Data
public class ChanghuiUpgradeTaskRespVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deviceId;

    @Schema(description = "测站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    private String stationCode;

    @Schema(description = "固件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long firmwareId;

    @Schema(description = "固件版本", example = "V1.0.0")
    private String firmwareVersion;

    @Schema(description = "升级模式：0=TCP帧传输, 1=HTTP URL下载", example = "1")
    private Integer upgradeMode;

    @Schema(description = "升级模式名称", example = "HTTP URL下载")
    private String upgradeModeName;

    @Schema(description = "固件下载URL", example = "http://example.com/firmware/v1.0.0.bin")
    private String firmwareUrl;

    @Schema(description = "状态：0-待执行,1-进行中,2-成功,3-失败,4-已取消,5-已拒绝", example = "1")
    private Integer status;

    @Schema(description = "状态名称", example = "进行中")
    private String statusName;

    @Schema(description = "进度(0-100)", example = "50")
    private Integer progress;

    @Schema(description = "总帧数", example = "100")
    private Integer totalFrames;

    @Schema(description = "已发送帧数", example = "50")
    private Integer sentFrames;

    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "错误信息", example = "设备离线")
    private String errorMessage;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
