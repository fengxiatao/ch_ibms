package cn.iocoder.yudao.module.iot.controller.app.parking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 停车费微信支付参数 Response VO
 */
@Schema(description = "小程序 - 停车费微信支付参数 Response VO")
@Data
public class ParkingPayRespVO {

    @Schema(description = "时间戳", example = "1609459200")
    private String timeStamp;

    @Schema(description = "随机字符串", example = "5K8264ILTKCH16CQ2502SI8ZNMTM67VS")
    private String nonceStr;

    @Schema(description = "统一下单接口返回的 prepay_id 参数值", example = "prepay_id=wx201410272009395522657a690389285100")
    private String packageValue;

    @Schema(description = "签名类型", example = "MD5")
    private String signType;

    @Schema(description = "签名", example = "C380BEC2BFD727A4B6845133519F3AD6")
    private String paySign;

    @Schema(description = "订单号", example = "PKG202401140001")
    private String orderId;
}
