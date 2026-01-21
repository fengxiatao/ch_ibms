package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.refund.ParkingRefundRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.refund.ParkingRefundReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRefundRecordDO;

/**
 * 停车退款 Service 接口
 */
public interface ParkingRefundService {

    /**
     * 申请退款
     *
     * @param reqVO 退款请求
     * @param applyUser 申请人
     * @return 退款记录ID
     */
    Long applyRefund(ParkingRefundReqVO reqVO, String applyUser);

    /**
     * 执行退款
     *
     * @param refundId 退款记录ID
     * @param auditUser 审核人
     */
    void executeRefund(Long refundId, String auditUser);

    /**
     * 关闭退款
     *
     * @param refundId 退款记录ID
     * @param reason 关闭原因
     */
    void closeRefund(Long refundId, String reason);

    /**
     * 同步退款状态（从微信查询）
     *
     * @param refundId 退款记录ID
     */
    void syncRefundStatus(Long refundId);

    /**
     * 处理退款回调
     *
     * @param xmlData 微信回调数据
     * @return 响应给微信的结果
     */
    String handleRefundNotify(String xmlData);

    /**
     * 获取退款记录分页
     *
     * @param pageReqVO 分页查询
     * @return 退款记录分页
     */
    PageResult<ParkingRefundRecordDO> getRefundRecordPage(ParkingRefundRecordPageReqVO pageReqVO);

    /**
     * 获取退款记录
     *
     * @param id 退款记录ID
     * @return 退款记录
     */
    ParkingRefundRecordDO getRefundRecord(Long id);
}
