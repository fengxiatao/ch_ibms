package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargeruleapply.ParkingChargeRuleApplyPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargeruleapply.ParkingChargeRuleApplySaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingChargeRuleApplyDO;
import jakarta.validation.Valid;

public interface ParkingChargeRuleApplyService {

    Long createChargeRuleApply(@Valid ParkingChargeRuleApplySaveReqVO createReqVO);

    void updateChargeRuleApply(@Valid ParkingChargeRuleApplySaveReqVO updateReqVO);

    void deleteChargeRuleApply(Long id);

    ParkingChargeRuleApplyDO getChargeRuleApply(Long id);

    PageResult<ParkingChargeRuleApplyDO> getChargeRuleApplyPage(ParkingChargeRuleApplyPageReqVO pageReqVO);
}