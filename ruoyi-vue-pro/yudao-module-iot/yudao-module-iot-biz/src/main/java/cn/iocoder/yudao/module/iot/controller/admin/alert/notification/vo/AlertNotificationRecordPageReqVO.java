package cn.iocoder.yudao.module.iot.controller.admin.alert.notification.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - IoT 告警通知记录分页 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 告警通知记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AlertNotificationRecordPageReqVO extends PageParam {

    @Schema(description = "告警ID", example = "1")
    private Long alertId;

    @Schema(description = "通知类型", example = "email")
    private String notificationType;

    @Schema(description = "发送状态", example = "SUCCESS")
    private String sendStatus;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;
}





