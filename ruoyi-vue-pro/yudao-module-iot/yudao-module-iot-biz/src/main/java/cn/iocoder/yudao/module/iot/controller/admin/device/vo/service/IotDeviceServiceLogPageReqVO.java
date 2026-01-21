package cn.iocoder.yudao.module.iot.controller.admin.device.vo.service;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * IoT 设备服务调用日志 - 分页查询 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备服务调用日志分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotDeviceServiceLogPageReqVO extends PageParam {

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "产品ID", example = "1")
    private Long productId;

    @Schema(description = "服务标识符", example = "Snapshot")
    private String serviceIdentifier;

    @Schema(description = "响应状态码", example = "200")
    private Integer statusCode;

    @Schema(description = "请求时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] requestTime;
}












