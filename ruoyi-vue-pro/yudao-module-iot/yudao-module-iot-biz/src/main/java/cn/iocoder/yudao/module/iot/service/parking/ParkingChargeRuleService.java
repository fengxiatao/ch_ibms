package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargerule.ParkingChargeRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargerule.ParkingChargeRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingChargeRuleDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 收费规则 Service 接口
 *
 * @author 芋道源码
 */
public interface ParkingChargeRuleService {

    Long createChargeRule(@Valid ParkingChargeRuleSaveReqVO createReqVO);

    void updateChargeRule(@Valid ParkingChargeRuleSaveReqVO updateReqVO);

    void deleteChargeRule(Long id);

    ParkingChargeRuleDO getChargeRule(Long id);

    PageResult<ParkingChargeRuleDO> getChargeRulePage(ParkingChargeRulePageReqVO pageReqVO);

    List<ParkingChargeRuleDO> getChargeRuleList();
}
