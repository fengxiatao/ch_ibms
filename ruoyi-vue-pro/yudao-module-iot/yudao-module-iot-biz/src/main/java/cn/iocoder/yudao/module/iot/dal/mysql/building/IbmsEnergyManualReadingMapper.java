package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy.IbmsEnergyManualReadingPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnergyManualReadingDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 人工抄表记录 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsEnergyManualReadingMapper extends BaseMapperX<IbmsEnergyManualReadingDO> {

    default PageResult<IbmsEnergyManualReadingDO> selectPage(IbmsEnergyManualReadingPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsEnergyManualReadingDO>()
                .eqIfPresent(IbmsEnergyManualReadingDO::getMeterId, reqVO.getMeterId())
                .likeIfPresent(IbmsEnergyManualReadingDO::getMeterName, reqVO.getMeterName())
                .eqIfPresent(IbmsEnergyManualReadingDO::getReadingDate, reqVO.getReadingDate())
                .eqIfPresent(IbmsEnergyManualReadingDO::getReader, reqVO.getReader())
                .eqIfPresent(IbmsEnergyManualReadingDO::getStatus, reqVO.getStatus())
                .orderByDesc(IbmsEnergyManualReadingDO::getReadingTime));
    }

    default List<IbmsEnergyManualReadingDO> selectListByDate(LocalDate date) {
        return selectList(new LambdaQueryWrapperX<IbmsEnergyManualReadingDO>()
                .eq(IbmsEnergyManualReadingDO::getReadingDate, date)
                .orderByDesc(IbmsEnergyManualReadingDO::getReadingTime));
    }

    default List<IbmsEnergyManualReadingDO> selectListByDateAndReader(LocalDate date, String reader) {
        return selectList(new LambdaQueryWrapperX<IbmsEnergyManualReadingDO>()
                .eq(IbmsEnergyManualReadingDO::getReadingDate, date)
                .eqIfPresent(IbmsEnergyManualReadingDO::getReader, reader)
                .orderByDesc(IbmsEnergyManualReadingDO::getReadingTime));
    }

    default IbmsEnergyManualReadingDO selectLatestByMeterId(Long meterId) {
        return selectOne(new LambdaQueryWrapperX<IbmsEnergyManualReadingDO>()
                .eq(IbmsEnergyManualReadingDO::getMeterId, meterId)
                .orderByDesc(IbmsEnergyManualReadingDO::getReadingTime)
                .last("LIMIT 1"));
    }

    default long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapperX<IbmsEnergyManualReadingDO>()
                .eqIfPresent(IbmsEnergyManualReadingDO::getStatus, status));
    }

}
