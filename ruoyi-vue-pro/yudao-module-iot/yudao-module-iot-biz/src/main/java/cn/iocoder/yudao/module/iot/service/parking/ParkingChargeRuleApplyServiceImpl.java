package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargeruleapply.ParkingChargeRuleApplyPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargeruleapply.ParkingChargeRuleApplySaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingChargeRuleApplyDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingChargeRuleApplyMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PARKING_CHARGE_RULE_APPLY_NOT_EXISTS;

@Service
@Validated
public class ParkingChargeRuleApplyServiceImpl implements ParkingChargeRuleApplyService {

    @Resource
    private ParkingChargeRuleApplyMapper parkingChargeRuleApplyMapper;

    @Override
    public Long createChargeRuleApply(ParkingChargeRuleApplySaveReqVO createReqVO) {
        ParkingChargeRuleApplyDO chargeRuleApply = BeanUtils.toBean(createReqVO, ParkingChargeRuleApplyDO.class);
        parkingChargeRuleApplyMapper.insert(chargeRuleApply);
        return chargeRuleApply.getId();
    }

    @Override
    public void updateChargeRuleApply(ParkingChargeRuleApplySaveReqVO updateReqVO) {
        validateChargeRuleApplyExists(updateReqVO.getId());
        ParkingChargeRuleApplyDO updateObj = BeanUtils.toBean(updateReqVO, ParkingChargeRuleApplyDO.class);
        parkingChargeRuleApplyMapper.updateById(updateObj);
    }

    @Override
    public void deleteChargeRuleApply(Long id) {
        validateChargeRuleApplyExists(id);
        parkingChargeRuleApplyMapper.deleteById(id);
    }

    private void validateChargeRuleApplyExists(Long id) {
        if (parkingChargeRuleApplyMapper.selectById(id) == null) {
            throw exception(PARKING_CHARGE_RULE_APPLY_NOT_EXISTS);
        }
    }

    @Override
    public ParkingChargeRuleApplyDO getChargeRuleApply(Long id) {
        return parkingChargeRuleApplyMapper.selectById(id);
    }

    @Override
    public PageResult<ParkingChargeRuleApplyDO> getChargeRuleApplyPage(ParkingChargeRuleApplyPageReqVO pageReqVO) {
        return parkingChargeRuleApplyMapper.selectPage(pageReqVO);
    }
}