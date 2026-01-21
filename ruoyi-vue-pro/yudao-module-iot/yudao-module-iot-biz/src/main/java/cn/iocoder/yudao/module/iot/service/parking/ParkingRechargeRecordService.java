package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.rechargerecord.ParkingRechargeRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.rechargerecord.ParkingRechargeStatisticsVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRechargeRecordDO;

/**
 * 月卡充值记录 Service 接口
 *
 * @author 芋道源码
 */
public interface ParkingRechargeRecordService {

    /**
     * 获得月卡充值记录分页
     *
     * @param pageReqVO 分页查询
     * @return 充值记录分页
     */
    PageResult<ParkingRechargeRecordDO> getRechargeRecordPage(ParkingRechargeRecordPageReqVO pageReqVO);

    /**
     * 获得充值统计数据
     *
     * @return 统计数据
     */
    ParkingRechargeStatisticsVO getRechargeStatistics();
}
