package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.module.iot.controller.app.parking.vo.ParkingPayRespVO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 停车场微信支付 Service 接口
 */
public interface ParkingWechatPayService {

    /**
     * 创建统一下单
     *
     * @param code 微信授权码
     * @param outTradeNo 商户订单号
     * @param body 商品描述
     * @param amount 支付金额（元）
     * @param plateNumber 车牌号
     * @return 支付参数
     */
    ParkingPayRespVO createUnifiedOrder(String code, String outTradeNo, String body, 
                                        BigDecimal amount, String plateNumber);

    /**
     * 解析微信支付回调数据
     *
     * @param xmlData XML格式的回调数据
     * @return 解析后的Map
     */
    Map<String, String> parseNotifyData(String xmlData);

    /**
     * 验证签名
     *
     * @param data 回调数据
     * @return 是否验证通过
     */
    boolean verifySign(Map<String, String> data);

    /**
     * 申请退款
     *
     * @param outTradeNo 原商户订单号
     * @param outRefundNo 商户退款单号
     * @param totalFee 订单总金额（元）
     * @param refundFee 退款金额（元）
     * @param refundReason 退款原因
     * @return 退款结果，包含微信退款单号等
     */
    Map<String, String> refund(String outTradeNo, String outRefundNo, 
                               BigDecimal totalFee, BigDecimal refundFee, String refundReason);

    /**
     * 查询退款状态
     *
     * @param outRefundNo 商户退款单号
     * @return 退款状态信息
     */
    Map<String, String> queryRefund(String outRefundNo);
}
