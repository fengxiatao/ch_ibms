package cn.iocoder.yudao.module.iot.controller.app.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.app.parking.vo.*;
import cn.iocoder.yudao.module.iot.service.parking.ParkingMiniAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 小程序 - 停车缴费 Controller
 * 
 * 提供停车费用查询、微信支付、道闸放行等接口
 */
@Tag(name = "小程序 - 停车缴费")
@RestController
@RequestMapping("/parking")
@Validated
@Slf4j
public class AppParkingController {

    @Resource
    private ParkingMiniAppService parkingMiniAppService;

    @GetMapping("/queryFee")
    @Operation(summary = "查询停车费用")
    public CommonResult<ParkingFeeRespVO> queryParkingFee(@Valid ParkingFeeQueryReqVO reqVO) {
        log.info("[queryParkingFee] 查询停车费用，车牌号：{}", reqVO.getPlateNumber());
        ParkingFeeRespVO respVO = parkingMiniAppService.queryParkingFee(reqVO);
        return success(respVO);
    }

    @PostMapping("/wechat/pay")
    @Operation(summary = "停车费微信支付")
    public CommonResult<ParkingPayRespVO> wechatPay(@Valid @RequestBody ParkingPayReqVO reqVO) {
        log.info("[wechatPay] 停车费微信支付，订单ID：{}，金额：{}", reqVO.getOrderId(), reqVO.getAmount());
        ParkingPayRespVO respVO = parkingMiniAppService.createWechatPayOrder(reqVO);
        return success(respVO);
    }

    @PostMapping("/gate/open")
    @Operation(summary = "通知道闸放行")
    public CommonResult<Boolean> notifyGateOpen(@Valid @RequestBody ParkingGateOpenReqVO reqVO) {
        log.info("[notifyGateOpen] 通知道闸放行，车牌号：{}", reqVO.getPlateNumber());
        parkingMiniAppService.notifyGateOpen(reqVO);
        return success(true);
    }

    @GetMapping("/order/list")
    @Operation(summary = "停车订单列表")
    public CommonResult<PageResult<ParkingFeeRespVO>> getOrderList(@Valid ParkingOrderListReqVO reqVO) {
        log.info("[getOrderList] 查询停车订单列表");
        PageResult<ParkingFeeRespVO> pageResult = parkingMiniAppService.getOrderList(reqVO);
        return success(pageResult);
    }

    @GetMapping("/order/detail")
    @Operation(summary = "停车订单详情")
    public CommonResult<ParkingFeeRespVO> getOrderDetail(@RequestParam("orderId") Long orderId) {
        log.info("[getOrderDetail] 查询停车订单详情，订单ID：{}", orderId);
        ParkingFeeRespVO respVO = parkingMiniAppService.getOrderDetail(orderId);
        return success(respVO);
    }

    @PostMapping("/wechat/notify")
    @Operation(summary = "微信支付回调通知")
    public String wechatPayNotify(@RequestBody String xmlData) {
        log.info("[wechatPayNotify] 收到微信支付回调");
        return parkingMiniAppService.handleWechatPayNotify(xmlData);
    }
}
