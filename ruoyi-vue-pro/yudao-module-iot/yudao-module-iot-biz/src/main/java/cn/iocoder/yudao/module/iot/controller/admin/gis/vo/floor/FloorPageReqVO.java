package cn.iocoder.yudao.module.iot.controller.admin.gis.vo.floor;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 楼层分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FloorPageReqVO extends PageParam {

    @Schema(description = "楼层名称", example = "1层")
    private String name;

    @Schema(description = "楼层编码", example = "FLOOR001")
    private String code;

    @Schema(description = "所属建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "楼层号", example = "1")
    private Integer floorNumber;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}

