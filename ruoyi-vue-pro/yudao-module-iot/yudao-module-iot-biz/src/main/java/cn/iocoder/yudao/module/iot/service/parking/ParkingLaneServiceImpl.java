package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane.ParkingLanePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane.ParkingLaneSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingGateDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingLaneDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingGateMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingLaneMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class ParkingLaneServiceImpl implements ParkingLaneService {

    @Resource
    private ParkingLaneMapper parkingLaneMapper;

    @Resource
    private ParkingGateMapper parkingGateMapper;

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

    @Override
    public void openGate(Long laneId) {
        // 1. 校验车道存在
        ParkingLaneDO lane = parkingLaneMapper.selectById(laneId);
        if (lane == null) {
            throw exception(PARKING_LANE_NOT_EXISTS);
        }

        // 2. 查询该车道下的道闸设备
        List<ParkingGateDO> gates = parkingGateMapper.selectListByLaneId(laneId);
        if (gates.isEmpty()) {
            log.warn("[openGate] 车道下没有配置道闸设备，车道ID：{}", laneId);
            return;
        }

        // 3. 向车道下的道闸发送开闸指令
        for (ParkingGateDO gate : gates) {
            if (gate.getStatus() != null && gate.getStatus() == 1) {
                log.warn("[openGate] 道闸已停用，跳过，道闸ID：{}", gate.getId());
                continue;
            }
            try {
                sendOpenCommand(gate);
                log.info("[openGate] 开闸指令发送成功，车道ID：{}，道闸ID：{}", laneId, gate.getId());
            } catch (Exception e) {
                log.error("[openGate] 开闸指令发送失败，车道ID：{}，道闸ID：{}，错误：{}", 
                        laneId, gate.getId(), e.getMessage());
            }
        }
    }

    @Override
    public void closeGate(Long laneId) {
        // 1. 校验车道存在
        ParkingLaneDO lane = parkingLaneMapper.selectById(laneId);
        if (lane == null) {
            throw exception(PARKING_LANE_NOT_EXISTS);
        }

        // 2. 查询该车道下的道闸设备
        List<ParkingGateDO> gates = parkingGateMapper.selectListByLaneId(laneId);
        if (gates.isEmpty()) {
            log.warn("[closeGate] 车道下没有配置道闸设备，车道ID：{}", laneId);
            return;
        }

        // 3. 向车道下的道闸发送关闸指令
        for (ParkingGateDO gate : gates) {
            if (gate.getStatus() != null && gate.getStatus() == 1) {
                log.warn("[closeGate] 道闸已停用，跳过，道闸ID：{}", gate.getId());
                continue;
            }
            try {
                sendCloseCommand(gate);
                log.info("[closeGate] 关闸指令发送成功，车道ID：{}，道闸ID：{}", laneId, gate.getId());
            } catch (Exception e) {
                log.error("[closeGate] 关闸指令发送失败，车道ID：{}，道闸ID：{}，错误：{}", 
                        laneId, gate.getId(), e.getMessage());
            }
        }
    }

    /**
     * 发送开闸指令
     *
     * TODO: 实际项目中需要根据道闸设备类型调用不同的通信协议
     */
    private void sendOpenCommand(ParkingGateDO gate) {
        String manufacturer = gate.getManufacturer();
        String deviceIp = gate.getIpAddress();
        Integer devicePort = gate.getPort();

        log.info("[sendOpenCommand] 发送开闸指令，厂商：{}，设备IP：{}，端口：{}", 
                manufacturer, deviceIp, devicePort);

        // TODO: 实现实际的设备通信逻辑
    }

    /**
     * 发送关闸指令
     */
    private void sendCloseCommand(ParkingGateDO gate) {
        String manufacturer = gate.getManufacturer();
        String deviceIp = gate.getIpAddress();
        Integer devicePort = gate.getPort();

        log.info("[sendCloseCommand] 发送关闸指令，厂商：{}，设备IP：{}，端口：{}", 
                manufacturer, deviceIp, devicePort);

        // TODO: 实现实际的设备通信逻辑
    }
}
