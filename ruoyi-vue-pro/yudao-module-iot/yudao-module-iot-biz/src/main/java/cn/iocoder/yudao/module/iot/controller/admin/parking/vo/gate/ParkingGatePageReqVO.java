package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.gate;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 道闸设备分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ParkingGatePageReqVO extends PageParam {

    @Schema(description = "道闸名称", example = "1号入口道闸")
    private String gateName;

    @Schema(description = "道闸编码", example = "GATE001")
    private String gateCode;

    @Schema(description = "所属车场ID", example = "1")
    private Long lotId;

    @Schema(description = "所属车道ID", example = "1")
    private Long laneId;

    @Schema(description = "道闸类型：1-车牌识别一体机，2-普通道闸", example = "1")
    private Integer gateType;

    @Schema(description = "方向：1-入口，2-出口，3-出入口", example = "1")
    private Integer direction;

    @Schema(description = "在线状态：0-离线，1-在线", example = "1")
    private Integer onlineStatus;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
