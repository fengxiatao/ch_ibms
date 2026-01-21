package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargeruleapply;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 收费规则应用分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ParkingChargeRuleApplyPageReqVO extends PageParam {

    @Schema(description = "应用名称", example = "临时车收费")
    private String applyName;

    @Schema(description = "关联的收费规则ID", example = "1")
    private Long ruleId;

    @Schema(description = "是否启用：0-关闭，1-启用", example = "1")
    private Integer enabled;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
