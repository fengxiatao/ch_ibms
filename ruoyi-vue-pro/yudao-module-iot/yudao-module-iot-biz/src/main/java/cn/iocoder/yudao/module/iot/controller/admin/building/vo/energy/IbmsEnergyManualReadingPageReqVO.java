package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 人工抄表记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IbmsEnergyManualReadingPageReqVO extends PageParam {

    @Schema(description = "仪表ID")
    private Long meterId;

    @Schema(description = "仪表名称")
    private String meterName;

    @Schema(description = "抄表日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate readingDate;

    @Schema(description = "抄表人")
    private String reader;

    @Schema(description = "状态：0-待复核 1-已确认 2-已作废")
    private Integer status;

}
