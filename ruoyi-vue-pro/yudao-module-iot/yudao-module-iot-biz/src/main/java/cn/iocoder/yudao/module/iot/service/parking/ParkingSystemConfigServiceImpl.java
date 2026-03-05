package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.system.ParkingSystemConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingSystemConfigDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingSystemConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class ParkingSystemConfigServiceImpl implements ParkingSystemConfigService {

    @Resource
    private ParkingSystemConfigMapper parkingSystemConfigMapper;

    @Override
    public ParkingSystemConfigDO getConfig(Long lotId) {
        // 简化处理：如果未传 lotId，则取默认配置
        return parkingSystemConfigMapper.selectByLotIdOrFirst(lotId);
    }

    @Override
    public void saveConfig(ParkingSystemConfigSaveReqVO reqVO) {
        ParkingSystemConfigDO config = parkingSystemConfigMapper.selectById(reqVO.getId());
        if (config == null) {
            config = new ParkingSystemConfigDO();
        }
        config.setId(reqVO.getId());
        config.setParkingName(reqVO.getParkingName());
        config.setAddress(reqVO.getAddress());
        config.setPhone(reqVO.getPhone());
        config.setTotalSpaces(reqVO.getTotalSpaces());
        config.setBusinessHours(reqVO.getBusinessHours());
        config.setParkingType(reqVO.getParkingType());
        config.setRemark(reqVO.getRemark());
        if (config.getId() == null) {
            parkingSystemConfigMapper.insert(config);
        } else {
            parkingSystemConfigMapper.updateById(config);
        }
    }
}

