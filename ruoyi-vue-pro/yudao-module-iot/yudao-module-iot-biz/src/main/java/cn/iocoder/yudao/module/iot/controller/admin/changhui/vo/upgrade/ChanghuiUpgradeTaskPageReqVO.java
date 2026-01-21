package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 长辉升级任务分页查询 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉升级任务分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ChanghuiUpgradeTaskPageReqVO extends PageParam {

    @Schema(description = "测站编码", example = "1234567890")
    private String stationCode;

    @Schema(description = "固件ID", example = "1")
    private Long firmwareId;

    @Schema(description = "状态：0-待执行,1-进行中,2-成功,3-失败,4-已取消,5-已拒绝", example = "1")
    private Integer status;

    @Schema(description = "创建时间开始")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTimeStart;

    @Schema(description = "创建时间结束")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTimeEnd;

}
