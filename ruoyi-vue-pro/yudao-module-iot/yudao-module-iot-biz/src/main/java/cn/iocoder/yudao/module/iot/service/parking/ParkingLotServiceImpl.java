package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lot.ParkingLotPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lot.ParkingLotSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingLotDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingLotMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 停车场/车场 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ParkingLotServiceImpl implements ParkingLotService {

    @Resource
    private ParkingLotMapper parkingLotMapper;

    @Override
    public Long createParkingLot(ParkingLotSaveReqVO createReqVO) {
        // 校验名称唯一
        validateLotNameUnique(null, createReqVO.getLotName());
        // 插入
        ParkingLotDO parkingLot = BeanUtils.toBean(createReqVO, ParkingLotDO.class);
        parkingLotMapper.insert(parkingLot);
        return parkingLot.getId();
    }

    @Override
    public void updateParkingLot(ParkingLotSaveReqVO updateReqVO) {
        // 校验存在
        validateParkingLotExists(updateReqVO.getId());
        // 校验名称唯一
        validateLotNameUnique(updateReqVO.getId(), updateReqVO.getLotName());
        // 更新
        ParkingLotDO updateObj = BeanUtils.toBean(updateReqVO, ParkingLotDO.class);
        parkingLotMapper.updateById(updateObj);
    }

    @Override
    public void deleteParkingLot(Long id) {
        // 校验存在
        validateParkingLotExists(id);
        // 删除
        parkingLotMapper.deleteById(id);
    }

    private void validateParkingLotExists(Long id) {
        if (parkingLotMapper.selectById(id) == null) {
            throw exception(PARKING_LOT_NOT_EXISTS);
        }
    }

    private void validateLotNameUnique(Long id, String lotName) {
        ParkingLotDO lot = parkingLotMapper.selectByLotName(lotName);
        if (lot == null) {
            return;
        }
        if (id == null || !id.equals(lot.getId())) {
            throw exception(PARKING_LOT_NAME_EXISTS);
        }
    }

    @Override
    public ParkingLotDO getParkingLot(Long id) {
        return parkingLotMapper.selectById(id);
    }

    @Override
    public PageResult<ParkingLotDO> getParkingLotPage(ParkingLotPageReqVO pageReqVO) {
        return parkingLotMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ParkingLotDO> getParkingLotList() {
        return parkingLotMapper.selectListByStatus(0);
    }
}
