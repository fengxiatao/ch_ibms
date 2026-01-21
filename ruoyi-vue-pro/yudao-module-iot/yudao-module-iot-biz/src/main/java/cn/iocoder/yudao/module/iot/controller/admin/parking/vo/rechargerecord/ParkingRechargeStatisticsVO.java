package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.rechargerecord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 月卡充值统计 Response VO")
@Data
public class ParkingRechargeStatisticsVO {

    @Schema(description = "今日充值笔数", example = "10")
    private Long todayCount;

    @Schema(description = "今日充值金额", example = "3000.00")
    private BigDecimal todayAmount;

    @Schema(description = "本月充值笔数", example = "150")
    private Long monthCount;

    @Schema(description = "本月充值金额", example = "45000.00")
    private BigDecimal monthAmount;

    @Schema(description = "累计充值笔数", example = "1200")
    private Long totalCount;

    @Schema(description = "累计充值金额", example = "360000.00")
    private BigDecimal totalAmount;
}
