package cn.iocoder.yudao.module.iot.controller.admin.alert.notification.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - IoT 告警通知规则 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 告警通知规则 Response VO")
@Data
public class AlertNotificationRuleRespVO {

    @Schema(description = "规则ID", example = "1")
    private Long id;

    @Schema(description = "告警配置ID", example = "1")
    private Long alertConfigId;

    @Schema(description = "规则名称", example = "默认通知规则")
    private String ruleName;

    @Schema(description = "是否启用邮件通知", example = "true")
    private Boolean enableEmail;

    @Schema(description = "邮件接收人列表")
    private List<String> emailRecipients;

    @Schema(description = "邮件通知启用状态", example = "true")
    private Boolean emailEnabled;

    @Schema(description = "是否启用短信通知", example = "true")
    private Boolean enableSms;

    @Schema(description = "短信接收人手机号列表")
    private List<String> smsRecipients;

    @Schema(description = "短信通知启用状态", example = "true")
    private Boolean smsEnabled;

    @Schema(description = "是否启用WebSocket推送", example = "true")
    private Boolean enableWebSocket;

    @Schema(description = "WebSocket推送目标用户ID列表")
    private List<Long> webSocketUserIds;

    @Schema(description = "WebSocket推送启用状态", example = "true")
    private Boolean websocketEnabled;

    @Schema(description = "是否启用系统内消息", example = "true")
    private Boolean enableSystemMessage;

    @Schema(description = "系统内消息接收人用户ID列表")
    private List<Long> systemMessageUserIds;

    @Schema(description = "系统消息启用状态", example = "true")
    private Boolean systemEnabled;

    @Schema(description = "通知间隔（秒）", example = "300")
    private Integer notifyInterval;

    @Schema(description = "最大通知次数", example = "10")
    private Integer maxNotifyCount;

    @Schema(description = "规则状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

