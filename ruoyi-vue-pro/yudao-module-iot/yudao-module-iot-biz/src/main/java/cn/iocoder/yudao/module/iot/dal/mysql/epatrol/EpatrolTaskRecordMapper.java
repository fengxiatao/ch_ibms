package cn.iocoder.yudao.module.iot.dal.mysql.epatrol;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolTaskRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 电子巡更 - 任务打卡记录 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface EpatrolTaskRecordMapper extends BaseMapperX<EpatrolTaskRecordDO> {

    default List<EpatrolTaskRecordDO> selectByTaskId(Long taskId) {
        return selectList(new LambdaQueryWrapperX<EpatrolTaskRecordDO>()
                .eq(EpatrolTaskRecordDO::getTaskId, taskId)
                .orderByAsc(EpatrolTaskRecordDO::getExpectedSort));
    }

    default void deleteByTaskId(Long taskId) {
        delete(new LambdaQueryWrapperX<EpatrolTaskRecordDO>()
                .eq(EpatrolTaskRecordDO::getTaskId, taskId));
    }

}
