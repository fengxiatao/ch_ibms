package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.record.ParkingRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
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

    /**
     * 统计指定时间范围内的入场车辆数
     */
    default Long selectCountByEntryTime(LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(new LambdaQueryWrapperX<ParkingRecordDO>()
                .between(ParkingRecordDO::getEntryTime, startTime, endTime));
    }

    /**
     * 统计指定时间范围内的出场车辆数
     */
    default Long selectCountByExitTime(LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(new LambdaQueryWrapperX<ParkingRecordDO>()
                .between(ParkingRecordDO::getExitTime, startTime, endTime)
                .isNotNull(ParkingRecordDO::getExitTime));
    }

    /**
     * 统计今日收入
     */
    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM iot_parking_record " +
            "WHERE exit_time BETWEEN #{startTime} AND #{endTime} " +
            "AND payment_status = 1 AND deleted = 0")
    BigDecimal selectTodayIncome(@Param("startTime") LocalDateTime startTime, 
                                  @Param("endTime") LocalDateTime endTime);
}
