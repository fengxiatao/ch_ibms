package cn.iocoder.yudao.module.iot.controller.admin.alert.notification.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - IoT 告警短信通知 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 告警短信通知 Request VO")
@Data
public class AlertSmsNotificationReqVO {

    @Schema(description = "收件人手机号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "收件人手机号列表不能为空")
    private List<String> phoneNumbers;

    @Schema(description = "短信签名", example = "长辉物联")
    private String signName;

    @Schema(description = "短信模板编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "SMS_001")
    @NotEmpty(message = "短信模板编码不能为空")
    private String templateCode;

    @Schema(description = "短信内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "短信内容不能为空")
    private String content;

    @Schema(description = "模板参数", example = "{\"deviceName\":\"摄像头01\",\"alertType\":\"运动检测\"}")
    private String templateParams;
}

