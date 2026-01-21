package cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 建筑分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BuildingPageReqVO extends PageParam {

    @Schema(description = "建筑名称", example = "A栋")
    private String name;

    @Schema(description = "建筑编码", example = "BUILD001")
    private String code;

    @Schema(description = "所属园区ID", example = "1")
    private Long campusId;

    @Schema(description = "建筑类型", example = "OFFICE")
    private String buildingType;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}

