package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargerule.ParkingChargeRulePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingChargeRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 收费规则 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingChargeRuleMapper extends BaseMapperX<ParkingChargeRuleDO> {

    default PageResult<ParkingChargeRuleDO> selectPage(ParkingChargeRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingChargeRuleDO>()
                .likeIfPresent(ParkingChargeRuleDO::getRuleName, reqVO.getRuleName())
                .eqIfPresent(ParkingChargeRuleDO::getChargeMode, reqVO.getChargeMode())
                .eqIfPresent(ParkingChargeRuleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ParkingChargeRuleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ParkingChargeRuleDO::getId));
    }

    default List<ParkingChargeRuleDO> selectListByStatus(Integer status) {
        return selectList(ParkingChargeRuleDO::getStatus, status);
    }
}
