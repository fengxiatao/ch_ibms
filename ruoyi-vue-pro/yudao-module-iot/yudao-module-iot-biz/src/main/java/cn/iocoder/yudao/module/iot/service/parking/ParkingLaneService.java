package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane.ParkingLanePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane.ParkingLaneSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingLaneDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 车道 Service 接口
 *
 * @author 芋道源码
 */
public interface ParkingLaneService {

    Long createParkingLane(@Valid ParkingLaneSaveReqVO createReqVO);

    void updateParkingLane(@Valid ParkingLaneSaveReqVO updateReqVO);

    void deleteParkingLane(Long id);

    ParkingLaneDO getParkingLane(Long id);

    PageResult<ParkingLaneDO> getParkingLanePage(ParkingLanePageReqVO pageReqVO);

    List<ParkingLaneDO> getParkingLaneListByLotId(Long lotId);

    /**
     * 开闸（升闸）
     *
     * @param laneId 车道ID
     */
    void openGate(Long laneId);

    /**
     * 关闸（落闸）
     *
     * @param laneId 车道ID
     */
    void closeGate(Long laneId);
}
