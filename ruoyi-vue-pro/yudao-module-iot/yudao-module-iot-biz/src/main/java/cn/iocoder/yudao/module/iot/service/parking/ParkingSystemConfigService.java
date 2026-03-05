package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.system.ParkingSystemConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingSystemConfigDO;
import jakarta.validation.Valid;

public interface ParkingSystemConfigService {

    ParkingSystemConfigDO getConfig(Long lotId);

    void saveConfig(@Valid ParkingSystemConfigSaveReqVO reqVO);
}

