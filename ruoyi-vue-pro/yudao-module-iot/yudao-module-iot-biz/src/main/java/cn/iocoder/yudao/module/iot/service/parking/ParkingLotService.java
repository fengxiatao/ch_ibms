package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lot.ParkingLotPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lot.ParkingLotSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingLotDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 停车场/车场 Service 接口
 *
 * @author 芋道源码
 */
public interface ParkingLotService {

    /**
     * 创建车场
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createParkingLot(@Valid ParkingLotSaveReqVO createReqVO);

    /**
     * 更新车场
     *
     * @param updateReqVO 更新信息
     */
    void updateParkingLot(@Valid ParkingLotSaveReqVO updateReqVO);

    /**
     * 删除车场
     *
     * @param id 编号
     */
    void deleteParkingLot(Long id);

    /**
     * 获得车场
     *
     * @param id 编号
     * @return 车场
     */
    ParkingLotDO getParkingLot(Long id);

    /**
     * 获得车场分页
     *
     * @param pageReqVO 分页查询
     * @return 车场分页
     */
    PageResult<ParkingLotDO> getParkingLotPage(ParkingLotPageReqVO pageReqVO);

    /**
     * 获得所有正常状态的车场列表
     *
     * @return 车场列表
     */
    List<ParkingLotDO> getParkingLotList();
}
