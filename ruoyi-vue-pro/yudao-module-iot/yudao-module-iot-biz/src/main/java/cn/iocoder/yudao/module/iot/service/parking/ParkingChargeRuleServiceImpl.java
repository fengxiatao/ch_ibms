package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargerule.ParkingChargeRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargerule.ParkingChargeRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingChargeRuleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingChargeRuleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ParkingChargeRuleServiceImpl implements ParkingChargeRuleService {

    @Resource
    private ParkingChargeRuleMapper parkingChargeRuleMapper;

    @Override
    public Long createChargeRule(ParkingChargeRuleSaveReqVO createReqVO) {
        ParkingChargeRuleDO chargeRule = BeanUtils.toBean(createReqVO, ParkingChargeRuleDO.class);
        parkingChargeRuleMapper.insert(chargeRule);
        return chargeRule.getId();
    }

    @Override
    public void updateChargeRule(ParkingChargeRuleSaveReqVO updateReqVO) {
        validateChargeRuleExists(updateReqVO.getId());
        ParkingChargeRuleDO updateObj = BeanUtils.toBean(updateReqVO, ParkingChargeRuleDO.class);
        parkingChargeRuleMapper.updateById(updateObj);
    }

    @Override
    public void deleteChargeRule(Long id) {
        validateChargeRuleExists(id);
        parkingChargeRuleMapper.deleteById(id);
    }

    private void validateChargeRuleExists(Long id) {
        if (parkingChargeRuleMapper.selectById(id) == null) {
            throw exception(PARKING_CHARGE_RULE_NOT_EXISTS);
        }
    }

    @Override
    public ParkingChargeRuleDO getChargeRule(Long id) {
        return parkingChargeRuleMapper.selectById(id);
    }

    @Override
    public PageResult<ParkingChargeRuleDO> getChargeRulePage(ParkingChargeRulePageReqVO pageReqVO) {
        return parkingChargeRuleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ParkingChargeRuleDO> getChargeRuleList() {
        return parkingChargeRuleMapper.selectListByStatus(0);
    }
}
