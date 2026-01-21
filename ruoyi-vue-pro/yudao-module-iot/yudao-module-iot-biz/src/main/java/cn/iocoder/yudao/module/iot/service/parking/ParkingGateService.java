package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.gate.ParkingGatePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.gate.ParkingGateSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingGateDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 道闸设备 Service 接口
 *
 * @author 芋道源码
 */
public interface ParkingGateService {

    /**
     * 创建道闸设备
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createParkingGate(@Valid ParkingGateSaveReqVO createReqVO);

    /**
     * 更新道闸设备
     *
     * @param updateReqVO 更新信息
     */
    void updateParkingGate(@Valid ParkingGateSaveReqVO updateReqVO);

    /**
     * 删除道闸设备
     *
     * @param id 编号
     */
    void deleteParkingGate(Long id);

    /**
     * 获得道闸设备
     *
     * @param id 编号
     * @return 道闸设备
     */
    ParkingGateDO getParkingGate(Long id);

    /**
     * 获得道闸设备分页
     *
     * @param pageReqVO 分页查询
     * @return 道闸设备分页
     */
    PageResult<ParkingGateDO> getParkingGatePage(ParkingGatePageReqVO pageReqVO);

    /**
     * 获得车场的道闸设备列表
     *
     * @param lotId 车场ID
     * @return 道闸设备列表
     */
    List<ParkingGateDO> getParkingGateListByLotId(Long lotId);
}
