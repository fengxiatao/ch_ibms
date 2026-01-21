package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.refund.ParkingRefundRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRefundRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 停车退款记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingRefundRecordMapper extends BaseMapperX<ParkingRefundRecordDO> {

    default PageResult<ParkingRefundRecordDO> selectPage(ParkingRefundRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingRefundRecordDO>()
                .likeIfPresent(ParkingRefundRecordDO::getPlateNumber, reqVO.getPlateNumber())
                .eqIfPresent(ParkingRefundRecordDO::getRefundStatus, reqVO.getRefundStatus())
                .likeIfPresent(ParkingRefundRecordDO::getOutTradeNo, reqVO.getOutTradeNo())
                .likeIfPresent(ParkingRefundRecordDO::getOutRefundNo, reqVO.getOutRefundNo())
                .betweenIfPresent(ParkingRefundRecordDO::getApplyTime, reqVO.getApplyTime())
                .orderByDesc(ParkingRefundRecordDO::getId));
    }

    default ParkingRefundRecordDO selectByOutTradeNo(String outTradeNo) {
        return selectOne(ParkingRefundRecordDO::getOutTradeNo, outTradeNo);
    }

    default ParkingRefundRecordDO selectByOutRefundNo(String outRefundNo) {
        return selectOne(ParkingRefundRecordDO::getOutRefundNo, outRefundNo);
    }

    default ParkingRefundRecordDO selectByRecordId(Long recordId) {
        return selectOne(ParkingRefundRecordDO::getRecordId, recordId);
    }
}
