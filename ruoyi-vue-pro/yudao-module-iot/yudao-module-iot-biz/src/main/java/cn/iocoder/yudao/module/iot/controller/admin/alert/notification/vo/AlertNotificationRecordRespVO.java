package cn.iocoder.yudao.module.iot.controller.admin.alert.notification.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - IoT 告警通知记录 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 告警通知记录 Response VO")
@Data
public class AlertNotificationRecordRespVO {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "告警ID", example = "1")
    private Long alertId;

    @Schema(description = "通知类型", example = "email")
    private String notificationType;

    @Schema(description = "接收人", example = "admin@example.com")
    private String recipient;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "发送状态", example = "SUCCESS")
    private String sendStatus;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}





