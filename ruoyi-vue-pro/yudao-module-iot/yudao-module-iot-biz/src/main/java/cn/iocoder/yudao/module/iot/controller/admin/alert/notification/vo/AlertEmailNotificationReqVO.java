package cn.iocoder.yudao.module.iot.controller.admin.alert.notification.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - IoT 告警邮件通知 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 告警邮件通知 Request VO")
@Data
public class AlertEmailNotificationReqVO {

    @Schema(description = "发件人", example = "noreply@ch-ibms.com")
    private String from;

    @Schema(description = "收件人列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "收件人列表不能为空")
    private List<String> recipients;

    @Schema(description = "邮件主题", requiredMode = Schema.RequiredMode.REQUIRED, example = "【告警通知】运动检测告警")
    @NotEmpty(message = "邮件主题不能为空")
    private String subject;

    @Schema(description = "邮件内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "邮件内容不能为空")
    private String content;

    @Schema(description = "是否HTML格式", example = "false")
    private Boolean htmlFormat = false;
}












