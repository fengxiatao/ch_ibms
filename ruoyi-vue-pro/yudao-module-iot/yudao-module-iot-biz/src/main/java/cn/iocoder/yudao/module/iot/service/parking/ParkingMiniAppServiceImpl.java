package cn.iocoder.yudao.module.iot.service.parking;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.app.parking.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingLotDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingPresentVehicleDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingLotMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingPresentVehicleMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 停车小程序 Service 实现类
 */
@Service
@Slf4j
public class ParkingMiniAppServiceImpl implements ParkingMiniAppService {

    @Resource
    private ParkingRecordMapper parkingRecordMapper;

    @Resource
    private ParkingPresentVehicleMapper parkingPresentVehicleMapper;

    @Resource
    private ParkingLotMapper parkingLotMapper;

    @Resource
    private ParkingRecordService parkingRecordService;

    @Resource
    private ParkingWechatPayService parkingWechatPayService;

    @Resource
    private ParkingGateControlService parkingGateControlService;

    @Override
    public ParkingFeeRespVO queryParkingFee(ParkingFeeQueryReqVO reqVO) {
        // 1. 查询在场车辆
        LambdaQueryWrapper<ParkingPresentVehicleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingPresentVehicleDO::getPlateNumber, reqVO.getPlateNumber());
        if (reqVO.getParkingLotId() != null) {
            queryWrapper.eq(ParkingPresentVehicleDO::getLotId, reqVO.getParkingLotId());
        }
        queryWrapper.orderByDesc(ParkingPresentVehicleDO::getEntryTime);
        queryWrapper.last("LIMIT 1");

        ParkingPresentVehicleDO presentVehicle = parkingPresentVehicleMapper.selectOne(queryWrapper);
        if (presentVehicle == null) {
            // 没有在场车辆记录
            return null;
        }

        // 2. 获取停车场信息
        ParkingLotDO parkingLot = parkingLotMapper.selectById(presentVehicle.getLotId());
        String parkingLotName = parkingLot != null ? parkingLot.getLotName() : "停车场";

        // 3. 计算停车时长
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime entryTime = presentVehicle.getEntryTime();
        Duration duration = Duration.between(entryTime, now);
        int minutes = (int) duration.toMinutes();

        // 4. 获取车辆类型
        String vehicleCategory = parkingRecordService.getVehicleCategory(reqVO.getPlateNumber());
        String vehicleCategoryName = getVehicleCategoryName(vehicleCategory);

        // 5. 获取收费车型（从在场车辆记录中获取）
        Integer vehicleType = presentVehicle.getVehicleType();

        // 6. 计算停车费用（传入车型以匹配对应收费规则）
        BigDecimal amount = parkingRecordService.calculateParkingFee(
                reqVO.getPlateNumber(),
                presentVehicle.getLotId(),
                vehicleType
        );

        // 7. 获取收费规则名称
        String chargeRuleName = parkingRecordService.getChargeRuleName(presentVehicle.getLotId(), vehicleCategory, vehicleType);

        // 8. 构建响应
        ParkingFeeRespVO respVO = new ParkingFeeRespVO();
        respVO.setOrderId(presentVehicle.getId());
        respVO.setParkingLotName(parkingLotName);
        respVO.setPlateNumber(reqVO.getPlateNumber());
        respVO.setEntryTime(entryTime);
        respVO.setQueryTime(now);
        respVO.setDuration(minutes);
        respVO.setDurationText(formatDuration(minutes));
        respVO.setAmount(amount);
        respVO.setPhotoUrl(presentVehicle.getEntryPhotoUrl());
        
        // 9. 设置车辆类型信息
        respVO.setVehicleCategory(vehicleCategory);
        respVO.setVehicleCategoryName(vehicleCategoryName);
        respVO.setChargeRuleName(chargeRuleName);
        
        // 10. 设置支付状态
        if ("free".equals(vehicleCategory)) {
            respVO.setPaymentStatus(2); // 免费车
        } else if ("monthly".equals(vehicleCategory)) {
            respVO.setPaymentStatus(2); // 月租车（有效期内免费）
        } else {
            respVO.setPaymentStatus(amount.compareTo(BigDecimal.ZERO) > 0 ? 0 : 2);
        }

