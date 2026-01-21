package cn.iocoder.yudao.module.iot.controller.admin.alert.notification.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - IoT 告警WebSocket推送 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 告警WebSocket推送 Request VO")
@Data
public class AlertWebSocketPushReqVO {

    @Schema(description = "目标用户ID列表")
    private List<Long> userIds;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "alert")
    @NotEmpty(message = "消息类型不能为空")
    private String messageType;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "消息内容不能为空")
    private String content;

    @Schema(description = "消息数据（JSON格式）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "消息数据不能为空")
    private String data;

    @Schema(description = "告警ID", example = "1")
    private Long alertId;

    @Schema(description = "告警级别", example = "Critical")
    private String alertLevel;
}

