package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.passrule.ParkingPassRulePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingPassRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 放行规则 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingPassRuleMapper extends BaseMapperX<ParkingPassRuleDO> {

    default PageResult<ParkingPassRuleDO> selectPage(ParkingPassRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingPassRuleDO>()
                .likeIfPresent(ParkingPassRuleDO::getRuleName, reqVO.getRuleName())
                .eqIfPresent(ParkingPassRuleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ParkingPassRuleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ParkingPassRuleDO::getPriority)
                .orderByDesc(ParkingPassRuleDO::getId));
    }

    default List<ParkingPassRuleDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ParkingPassRuleDO>()
                .eq(ParkingPassRuleDO::getStatus, status)
                .orderByDesc(ParkingPassRuleDO::getPriority));
    }
}
