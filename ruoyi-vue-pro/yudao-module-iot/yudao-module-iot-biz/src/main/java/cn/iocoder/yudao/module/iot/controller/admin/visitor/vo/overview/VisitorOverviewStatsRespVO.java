package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.overview;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 新访客管理统计卡片 Response VO")
@Data
public class VisitorOverviewStatsRespVO {

    @Schema(description = "当前在访人数")
    private Long currentVisitors;

    @Schema(description = "较昨日涨幅（百分比整数）")
    private Integer visitTrend;

    @Schema(description = "今日预约人数")
    private Long todayAppointments;

    @Schema(description = "待确认数量（预留）")
    private Long pendingConfirm;

    @Schema(description = "待审批数量")
    private Long pendingApproval;

    @Schema(description = "异常预警数量")
    private Long abnormalCount;

    @Schema(description = "本月累计访客（按预约单统计）")
    private Long monthlyTotal;

    @Schema(description = "今日已处理数量（已通过+已拒绝）")
    private Long todayProcessedCount;
}

