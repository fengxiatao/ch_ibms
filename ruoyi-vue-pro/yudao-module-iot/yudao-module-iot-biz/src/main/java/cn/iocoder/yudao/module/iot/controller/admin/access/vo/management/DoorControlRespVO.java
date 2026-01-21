package cn.iocoder.yudao.module.iot.controller.admin.access.vo.management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门控操作响应 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 门控操作响应 VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoorControlRespVO {

    @Schema(description = "操作是否成功", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean success;

    @Schema(description = "设备ID", example = "110")
    private Long deviceId;

    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    @Schema(description = "操作类型", example = "OPEN_DOOR")
    private String action;

    @Schema(description = "错误信息", example = "设备离线")
    private String errorMessage;

    @Schema(description = "操作耗时（毫秒）", example = "150")
    private Long duration;

    /**
     * 创建成功响应
     */
    public static DoorControlRespVO success(Long deviceId, Long channelId, String action, Long duration) {
        return DoorControlRespVO.builder()
                .success(true)
                .deviceId(deviceId)
                .channelId(channelId)
                .action(action)
                .duration(duration)
                .build();
    }

    /**
     * 创建失败响应
     */
    public static DoorControlRespVO failure(Long deviceId, Long channelId, String action, String errorMessage) {
        return DoorControlRespVO.builder()
                .success(false)
                .deviceId(deviceId)
                .channelId(channelId)
                .action(action)
                .errorMessage(errorMessage)
                .build();
    }

}
