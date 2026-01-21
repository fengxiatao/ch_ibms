package cn.iocoder.yudao.module.iot.controller.app.parking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 停车费用 Response VO
 */
@Schema(description = "小程序 - 停车费用 Response VO")
@Data
public class ParkingFeeRespVO {

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "停车场名称", example = "曲靖市小方村仟喜缘酒店")
    private String parkingLotName;

    @Schema(description = "车牌号", example = "云D3B947")
    private String plateNumber;

    @Schema(description = "入场时间", example = "2024-12-18 13:36:06")
    private LocalDateTime entryTime;

    @Schema(description = "查询时间", example = "2024-12-18 13:46:03")
    private LocalDateTime queryTime;

    @Schema(description = "停车时长（分钟）", example = "10")
    private Integer duration;

    @Schema(description = "停车时长（格式化显示）", example = "10分钟")
    private String durationText;

    @Schema(description = "应收金额", example = "3.00")
    private BigDecimal amount;

    @Schema(description = "入场照片URL", example = "http://xxx/photo.jpg")
    private String photoUrl;

    @Schema(description = "支付状态：0-未支付，1-已支付，2-免费", example = "0")
    private Integer paymentStatus;

    @Schema(description = "车辆类型：free-免费车，monthly-月租车，temporary-临时车", example = "temporary")
    private String vehicleCategory;

    @Schema(description = "车辆类型名称", example = "临时车")
    private String vehicleCategoryName;

    @Schema(description = "收费规则名称", example = "临时车标准收费")
    private String chargeRuleName;

    @Schema(description = "月租到期时间（月租车有效）")
    private LocalDateTime monthlyExpireTime;
}
