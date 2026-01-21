package cn.iocoder.yudao.module.iot.controller.admin.access.vo.credential;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 凭证验证开门响应 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 凭证验证开门响应 VO")
@Data
public class IotAccessCredentialVerifyRespVO {

    @Schema(description = "验证是否成功", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean success;

    @Schema(description = "验证结果消息", example = "验证成功，开门成功")
    private String message;

    @Schema(description = "人员ID", example = "1")
    private Long personId;

    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    @Schema(description = "人员编号", example = "P001")
    private String personCode;

    @Schema(description = "凭证类型", example = "CARD")
    private String credentialType;

    @Schema(description = "凭证数据", example = "1234567890")
    private String credentialData;

    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    @Schema(description = "通道名称", example = "一号门")
    private String channelName;

    @Schema(description = "设备ID", example = "110")
    private Long deviceId;

    @Schema(description = "设备名称", example = "一号门门禁")
    private String deviceName;

    @Schema(description = "开门时间")
    private LocalDateTime openTime;

    @Schema(description = "失败原因", example = "凭证不存在")
    private String failReason;

}
