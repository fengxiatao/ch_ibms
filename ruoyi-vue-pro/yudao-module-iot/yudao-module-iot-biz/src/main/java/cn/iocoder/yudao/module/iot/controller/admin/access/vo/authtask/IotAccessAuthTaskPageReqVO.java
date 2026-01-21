package cn.iocoder.yudao.module.iot.controller.admin.access.vo.authtask;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 门禁授权任务分页 Request VO
 */
@Schema(description = "管理后台 - 门禁授权任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotAccessAuthTaskPageReqVO extends PageParam {

    @Schema(description = "任务类型（group_dispatch-权限组下发，person_dispatch-单人下发，revoke-权限撤销）", example = "group_dispatch")
    private String taskType;

    @Schema(description = "权限组ID", example = "1")
    private Long groupId;

    @Schema(description = "人员ID", example = "100")
    private Long personId;

    @Schema(description = "任务状态（0待执行 1执行中 2已完成 3部分失败 4全部失败）", example = "2")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
