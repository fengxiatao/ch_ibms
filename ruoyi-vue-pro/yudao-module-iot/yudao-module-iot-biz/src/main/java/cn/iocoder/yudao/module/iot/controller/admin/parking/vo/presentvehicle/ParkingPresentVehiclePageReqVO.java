package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.presentvehicle;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 在场车辆分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ParkingPresentVehiclePageReqVO extends PageParam {

    @Schema(description = "车牌号", example = "京A12345")
    private String plateNumber;

    @Schema(description = "车辆类型", example = "1")
    private Integer vehicleType;

    @Schema(description = "车辆类别：free-免费车，monthly-月租车，temporary-临时车", example = "temporary")
    private String vehicleCategory;

    @Schema(description = "车场ID", example = "1")
    private Long lotId;

    @Schema(description = "长期停车标识：0-否，1-超一个月，2-超三个月", example = "0")
    private Integer longTermFlag;

    @Schema(description = "入场时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] entryTime;
}
