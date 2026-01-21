package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 车道分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ParkingLanePageReqVO extends PageParam {

    @Schema(description = "车道名称", example = "1号车道")
    private String laneName;

    @Schema(description = "车道编码", example = "LANE001")
    private String laneCode;

    @Schema(description = "所属车场ID", example = "1")
    private Long lotId;

    @Schema(description = "出入口配置：1-入口，2-出口，3-出入口", example = "1")
    private Integer direction;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
