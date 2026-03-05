package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.env.IbmsEnvDataRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvDataRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 环境监测数据记录 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsEnvDataRecordMapper extends BaseMapperX<IbmsEnvDataRecordDO> {

    default PageResult<IbmsEnvDataRecordDO> selectPage(IbmsEnvDataRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsEnvDataRecordDO>()
                .eqIfPresent(IbmsEnvDataRecordDO::getSensorId, reqVO.getSensorId())
                .eqIfPresent(IbmsEnvDataRecordDO::getSensorCode, reqVO.getSensorCode())
                .betweenIfPresent(IbmsEnvDataRecordDO::getCollectTime, reqVO.getStartTime(), reqVO.getEndTime())
                .orderByDesc(IbmsEnvDataRecordDO::getCollectTime));
    }

    default List<IbmsEnvDataRecordDO> selectLatestBySensorId(Long sensorId, int limit) {
        return selectList(new LambdaQueryWrapperX<IbmsEnvDataRecordDO>()
                .eq(IbmsEnvDataRecordDO::getSensorId, sensorId)
                .orderByDesc(IbmsEnvDataRecordDO::getCollectTime)
                .last("LIMIT " + limit));
    }

    default IbmsEnvDataRecordDO selectLatestBySensorId(Long sensorId) {
        List<IbmsEnvDataRecordDO> list = selectLatestBySensorId(sensorId, 1);
        return list.isEmpty() ? null : list.get(0);
    }

}
