package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.freevehicle.ParkingFreeVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.freevehicle.ParkingFreeVehicleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingFreeVehicleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingFreeVehicleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ParkingFreeVehicleServiceImpl implements ParkingFreeVehicleService {

    @Resource
    private ParkingFreeVehicleMapper parkingFreeVehicleMapper;

    @Override
    public Long createFreeVehicle(ParkingFreeVehicleSaveReqVO createReqVO) {
        validatePlateNumberUnique(null, createReqVO.getPlateNumber());
        ParkingFreeVehicleDO freeVehicle = BeanUtils.toBean(createReqVO, ParkingFreeVehicleDO.class);
        parkingFreeVehicleMapper.insert(freeVehicle);
        return freeVehicle.getId();
    }

    @Override
    public void updateFreeVehicle(ParkingFreeVehicleSaveReqVO updateReqVO) {
        validateFreeVehicleExists(updateReqVO.getId());
        validatePlateNumberUnique(updateReqVO.getId(), updateReqVO.getPlateNumber());
        ParkingFreeVehicleDO updateObj = BeanUtils.toBean(updateReqVO, ParkingFreeVehicleDO.class);
        parkingFreeVehicleMapper.updateById(updateObj);
    }

    @Override
    public void deleteFreeVehicle(Long id) {
        validateFreeVehicleExists(id);
        parkingFreeVehicleMapper.deleteById(id);
    }

    private void validateFreeVehicleExists(Long id) {
        if (parkingFreeVehicleMapper.selectById(id) == null) {
            throw exception(PARKING_FREE_VEHICLE_NOT_EXISTS);
        }
    }

    private void validatePlateNumberUnique(Long id, String plateNumber) {
        ParkingFreeVehicleDO vehicle = parkingFreeVehicleMapper.selectByPlateNumber(plateNumber);
        if (vehicle == null) {
            return;
        }
        if (id == null || !id.equals(vehicle.getId())) {
            throw exception(PARKING_FREE_VEHICLE_PLATE_EXISTS);
        }
    }

    @Override
    public ParkingFreeVehicleDO getFreeVehicle(Long id) {
        return parkingFreeVehicleMapper.selectById(id);
    }

    @Override
    public PageResult<ParkingFreeVehicleDO> getFreeVehiclePage(ParkingFreeVehiclePageReqVO pageReqVO) {
        return parkingFreeVehicleMapper.selectPage(pageReqVO);
    }

    @Override
    public ParkingFreeVehicleDO getFreeVehicleByPlateNumber(String plateNumber) {
        return parkingFreeVehicleMapper.selectByPlateNumber(plateNumber);
    }
}
