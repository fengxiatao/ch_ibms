package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.overview;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 访客仪表盘（图表/排行）Response VO")
@Data
public class VisitorDashboardRespVO {

    @Schema(description = "被访人排行：{ name, dept, count, percent }")
    private List<HostRankItem> hostRank;

    @Schema(description = "来访事由分布：{ name, value }")
    private List<Map<String, Object>> reasonDistribution;

    @Schema(description = "访客趋势（近7天）：{ date, count }")
    private List<Map<String, Object>> trend;

    @Schema(description = "异常事件分布：{ name, value }")
    private List<Map<String, Object>> abnormalDistribution;

    @Schema(description = "来访时段分布：{ name, value }")
    private List<Map<String, Object>> timeDistribution;

    @Data
    public static class HostRankItem {
        private String name;
        private String dept;
        private Long count;
        private String percent;
    }
}
