package cn.iocoder.yudao.module.iot.controller.admin.access.vo.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门禁事件日志 Response VO
 */
@Schema(description = "管理后台 - 门禁事件日志 Response VO")
@Data
public class IotAccessEventLogRespVO {

    @Schema(description = "事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备ID", example = "110")
    private Long deviceId;

    @Schema(description = "设备名称", example = "大门门禁")
    private String deviceName;

    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    @Schema(description = "通道名称", example = "1号门")
    private String channelName;

    @Schema(description = "事件类型（CARD_SWIPE/FACE_RECOGNIZE/FINGERPRINT/PASSWORD/ALARM等）", example = "FACE_RECOGNIZE")
    private String eventType;

    @Schema(description = "事件类型名称", example = "人脸识别")
    private String eventTypeName;

    @Schema(description = "人员ID", example = "1")
    private Long personId;

    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    @Schema(description = "卡号", example = "12345678")
    private String cardNo;

    @Schema(description = "验证方式", example = "FACE")
    private String verifyMode;

    @Schema(description = "验证结果（0失败 1成功）", example = "1")
    private Integer verifyResult;

    @Schema(description = "验证结果描述", example = "验证成功")
    private String verifyResultDesc;

    @Schema(description = "失败原因", example = "人脸不匹配")
    private String failReason;

    @Schema(description = "抓拍图片URL", example = "https://example.com/capture.jpg")
    private String captureUrl;

    @Schema(description = "体温（℃）", example = "36.5")
    private BigDecimal temperature;

    @Schema(description = "口罩状态（0-未佩戴，1-已佩戴）", example = "1")
    private Integer maskStatus;

    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
