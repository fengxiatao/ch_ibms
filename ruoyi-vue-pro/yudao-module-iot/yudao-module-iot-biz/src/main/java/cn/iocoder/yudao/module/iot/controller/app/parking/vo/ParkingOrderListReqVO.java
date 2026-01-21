package cn.iocoder.yudao.module.iot.controller.app.parking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 停车订单列表查询 Request VO
 */
@Schema(description = "小程序 - 停车订单列表查询 Request VO")
@Data
public class ParkingOrderListReqVO {

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "支付状态：0-未支付，1-已支付", example = "1")
    private Integer status;

    @Schema(description = "车牌号", example = "粤A12345")
    private String plateNumber;
}
