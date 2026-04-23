package cn.iocoder.yudao.module.iot.dal.mysql.ibms;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.hutool.core.collection.CollUtil;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IBMS 设备运行态 Mapper
 */
@Mapper
public interface IbmsDeviceRuntimeMapper extends BaseMapperX<IbmsDeviceRuntimeDO> {

    /**
     * 运行态为指定状态的设备主键列表（通常为在线调度、采集任务用）。
     */
    default List<Long> selectDeviceIdsByState(int state) {
        return selectObjs(new LambdaQueryWrapperX<IbmsDeviceRuntimeDO>()
                .select(IbmsDeviceRuntimeDO::getDeviceId)
                .eq(IbmsDeviceRuntimeDO::getState, state));
    }

    /**
     * 在线 IBMS 设备主键（与 {@link IotDeviceStateEnum#ONLINE} 对齐）。
     */
    default List<Long> selectOnlineDeviceIds() {
        return selectDeviceIdsByState(IotDeviceStateEnum.ONLINE.getState());
    }

    /**
     * 批量查询设备运行态 state，供列表/精简列表补全在线态。
     */
    default Map<Long, Integer> selectStateMapByDeviceIds(Collection<Long> deviceIds) {
        if (CollUtil.isEmpty(deviceIds)) {
            return Map.of();
        }
        List<IbmsDeviceRuntimeDO> list = selectList(new LambdaQueryWrapperX<IbmsDeviceRuntimeDO>()
                .in(IbmsDeviceRuntimeDO::getDeviceId, deviceIds)
                .select(IbmsDeviceRuntimeDO::getDeviceId, IbmsDeviceRuntimeDO::getState));
        Map<Long, Integer> m = new HashMap<>();
        if (list == null) {
            return m;
        }
        for (IbmsDeviceRuntimeDO r : list) {
            if (r.getDeviceId() != null && r.getState() != null) {
                m.put(r.getDeviceId(), r.getState());
            }
        }
        return m;
    }
}
