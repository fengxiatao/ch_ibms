package cn.iocoder.yudao.module.iot.controller.admin.access.vo.management;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 授权记录分页查询请求 VO
 * 
 * Requirements: 7.1
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 授权记录分页查询请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AuthRecordPageReqVO extends PageParam {

    @Schema(description = "人员ID", example = "100")
    private Long personId;

    @Schema(description = "人员姓名（模糊匹配）", example = "张三")
    private String personName;

    @Schema(description = "设备ID", example = "200")
    private Long deviceId;

    @Schema(description = "设备名称（模糊匹配）", example = "一号门")
    private String deviceName;

    @Schema(description = "通道ID", example = "300")
    private Long channelId;

    @Schema(description = "授权状态: 0-未授权, 1-已授权, 2-授权中, 3-授权失败, 4-待撤销, 5-撤销中", example = "1")
    private Integer authStatus;

    @Schema(description = "创建时间-开始")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTimeStart;

    @Schema(description = "创建时间-结束")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTimeEnd;

}
