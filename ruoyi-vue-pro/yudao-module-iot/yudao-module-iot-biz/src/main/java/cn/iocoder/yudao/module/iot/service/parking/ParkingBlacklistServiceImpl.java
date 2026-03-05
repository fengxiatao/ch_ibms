package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.blacklist.ParkingBlacklistPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.blacklist.ParkingBlacklistSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingBlacklistDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingBlacklistMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 停车场黑名单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ParkingBlacklistServiceImpl implements ParkingBlacklistService {

    @Resource
    private ParkingBlacklistMapper parkingBlacklistMapper;

    @Override
    public Long createBlacklist(ParkingBlacklistSaveReqVO createReqVO) {
        // 校验车牌号是否已在黑名单中
        validatePlateNumberUnique(null, createReqVO.getPlateNumber());
        
        ParkingBlacklistDO blacklist = BeanUtils.toBean(createReqVO, ParkingBlacklistDO.class);
        if (blacklist.getStatus() == null) {
            blacklist.setStatus(0); // 默认生效中
        }
        parkingBlacklistMapper.insert(blacklist);
        
        log.info("[createBlacklist] 新增黑名单车辆，车牌号：{}", createReqVO.getPlateNumber());
        return blacklist.getId();
    }

    @Override
    public void updateBlacklist(ParkingBlacklistSaveReqVO updateReqVO) {
        // 校验存在
        validateBlacklistExists(updateReqVO.getId());
        // 校验车牌号是否已在黑名单中
        validatePlateNumberUnique(updateReqVO.getId(), updateReqVO.getPlateNumber());
        
        ParkingBlacklistDO updateObj = BeanUtils.toBean(updateReqVO, ParkingBlacklistDO.class);
        parkingBlacklistMapper.updateById(updateObj);
        
        log.info("[updateBlacklist] 更新黑名单车辆，id={}，车牌号：{}", updateReqVO.getId(), updateReqVO.getPlateNumber());
    }

    @Override
    public void deleteBlacklist(Long id) {
        // 校验存在
        validateBlacklistExists(id);
        
        parkingBlacklistMapper.deleteById(id);
        log.info("[deleteBlacklist] 删除黑名单车辆，id={}", id);
    }

    private void validateBlacklistExists(Long id) {
        if (parkingBlacklistMapper.selectById(id) == null) {
            throw exception(PARKING_BLACKLIST_NOT_EXISTS);
        }
    }

    private void validatePlateNumberUnique(Long id, String plateNumber) {
        ParkingBlacklistDO blacklist = parkingBlacklistMapper.selectByPlateNumber(plateNumber);
        if (blacklist == null) {
            return;
        }
        // 如果是更新操作，且更新的是同一条记录，则不报错
        if (id != null && id.equals(blacklist.getId())) {
            return;
        }
        throw exception(PARKING_BLACKLIST_PLATE_EXISTS);
    }

    @Override
    public ParkingBlacklistDO getBlacklist(Long id) {
        return parkingBlacklistMapper.selectById(id);
    }

    @Override
    public PageResult<ParkingBlacklistDO> getBlacklistPage(ParkingBlacklistPageReqVO pageReqVO) {
        return parkingBlacklistMapper.selectPage(pageReqVO);
    }

    @Override
    public void releaseBlacklist(Long id) {
        // 校验存在
        ParkingBlacklistDO blacklist = parkingBlacklistMapper.selectById(id);
        if (blacklist == null) {
            throw exception(PARKING_BLACKLIST_NOT_EXISTS);
        }
        
        // 更新状态为已解除
        ParkingBlacklistDO updateObj = new ParkingBlacklistDO();
        updateObj.setId(id);
        updateObj.setStatus(1); // 已解除
        parkingBlacklistMapper.updateById(updateObj);
        
        log.info("[releaseBlacklist] 解除黑名单车辆，id={}，车牌号：{}", id, blacklist.getPlateNumber());
    }

    @Override
    public ParkingBlacklistDO getBlacklistByPlateNumber(String plateNumber) {
        return parkingBlacklistMapper.selectByPlateNumber(plateNumber);
    }

    @Override
    public boolean isInBlacklist(String plateNumber, Long lotId) {
        ParkingBlacklistDO blacklist = parkingBlacklistMapper.selectByPlateNumberAndLotId(plateNumber, lotId);
        if (blacklist == null) {
            return false;
        }
        // 检查是否已过期
        if (blacklist.getEndTime() != null && blacklist.getEndTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }
}
