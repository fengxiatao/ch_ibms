package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 电子巡更路线 Response VO")
@Data
public class EpatrolRouteRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "路线名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "白班巡更路线")
    private String routeName;

    @Schema(description = "包含巡更点数量", example = "5")
    private Integer pointCount;

    @Schema(description = "路线总耗时（分钟）", example = "30")
    private Integer totalDuration;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "路线点位列表")
    private List<RoutePointRespVO> points;

    @Data
    @Schema(description = "路线点位 Response VO")
    public static class RoutePointRespVO {

        @Schema(description = "关联ID", example = "1")
        private Long id;

        @Schema(description = "点位ID", example = "1")
        private Long pointId;

        @Schema(description = "点位编号", example = "XGD001")
        private String pointNo;

        @Schema(description = "点位名称", example = "1号楼大厅")
        private String pointName;

        @Schema(description = "点位位置", example = "A区1楼")
        private String pointLocation;

        @Schema(description = "顺序", example = "1")
        private Integer sort;

        @Schema(description = "到下一个点位的间隔时间（分钟）", example = "5")
        private Integer intervalMinutes;

    }

}
