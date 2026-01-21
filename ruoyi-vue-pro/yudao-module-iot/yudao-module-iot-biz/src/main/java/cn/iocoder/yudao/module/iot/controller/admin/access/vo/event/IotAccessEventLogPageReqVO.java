package cn.iocoder.yudao.module.iot.controller.admin.access.vo.event;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 门禁事件日志分页 Request VO
 */
@Schema(description = "管理后台 - 门禁事件日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotAccessEventLogPageReqVO extends PageParam {

    @Schema(description = "设备ID", example = "110")
    private Long deviceId;

    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    @Schema(description = "事件类别（NORMAL/ALARM/ABNORMAL）", example = "NORMAL")
    private String eventCategory;

    @Schema(description = "事件类型（字符串代码，如 CARD_SWIPE）", example = "CARD_SWIPE")
    private String eventType;

    @Schema(description = "人员ID", example = "1")
    private Long personId;

    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    @Schema(description = "验证结果（0失败 1成功）", example = "1")
    private Integer verifyResult;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}
