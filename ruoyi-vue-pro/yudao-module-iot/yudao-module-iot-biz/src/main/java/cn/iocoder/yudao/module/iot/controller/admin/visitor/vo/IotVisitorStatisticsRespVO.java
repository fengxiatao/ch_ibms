package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 访客统计 Response VO
 */
@Schema(description = "管理后台 - 访客统计 Response VO")
@Data
@Builder
public class IotVisitorStatisticsRespVO {

    @Schema(description = "当前在访人数", example = "5")
    private Long currentVisitingCount;

    @Schema(description = "当日访客人数", example = "20")
    private Long todayVisitorCount;

    @Schema(description = "历史访客总数", example = "1000")
    private Long totalVisitorCount;

    @Schema(description = "待审批申请数", example = "3")
    private Long pendingApproveCount;

    @Schema(description = "待下发权限数", example = "2")
    private Long pendingDispatchCount;

}
