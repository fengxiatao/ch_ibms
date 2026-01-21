package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.record;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 停车进出记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ParkingRecordPageReqVO extends PageParam {

    @Schema(description = "车牌号", example = "京A12345")
    private String plateNumber;

    @Schema(description = "车辆类型", example = "1")
    private Integer vehicleType;

    @Schema(description = "车辆类别：free-免费车，monthly-月租车，temporary-临时车", example = "temporary")
    private String vehicleCategory;

    @Schema(description = "车场ID", example = "1")
    private Long lotId;

    @Schema(description = "支付状态：0-未支付，1-已支付，2-免费", example = "1")
    private Integer paymentStatus;

    @Schema(description = "记录状态：1-在场，2-已出场", example = "2")
    private Integer recordStatus;

    @Schema(description = "入场时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] entryTime;

    @Schema(description = "出场时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] exitTime;
}
