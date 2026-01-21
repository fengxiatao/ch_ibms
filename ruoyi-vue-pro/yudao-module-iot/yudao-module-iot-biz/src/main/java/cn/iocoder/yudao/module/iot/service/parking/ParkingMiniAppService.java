package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.app.parking.vo.*;

/**
 * 停车小程序 Service 接口
 *
 * 提供停车费用查询、微信支付、道闸放行等服务
 */
public interface ParkingMiniAppService {

    /**
     * 查询停车费用
     *
     * @param reqVO 查询请求
     * @return 停车费用信息
     */
    ParkingFeeRespVO queryParkingFee(ParkingFeeQueryReqVO reqVO);

    /**
     * 创建微信支付订单
     *
     * @param reqVO 支付请求
     * @return 微信支付参数
     */
    ParkingPayRespVO createWechatPayOrder(ParkingPayReqVO reqVO);

    /**
     * 通知道闸放行
     *
     * @param reqVO 放行请求
     */
    void notifyGateOpen(ParkingGateOpenReqVO reqVO);

    /**
     * 获取停车订单列表
     *
     * @param reqVO 查询请求
     * @return 订单列表
     */
    PageResult<ParkingFeeRespVO> getOrderList(ParkingOrderListReqVO reqVO);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    ParkingFeeRespVO getOrderDetail(Long orderId);

    /**
     * 处理微信支付回调通知
     *
     * @param xmlData 微信回调数据（XML格式）
     * @return 响应给微信的结果
     */
    String handleWechatPayNotify(String xmlData);
}
