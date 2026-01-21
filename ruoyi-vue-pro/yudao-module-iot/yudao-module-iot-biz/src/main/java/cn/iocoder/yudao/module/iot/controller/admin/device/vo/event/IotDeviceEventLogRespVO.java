package cn.iocoder.yudao.module.iot.controller.admin.device.vo.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IoT 设备事件日志 - 响应 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备事件日志 Response VO")
@Data
public class IotDeviceEventLogRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "产品ID", example = "1")
    private Long productId;

    @Schema(description = "产品标识", example = "camera_bullet")
    private String productKey;

    @Schema(description = "设备名称", example = "device_192_168_1_202")
    private String deviceName;

    @Schema(description = "事件标识符", example = "MotionAlarm")
    private String eventIdentifier;

    @Schema(description = "事件名称", example = "移动侦测告警")
    private String eventName;

    @Schema(description = "事件类型", example = "alert")
    private String eventType;

    @Schema(description = "事件数据", example = "{\"region\":1}")
    private String eventData;

    @Schema(description = "事件发生时间")
    private LocalDateTime eventTime;

    @Schema(description = "ONVIF原始Topic", example = "tns1:RuleEngine/MotionDetection/Motion")
    private String onvifTopic;

    @Schema(description = "是否已处理", example = "true")
    private Boolean processed;

    @Schema(description = "触发的场景规则ID列表", example = "[1,2,3]")
    private String triggeredSceneRuleIds;

    @Schema(description = "生成的告警记录ID列表", example = "[1,2,3]")
    private String generatedAlertRecordIds;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}












