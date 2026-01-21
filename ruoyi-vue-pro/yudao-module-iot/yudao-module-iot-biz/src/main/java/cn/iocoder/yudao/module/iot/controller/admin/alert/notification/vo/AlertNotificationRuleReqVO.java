package cn.iocoder.yudao.module.iot.controller.admin.alert.notification.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - IoT 告警通知规则 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 告警通知规则 Request VO")
@Data
public class AlertNotificationRuleReqVO {

    @Schema(description = "告警配置ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "告警配置ID不能为空")
    private Long alertConfigId;

    @Schema(description = "是否启用邮件通知", example = "true")
    private Boolean enableEmail = false;

    @Schema(description = "邮件接收人列表")
    private List<String> emailRecipients;

    @Schema(description = "是否启用短信通知", example = "true")
    private Boolean enableSms = false;

    @Schema(description = "短信接收人手机号列表")
    private List<String> smsRecipients;

    @Schema(description = "是否启用WebSocket推送", example = "true")
    private Boolean enableWebSocket = true;

    @Schema(description = "WebSocket推送目标用户ID列表")
    private List<Long> webSocketUserIds;

    @Schema(description = "是否启用系统内消息", example = "true")
    private Boolean enableSystemMessage = true;

    @Schema(description = "系统内消息接收人用户ID列表")
    private List<Long> systemMessageUserIds;
}





