package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.record.ParkingRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 停车进出记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingRecordMapper extends BaseMapperX<ParkingRecordDO> {

    default PageResult<ParkingRecordDO> selectPage(ParkingRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingRecordDO>()
                .likeIfPresent(ParkingRecordDO::getPlateNumber, reqVO.getPlateNumber())
                .eqIfPresent(ParkingRecordDO::getVehicleType, reqVO.getVehicleType())
                .eqIfPresent(ParkingRecordDO::getVehicleCategory, reqVO.getVehicleCategory())
                .eqIfPresent(ParkingRecordDO::getLotId, reqVO.getLotId())
                .eqIfPresent(ParkingRecordDO::getPaymentStatus, reqVO.getPaymentStatus())
                .eqIfPresent(ParkingRecordDO::getRecordStatus, reqVO.getRecordStatus())
                .betweenIfPresent(ParkingRecordDO::getEntryTime, reqVO.getEntryTime())
                .betweenIfPresent(ParkingRecordDO::getExitTime, reqVO.getExitTime())
                .orderByDesc(ParkingRecordDO::getId));
    }

    default List<ParkingRecordDO> selectListByPlateNumber(String plateNumber) {
        return selectList(new LambdaQueryWrapperX<ParkingRecordDO>()
                .eq(ParkingRecordDO::getPlateNumber, plateNumber)
                .orderByDesc(ParkingRecordDO::getEntryTime));
    }

    default List<ParkingRecordDO> selectListByLotId(Long lotId) {
        return selectList(ParkingRecordDO::getLotId, lotId);
    }

    default ParkingRecordDO selectLatestByPlateNumber(String plateNumber) {
        return selectOne(new LambdaQueryWrapperX<ParkingRecordDO>()
                .eq(ParkingRecordDO::getPlateNumber, plateNumber)
                .orderByDesc(ParkingRecordDO::getEntryTime)
                .last("LIMIT 1"));
    }

    default List<ParkingRecordDO> selectUnpaidRecords(Long lotId) {
        return selectList(new LambdaQueryWrapperX<ParkingRecordDO>()
                .eq(ParkingRecordDO::getLotId, lotId)
                .eq(ParkingRecordDO::getPaymentStatus, 0)
                .eq(ParkingRecordDO::getRecordStatus, 1));
    }

    default Long selectCountByPaymentStatus(Integer paymentStatus, LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(new LambdaQueryWrapperX<ParkingRecordDO>()
                .eq(ParkingRecordDO::getPaymentStatus, paymentStatus)
                .between(ParkingRecordDO::getExitTime, startTime, endTime));
    }
}
