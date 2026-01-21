package cn.iocoder.yudao.module.iot.service.parking;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.refund.ParkingRefundRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.refund.ParkingRefundReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRefundRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingRecordMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingRefundRecordMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 停车退款 Service 实现类
 */
@Service
@Validated
@Slf4j
public class ParkingRefundServiceImpl implements ParkingRefundService {

    @Resource
    private ParkingRefundRecordMapper refundRecordMapper;

    @Resource
    private ParkingRecordMapper parkingRecordMapper;

    @Resource
    private ParkingWechatPayService parkingWechatPayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyRefund(ParkingRefundReqVO reqVO, String applyUser) {
        // 1. 查询停车记录
        ParkingRecordDO record = parkingRecordMapper.selectById(reqVO.getRecordId());
        if (record == null) {
            throw exception(PARKING_RECORD_NOT_EXISTS);
        }

        // 2. 检查是否已支付
        if (record.getPaymentStatus() == null || record.getPaymentStatus() != 1) {
            throw exception(WECHAT_PAY_ORDER_NOT_EXISTS);
        }

        // 3. 检查是否已申请过退款
        ParkingRefundRecordDO existRefund = refundRecordMapper.selectByRecordId(reqVO.getRecordId());
        if (existRefund != null && existRefund.getRefundStatus() != 2 && existRefund.getRefundStatus() != 3) {
            throw exception(WECHAT_REFUND_ALREADY_PROCESSED);
        }

        // 4. 确定退款金额
        BigDecimal totalFee = record.getPaidAmount();
        BigDecimal refundFee = reqVO.getRefundFee();
        if (refundFee == null || refundFee.compareTo(BigDecimal.ZERO) <= 0) {
            refundFee = totalFee; // 全额退款
        }
        if (refundFee.compareTo(totalFee) > 0) {
            throw exception(WECHAT_REFUND_AMOUNT_EXCEED);
        }

        // 5. 生成退款单号
        String outRefundNo = "REF" + System.currentTimeMillis() + IdUtil.fastSimpleUUID().substring(0, 6);

        // 6. 创建退款记录
        ParkingRefundRecordDO refundRecord = ParkingRefundRecordDO.builder()
                .recordId(reqVO.getRecordId())
                .plateNumber(record.getPlateNumber())
                .outTradeNo(record.getRemark()) // 假设原订单号存在remark字段，实际需要调整
                .outRefundNo(outRefundNo)
                .totalFee(totalFee)
                .refundFee(refundFee)
                .refundReason(reqVO.getRefundReason())
                .refundStatus(0) // 申请中
                .applyTime(LocalDateTime.now())
                .applyUser(applyUser)
                .remark(reqVO.getRemark())
                .build();
        refundRecordMapper.insert(refundRecord);

        log.info("[applyRefund] 退款申请已创建，退款单号：{}，车牌号：{}，退款金额：{}", 
                outRefundNo, record.getPlateNumber(), refundFee);
        return refundRecord.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeRefund(Long refundId, String auditUser) {
        // 1. 查询退款记录
        ParkingRefundRecordDO refundRecord = refundRecordMapper.selectById(refundId);
        if (refundRecord == null) {
            throw exception(WECHAT_REFUND_NOT_EXISTS);
        }

        // 2. 检查状态
        if (refundRecord.getRefundStatus() != 0) {
            throw exception(WECHAT_REFUND_ALREADY_PROCESSED);
        }

        try {
            // 3. 调用微信退款接口
            Map<String, String> result = parkingWechatPayService.refund(
                    refundRecord.getOutTradeNo(),
                    refundRecord.getOutRefundNo(),
                    refundRecord.getTotalFee(),
                    refundRecord.getRefundFee(),
                    refundRecord.getRefundReason()
            );

            // 4. 更新退款记录
            refundRecord.setRefundId(result.get("refund_id"));
            refundRecord.setRefundStatus(1); // 退款成功
            refundRecord.setRefundTime(LocalDateTime.now());
            refundRecord.setAuditUser(auditUser);
            refundRecordMapper.updateById(refundRecord);

            // 5. 更新原停车记录的支付状态
            ParkingRecordDO record = parkingRecordMapper.selectById(refundRecord.getRecordId());
            if (record != null) {
                record.setPaymentStatus(3); // 已退款
                parkingRecordMapper.updateById(record);
            }

            log.info("[executeRefund] 退款成功，退款单号：{}，微信退款单号：{}", 
                    refundRecord.getOutRefundNo(), result.get("refund_id"));

        } catch (Exception e) {
            // 退款失败
            refundRecord.setRefundStatus(2);
            refundRecord.setFailReason(e.getMessage());
            refundRecord.setAuditUser(auditUser);
            refundRecordMapper.updateById(refundRecord);
            
            log.error("[executeRefund] 退款失败，退款单号：{}", refundRecord.getOutRefundNo(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeRefund(Long refundId, String reason) {
        ParkingRefundRecordDO refundRecord = refundRecordMapper.selectById(refundId);
        if (refundRecord == null) {
            throw exception(WECHAT_REFUND_NOT_EXISTS);
        }

        if (refundRecord.getRefundStatus() != 0) {
            throw exception(WECHAT_REFUND_ALREADY_PROCESSED);
        }

        refundRecord.setRefundStatus(3); // 退款关闭
        refundRecord.setFailReason(reason);
        refundRecordMapper.updateById(refundRecord);

        log.info("[closeRefund] 退款已关闭，退款单号：{}，原因：{}", refundRecord.getOutRefundNo(), reason);
    }

    @Override
    public void syncRefundStatus(Long refundId) {
        ParkingRefundRecordDO refundRecord = refundRecordMapper.selectById(refundId);
        if (refundRecord == null) {
            throw exception(WECHAT_REFUND_NOT_EXISTS);
        }

        // 查询微信退款状态
        Map<String, String> result = parkingWechatPayService.queryRefund(refundRecord.getOutRefundNo());
        String refundStatus = result.get("refund_status_0");

        if ("SUCCESS".equals(refundStatus)) {
            refundRecord.setRefundStatus(1);
            refundRecord.setRefundTime(LocalDateTime.now());
        } else if ("REFUNDCLOSE".equals(refundStatus)) {
            refundRecord.setRefundStatus(3);
        } else if ("CHANGE".equals(refundStatus)) {
            refundRecord.setRefundStatus(2);
            refundRecord.setFailReason("退款异常，需人工处理");
        }

        refundRecordMapper.updateById(refundRecord);
        log.info("[syncRefundStatus] 同步退款状态，退款单号：{}，状态：{}", 
                refundRecord.getOutRefundNo(), refundStatus);
    }

    @Override
    public String handleRefundNotify(String xmlData) {
        log.info("[handleRefundNotify] 收到退款回调：{}", xmlData);

        try {
            Map<String, String> notifyData = parkingWechatPayService.parseNotifyData(xmlData);
            
            String returnCode = notifyData.get("return_code");
            if (!"SUCCESS".equals(returnCode)) {
                log.error("[handleRefundNotify] 退款回调失败");
                return buildFailResponse("退款失败");
            }

            // 解密退款通知数据（实际生产环境需要解密req_info字段）
            String outRefundNo = notifyData.get("out_refund_no");
            String refundStatus = notifyData.get("refund_status");

            if (StrUtil.isNotBlank(outRefundNo)) {
                ParkingRefundRecordDO refundRecord = refundRecordMapper.selectByOutRefundNo(outRefundNo);
                if (refundRecord != null) {
                    if ("SUCCESS".equals(refundStatus)) {
                        refundRecord.setRefundStatus(1);
                        refundRecord.setRefundTime(LocalDateTime.now());
                    } else {
                        refundRecord.setRefundStatus(2);
                        refundRecord.setFailReason(refundStatus);
                    }
                    refundRecordMapper.updateById(refundRecord);
                }
            }

            return buildSuccessResponse();

        } catch (Exception e) {
            log.error("[handleRefundNotify] 处理退款回调异常：", e);
            return buildFailResponse(e.getMessage());
        }
    }

    @Override
    public PageResult<ParkingRefundRecordDO> getRefundRecordPage(ParkingRefundRecordPageReqVO pageReqVO) {
        return refundRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public ParkingRefundRecordDO getRefundRecord(Long id) {
        return refundRecordMapper.selectById(id);
    }

    private String buildSuccessResponse() {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    private String buildFailResponse(String msg) {
        return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[" + msg + "]]></return_msg></xml>";
    }
}
