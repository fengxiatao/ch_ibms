package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 停车记录 手工缴费请求 VO")
@Data
public class ParkingRecordManualPayReqVO {

    @Schema(description = "停车场 ID", example = "1")
    private Long lotId;

    @Schema(description = "车牌号", example = "粤B12345")
    @NotBlank(message = "车牌号不能为空")
    private String plateNo;

    @Schema(description = "车辆类型")
    private String vehicleType;

    @Schema(description = "入场时间", example = "2026-03-02 10:00:00")
    @NotBlank(message = "入场时间不能为空")
    private String inTime;

    @Schema(description = "出场时间", example = "2026-03-02 12:00:00")
    @NotBlank(message = "出场时间不能为空")
    private String outTime;

    @Schema(description = "停车时长（分钟）", example = "120")
    private Integer durationMinutes;

    @Schema(description = "应收金额", example = "20.00")
    @NotNull(message = "缴费金额不能为空")
    private Double amount;

    @Schema(description = "优惠类型")
    private String discountType;

    @Schema(description = "折扣系数", example = "0.8")
    private Double discountRate;

    @Schema(description = "支付渠道", example = "cash")
    private String payChannel;

    @Schema(description = "备注")
    private String remark;
}

