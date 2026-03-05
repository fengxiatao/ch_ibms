package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 在场车辆统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingPresentStatisticsRespVO {

    @Schema(description = "在场车辆总数", example = "100")
    private Long totalCount;

    @Schema(description = "临时车数量", example = "60")
    private Long temporaryCount;

    @Schema(description = "月租车数量", example = "30")
    private Long monthlyCount;

    @Schema(description = "免费车数量", example = "10")
    private Long freeCount;

    @Schema(description = "总车位数", example = "200")
    private Integer totalSpaces;

    @Schema(description = "剩余车位数", example = "100")
    private Integer availableSpaces;

    @Schema(description = "车位使用率(%)", example = "50.0")
    private Double occupancyRate;

    @Schema(description = "长停车辆数量（超过24小时）", example = "5")
    private Long longTermCount;
}
