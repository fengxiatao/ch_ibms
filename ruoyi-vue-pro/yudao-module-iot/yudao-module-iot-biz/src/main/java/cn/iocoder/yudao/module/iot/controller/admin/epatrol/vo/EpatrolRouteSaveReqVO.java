package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - 电子巡更路线新增/修改 Request VO")
@Data
public class EpatrolRouteSaveReqVO {

    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "路线名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "白班巡更路线")
    @NotBlank(message = "路线名称不能为空")
    private String routeName;

    @Schema(description = "路线点位列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "路线点位不能为空")
    private List<RoutePointItem> points;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Data
    @Schema(description = "路线点位项")
    public static class RoutePointItem {

        @Schema(description = "点位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long pointId;

        @Schema(description = "顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer sort;

        @Schema(description = "到下一个点位的间隔时间（分钟）", example = "5")
        private Integer intervalMinutes;

    }

}
