package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.blacklist;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 停车场黑名单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ParkingBlacklistPageReqVO extends PageParam {

    @Schema(description = "车牌号", example = "京A12345")
    private String plateNumber;

    @Schema(description = "拉黑原因", example = "恶意逃费")
    private String reason;

    @Schema(description = "适用车场ID", example = "1")
    private Long lotId;

    @Schema(description = "状态：0-生效中，1-已解除", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
