package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.passrule.ParkingPassRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.passrule.ParkingPassRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingPassRuleDO;
import jakarta.validation.Valid;

public interface ParkingPassRuleService {

    Long createPassRule(@Valid ParkingPassRuleSaveReqVO createReqVO);

    void updatePassRule(@Valid ParkingPassRuleSaveReqVO updateReqVO);

    void deletePassRule(Long id);

    ParkingPassRuleDO getPassRule(Long id);

    PageResult<ParkingPassRuleDO> getPassRulePage(ParkingPassRulePageReqVO pageReqVO);
}