package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy.IbmsEnergyRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnergyRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 能耗采集记录 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsEnergyRecordMapper extends BaseMapperX<IbmsEnergyRecordDO> {

    default PageResult<IbmsEnergyRecordDO> selectPage(IbmsEnergyRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsEnergyRecordDO>()
                .eqIfPresent(IbmsEnergyRecordDO::getMeterId, reqVO.getMeterId())
                .eqIfPresent(IbmsEnergyRecordDO::getMeterCode, reqVO.getMeterCode())
                .betweenIfPresent(IbmsEnergyRecordDO::getReadingTime, reqVO.getStartTime(), reqVO.getEndTime())
                .orderByDesc(IbmsEnergyRecordDO::getReadingTime));
    }

    default List<IbmsEnergyRecordDO> selectLatestByMeterId(Long meterId, int limit) {
        return selectList(new LambdaQueryWrapperX<IbmsEnergyRecordDO>()
                .eq(IbmsEnergyRecordDO::getMeterId, meterId)
                .orderByDesc(IbmsEnergyRecordDO::getReadingTime)
                .last("LIMIT " + limit));
    }

    default IbmsEnergyRecordDO selectLatestByMeterId(Long meterId) {
        List<IbmsEnergyRecordDO> list = selectLatestByMeterId(meterId, 1);
        return list.isEmpty() ? null : list.get(0);
    }

}
