package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargeruleapply.ParkingChargeRuleApplyPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingChargeRuleApplyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 收费规则应用 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingChargeRuleApplyMapper extends BaseMapperX<ParkingChargeRuleApplyDO> {

    default PageResult<ParkingChargeRuleApplyDO> selectPage(ParkingChargeRuleApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingChargeRuleApplyDO>()
                .likeIfPresent(ParkingChargeRuleApplyDO::getApplyName, reqVO.getApplyName())
                .eqIfPresent(ParkingChargeRuleApplyDO::getRuleId, reqVO.getRuleId())
                .eqIfPresent(ParkingChargeRuleApplyDO::getEnabled, reqVO.getEnabled())
                .eqIfPresent(ParkingChargeRuleApplyDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ParkingChargeRuleApplyDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ParkingChargeRuleApplyDO::getPriority)
                .orderByDesc(ParkingChargeRuleApplyDO::getId));
    }

    default List<ParkingChargeRuleApplyDO> selectListByRuleId(Long ruleId) {
        return selectList(ParkingChargeRuleApplyDO::getRuleId, ruleId);
    }

    default List<ParkingChargeRuleApplyDO> selectListByEnabled(Integer enabled) {
        return selectList(new LambdaQueryWrapperX<ParkingChargeRuleApplyDO>()
                .eq(ParkingChargeRuleApplyDO::getEnabled, enabled)
                .orderByDesc(ParkingChargeRuleApplyDO::getPriority));
    }

    /**
     * 根据停车场ID和车辆类别查询启用的收费规则应用
     * 按优先级降序排序，返回优先级最高的
     *
     * @param lotId 停车场ID
     * @param vehicleCategory 车辆类别（temporary-临时车）
     * @return 收费规则应用列表
     */
    default List<ParkingChargeRuleApplyDO> selectByLotIdAndCategory(Long lotId, String vehicleCategory) {
        return selectList(new LambdaQueryWrapperX<ParkingChargeRuleApplyDO>()
                .eq(ParkingChargeRuleApplyDO::getEnabled, 1)
                .eq(ParkingChargeRuleApplyDO::getStatus, 0)
                .eq(ParkingChargeRuleApplyDO::getVehicleCategory, vehicleCategory)
                .apply(lotId != null, "JSON_CONTAINS(lot_ids, CONCAT('\"', {0}, '\"')) OR lot_ids IS NULL OR lot_ids = '[]'", lotId.toString())
                .orderByDesc(ParkingChargeRuleApplyDO::getPriority));
    }

    /**
     * 根据停车场ID、车辆类别、收费车型查询启用的收费规则应用
     * 按优先级降序排序，返回优先级最高的
     *
     * @param lotId 停车场ID
     * @param vehicleCategory 车辆类别（temporary-临时车）
     * @param vehicleType 收费车型：1-小型车，2-中型车，3-新能源车，4-大型车，5-超大型车
     * @return 收费规则应用列表
     */
    default List<ParkingChargeRuleApplyDO> selectByLotIdAndCategoryAndVehicleType(Long lotId, String vehicleCategory, Integer vehicleType) {
        LambdaQueryWrapperX<ParkingChargeRuleApplyDO> wrapper = new LambdaQueryWrapperX<ParkingChargeRuleApplyDO>()
                .eq(ParkingChargeRuleApplyDO::getEnabled, 1)
                .eq(ParkingChargeRuleApplyDO::getStatus, 0)
                .eq(ParkingChargeRuleApplyDO::getVehicleCategory, vehicleCategory);
        
        // 停车场匹配：JSON_CONTAINS 或 空/null
        if (lotId != null) {
            wrapper.apply("(JSON_CONTAINS(lot_ids, CONCAT('\"', {0}, '\"')) OR lot_ids IS NULL OR lot_ids = '[]')", lotId.toString());
        }
        
        // 收费车型匹配：JSON_CONTAINS 或 空/null
        if (vehicleType != null) {
            wrapper.apply("(JSON_CONTAINS(charge_vehicle_types, CONCAT('\"', {0}, '\"')) OR charge_vehicle_types IS NULL OR charge_vehicle_types = '[]')", vehicleType.toString());
        }
        
        return selectList(wrapper.orderByDesc(ParkingChargeRuleApplyDO::getPriority));
    }
}
