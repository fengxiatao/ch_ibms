package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.freevehicle.ParkingFreeVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.freevehicle.ParkingFreeVehicleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingFreeVehicleDO;
import jakarta.validation.Valid;

/**
 * 免费车 Service 接口
 *
 * @author 芋道源码
 */
public interface ParkingFreeVehicleService {

    Long createFreeVehicle(@Valid ParkingFreeVehicleSaveReqVO createReqVO);

    void updateFreeVehicle(@Valid ParkingFreeVehicleSaveReqVO updateReqVO);

    void deleteFreeVehicle(Long id);

    ParkingFreeVehicleDO getFreeVehicle(Long id);

    PageResult<ParkingFreeVehicleDO> getFreeVehiclePage(ParkingFreeVehiclePageReqVO pageReqVO);

    ParkingFreeVehicleDO getFreeVehicleByPlateNumber(String plateNumber);
}
