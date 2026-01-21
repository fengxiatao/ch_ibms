package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyvehicle;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 月租车分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ParkingMonthlyVehiclePageReqVO extends PageParam {

    @Schema(description = "车牌号", example = "京A12345")
    private String plateNumber;

    @Schema(description = "车主姓名", example = "张三")
    private String ownerName;

    @Schema(description = "车主电话", example = "13800138000")
    private String ownerPhone;

    @Schema(description = "车辆类型", example = "1")
    private Integer vehicleType;

    @Schema(description = "所属车场ID", example = "1")
    private Long lotId;

    @Schema(description = "状态：0-正常，1-停用，2-过期", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
