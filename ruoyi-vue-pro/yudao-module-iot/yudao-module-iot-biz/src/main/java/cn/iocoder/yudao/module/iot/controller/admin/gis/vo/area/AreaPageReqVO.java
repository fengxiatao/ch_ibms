package cn.iocoder.yudao.module.iot.controller.admin.gis.vo.area;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 区域分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AreaPageReqVO extends PageParam {

    @Schema(description = "区域名称", example = "办公区A")
    private String name;

    @Schema(description = "区域编码", example = "AREA001")
    private String code;

    @Schema(description = "所属楼层ID", example = "1")
    private Long floorId;

    @Schema(description = "区域类型", example = "OFFICE")
    private String areaType;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}

