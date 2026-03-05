package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.blacklist.ParkingBlacklistPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.blacklist.ParkingBlacklistSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingBlacklistDO;
import jakarta.validation.Valid;

/**
 * 停车场黑名单 Service 接口
 *
 * @author 芋道源码
 */
public interface ParkingBlacklistService {

    /**
     * 创建黑名单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBlacklist(@Valid ParkingBlacklistSaveReqVO createReqVO);

    /**
     * 更新黑名单
     *
     * @param updateReqVO 更新信息
     */
    void updateBlacklist(@Valid ParkingBlacklistSaveReqVO updateReqVO);

    /**
     * 删除黑名单
     *
     * @param id 编号
     */
    void deleteBlacklist(Long id);

    /**
     * 获得黑名单
     *
     * @param id 编号
     * @return 黑名单
     */
    ParkingBlacklistDO getBlacklist(Long id);

    /**
     * 获得黑名单分页
     *
     * @param pageReqVO 分页查询
     * @return 黑名单分页
     */
    PageResult<ParkingBlacklistDO> getBlacklistPage(ParkingBlacklistPageReqVO pageReqVO);

    /**
     * 解除黑名单
     *
     * @param id 编号
     */
    void releaseBlacklist(Long id);

    /**
     * 根据车牌号查询黑名单
     *
     * @param plateNumber 车牌号
     * @return 黑名单记录
     */
    ParkingBlacklistDO getBlacklistByPlateNumber(String plateNumber);

    /**
     * 检查车牌是否在黑名单中
     *
     * @param plateNumber 车牌号
     * @param lotId 车场ID
     * @return 是否在黑名单中
     */
    boolean isInBlacklist(String plateNumber, Long lotId);
}
