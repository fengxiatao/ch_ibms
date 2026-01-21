package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.passrule.ParkingPassRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.passrule.ParkingPassRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingPassRuleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingPassRuleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PARKING_PASS_RULE_NOT_EXISTS;

@Service
@Validated
public class ParkingPassRuleServiceImpl implements ParkingPassRuleService {

    @Resource
    private ParkingPassRuleMapper parkingPassRuleMapper;

    @Override
    public Long createPassRule(ParkingPassRuleSaveReqVO createReqVO) {
        ParkingPassRuleDO passRule = BeanUtils.toBean(createReqVO, ParkingPassRuleDO.class);
        parkingPassRuleMapper.insert(passRule);
        return passRule.getId();
    }

    @Override
    public void updatePassRule(ParkingPassRuleSaveReqVO updateReqVO) {
        validatePassRuleExists(updateReqVO.getId());
        ParkingPassRuleDO updateObj = BeanUtils.toBean(updateReqVO, ParkingPassRuleDO.class);
        parkingPassRuleMapper.updateById(updateObj);
    }

    @Override
    public void deletePassRule(Long id) {
        validatePassRuleExists(id);
        parkingPassRuleMapper.deleteById(id);
    }

    private void validatePassRuleExists(Long id) {
        if (parkingPassRuleMapper.selectById(id) == null) {
            throw exception(PARKING_PASS_RULE_NOT_EXISTS);
        }
    }

    @Override
    public ParkingPassRuleDO getPassRule(Long id) {
        return parkingPassRuleMapper.selectById(id);
    }

    @Override
    public PageResult<ParkingPassRuleDO> getPassRulePage(ParkingPassRulePageReqVO pageReqVO) {
        return parkingPassRuleMapper.selectPage(pageReqVO);
    }
}