        return respVO;
    }

    /**
     * 获取车辆类型名称
     */
    private String getVehicleCategoryName(String category) {
        if ("free".equals(category)) {
            return "免费车";
        } else if ("monthly".equals(category)) {
            return "月租车";
        } else {
            return "临时车";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingPayRespVO createWechatPayOrder(ParkingPayReqVO reqVO) {
        // 1. 验证订单存在
        ParkingPresentVehicleDO presentVehicle = parkingPresentVehicleMapper.selectById(reqVO.getOrderId());
        if (presentVehicle == null) {
            throw exception(PARKING_RECORD_NOT_EXISTS);
        }

        // 2. 生成订单号
        String outTradeNo = "PKG" + System.currentTimeMillis() + IdUtil.fastSimpleUUID().substring(0, 6);

        // 3. 调用微信支付服务创建订单
        ParkingPayRespVO respVO = parkingWechatPayService.createUnifiedOrder(
                reqVO.getCode(),
                outTradeNo,
                "停车费用",
                reqVO.getAmount(),
                reqVO.getPlateNumber()
        );

        respVO.setOrderId(outTradeNo);
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyGateOpen(ParkingGateOpenReqVO reqVO) {
        log.info("[notifyGateOpen] 通知道闸放行，车牌号：{}", reqVO.getPlateNumber());

        // 1. 查询在场车辆获取出口道闸信息
        LambdaQueryWrapper<ParkingPresentVehicleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingPresentVehicleDO::getPlateNumber, reqVO.getPlateNumber());
        queryWrapper.orderByDesc(ParkingPresentVehicleDO::getEntryTime);
        queryWrapper.last("LIMIT 1");

        ParkingPresentVehicleDO presentVehicle = parkingPresentVehicleMapper.selectOne(queryWrapper);
        if (presentVehicle == null) {
            log.warn("[notifyGateOpen] 未找到在场车辆，车牌号：{}", reqVO.getPlateNumber());
            return;
        }

        // 2. 发送道闸放行指令
        parkingGateControlService.openGate(presentVehicle.getLotId(), reqVO.getPlateNumber());

        log.info("[notifyGateOpen] 道闸放行指令已发送，车牌号：{}", reqVO.getPlateNumber());
    }

    @Override
    public PageResult<ParkingFeeRespVO> getOrderList(ParkingOrderListReqVO reqVO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<ParkingRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        if (reqVO.getStatus() != null) {
            queryWrapper.eq(ParkingRecordDO::getPaymentStatus, reqVO.getStatus());
        }
        if (reqVO.getPlateNumber() != null) {
            queryWrapper.eq(ParkingRecordDO::getPlateNumber, reqVO.getPlateNumber());
        }
        queryWrapper.orderByDesc(ParkingRecordDO::getEntryTime);

        // 2. 分页查询
        Page<ParkingRecordDO> page = new Page<>(reqVO.getPage(), reqVO.getPageSize());
        IPage<ParkingRecordDO> recordPage = parkingRecordMapper.selectPage(page, queryWrapper);

        // 3. 转换为响应VO
        List<ParkingFeeRespVO> list = new ArrayList<>();
        for (ParkingRecordDO record : recordPage.getRecords()) {
            ParkingFeeRespVO respVO = convertToRespVO(record);
            list.add(respVO);
        }

        return new PageResult<>(list, recordPage.getTotal());
    }

    @Override
    public ParkingFeeRespVO getOrderDetail(Long orderId) {
        ParkingRecordDO record = parkingRecordMapper.selectById(orderId);
        if (record == null) {
            throw exception(PARKING_RECORD_NOT_EXISTS);
        }
        return convertToRespVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleWechatPayNotify(String xmlData) {
        log.info("[handleWechatPayNotify] 收到微信支付回调：{}", xmlData);

        try {
            // 1. 解析回调数据
            Map<String, String> notifyData = parkingWechatPayService.parseNotifyData(xmlData);

            // 2. 验证签名
            if (!parkingWechatPayService.verifySign(notifyData)) {
                log.error("[handleWechatPayNotify] 签名验证失败");
                return buildFailResponse("签名验证失败");
            }

            // 3. 检查返回状态
            String returnCode = notifyData.get("return_code");
            String resultCode = notifyData.get("result_code");
            if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
                log.error("[handleWechatPayNotify] 支付失败：return_code={}, result_code={}", returnCode, resultCode);
                return buildFailResponse("支付失败");
            }

            // 4. 获取订单信息
            String outTradeNo = notifyData.get("out_trade_no");
            String transactionId = notifyData.get("transaction_id");
            int totalFee = Integer.parseInt(notifyData.get("total_fee"));
            BigDecimal paidAmount = new BigDecimal(totalFee).divide(new BigDecimal(100));

            log.info("[handleWechatPayNotify] 支付成功，订单号：{}，交易号：{}，金额：{}元", 
                    outTradeNo, transactionId, paidAmount);

            // 5. 更新订单状态
            // 这里需要根据实际业务逻辑更新停车记录的支付状态
            // parkingRecordMapper.updatePaymentStatus(...)

            // 6. 通知道闸放行
            // 支付成功后自动通知道闸放行
            // parkingGateControlService.openGate(...)

            return buildSuccessResponse();

        } catch (Exception e) {
            log.error("[handleWechatPayNotify] 处理支付回调异常：", e);
            return buildFailResponse(e.getMessage());
        }
    }

    /**
     * 格式化停车时长
     */
    private String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + "分钟";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) {
            return hours + "小时";
        }
        return hours + "小时" + mins + "分钟";
    }

    /**
     * 转换为响应VO
     */
    private ParkingFeeRespVO convertToRespVO(ParkingRecordDO record) {
        ParkingFeeRespVO respVO = new ParkingFeeRespVO();
        respVO.setOrderId(record.getId());
        respVO.setPlateNumber(record.getPlateNumber());
        respVO.setEntryTime(record.getEntryTime());
        respVO.setDuration(record.getParkingDuration());
        respVO.setDurationText(formatDuration(record.getParkingDuration() != null ? record.getParkingDuration() : 0));
        respVO.setAmount(record.getChargeAmount());
        respVO.setPhotoUrl(record.getEntryPhotoUrl());
        respVO.setPaymentStatus(record.getPaymentStatus());

        // 获取停车场名称
        ParkingLotDO parkingLot = parkingLotMapper.selectById(record.getLotId());
        respVO.setParkingLotName(parkingLot != null ? parkingLot.getLotName() : "停车场");

        return respVO;
    }

    /**
     * 构建成功响应
     */
    private String buildSuccessResponse() {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    /**
     * 构建失败响应
     */
    private String buildFailResponse(String msg) {
        return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[" + msg + "]]></return_msg></xml>";
    }
}
