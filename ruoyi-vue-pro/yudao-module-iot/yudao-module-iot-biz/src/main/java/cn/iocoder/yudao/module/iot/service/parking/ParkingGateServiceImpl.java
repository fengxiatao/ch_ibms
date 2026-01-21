package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.gate.ParkingGatePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.gate.ParkingGateSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingGateDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingGateMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 道闸设备 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ParkingGateServiceImpl implements ParkingGateService {

    @Resource
    private ParkingGateMapper parkingGateMapper;

    @Override
    public Long createParkingGate(ParkingGateSaveReqVO createReqVO) {
        ParkingGateDO parkingGate = BeanUtils.toBean(createReqVO, ParkingGateDO.class);
        parkingGateMapper.insert(parkingGate);
        return parkingGate.getId();
    }

    @Override
    public void updateParkingGate(ParkingGateSaveReqVO updateReqVO) {
        validateParkingGateExists(updateReqVO.getId());
        ParkingGateDO updateObj = BeanUtils.toBean(updateReqVO, ParkingGateDO.class);
        parkingGateMapper.updateById(updateObj);
    }

    @Override
    public void deleteParkingGate(Long id) {
        validateParkingGateExists(id);
        parkingGateMapper.deleteById(id);
    }

    private void validateParkingGateExists(Long id) {
        if (parkingGateMapper.selectById(id) == null) {
            throw exception(PARKING_GATE_NOT_EXISTS);
        }
    }

    @Override
    public ParkingGateDO getParkingGate(Long id) {
        return parkingGateMapper.selectById(id);
    }

    @Override
    public PageResult<ParkingGateDO> getParkingGatePage(ParkingGatePageReqVO pageReqVO) {
        return parkingGateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ParkingGateDO> getParkingGateListByLotId(Long lotId) {
        return parkingGateMapper.selectListByLotId(lotId);
    }
}
