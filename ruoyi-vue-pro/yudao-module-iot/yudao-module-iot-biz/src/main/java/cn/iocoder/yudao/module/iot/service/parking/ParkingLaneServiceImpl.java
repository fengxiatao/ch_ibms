package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane.ParkingLanePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane.ParkingLaneSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingLaneDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingLaneMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ParkingLaneServiceImpl implements ParkingLaneService {

    @Resource
    private ParkingLaneMapper parkingLaneMapper;

    @Override
    public Long createParkingLane(ParkingLaneSaveReqVO createReqVO) {
        ParkingLaneDO parkingLane = BeanUtils.toBean(createReqVO, ParkingLaneDO.class);
        parkingLaneMapper.insert(parkingLane);
        return parkingLane.getId();
    }

    @Override
    public void updateParkingLane(ParkingLaneSaveReqVO updateReqVO) {
        validateParkingLaneExists(updateReqVO.getId());
        ParkingLaneDO updateObj = BeanUtils.toBean(updateReqVO, ParkingLaneDO.class);
        parkingLaneMapper.updateById(updateObj);
    }

    @Override
    public void deleteParkingLane(Long id) {
        validateParkingLaneExists(id);
        parkingLaneMapper.deleteById(id);
    }

    private void validateParkingLaneExists(Long id) {
        if (parkingLaneMapper.selectById(id) == null) {
            throw exception(PARKING_LANE_NOT_EXISTS);
        }
    }

    @Override
    public ParkingLaneDO getParkingLane(Long id) {
        return parkingLaneMapper.selectById(id);
    }

    @Override
    public PageResult<ParkingLaneDO> getParkingLanePage(ParkingLanePageReqVO pageReqVO) {
        return parkingLaneMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ParkingLaneDO> getParkingLaneListByLotId(Long lotId) {
        return parkingLaneMapper.selectListByLotId(lotId);
    }
}
