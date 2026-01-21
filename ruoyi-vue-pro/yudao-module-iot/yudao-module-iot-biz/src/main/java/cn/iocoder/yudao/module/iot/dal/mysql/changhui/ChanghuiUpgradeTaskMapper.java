package cn.iocoder.yudao.module.iot.dal.mysql.changhui;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiUpgradeTaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 长辉升级任务 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiUpgradeTaskMapper extends BaseMapperX<ChanghuiUpgradeTaskDO> {

    /**
     * 根据测站编码查询进行中的升级任务
     *
     * @param stationCode 测站编码
     * @return 进行中的升级任务
     */
    default ChanghuiUpgradeTaskDO selectInProgressByStationCode(String stationCode) {
        return selectOne(new LambdaQueryWrapperX<ChanghuiUpgradeTaskDO>()
                .eq(ChanghuiUpgradeTaskDO::getStationCode, stationCode)
                .in(ChanghuiUpgradeTaskDO::getStatus, 0, 1) // 待执行或进行中
                .orderByDesc(ChanghuiUpgradeTaskDO::getCreateTime)
                .last("LIMIT 1"));
    }

    /**
     * 根据固件ID查询升级任务数量
     *
     * @param firmwareId 固件ID
     * @return 任务数量
     */
    default Long selectCountByFirmwareId(Long firmwareId) {
        return selectCount(ChanghuiUpgradeTaskDO::getFirmwareId, firmwareId);
    }

    /**
     * 根据任务ID列表查询
     *
     * @param taskIds 任务ID列表
     * @return 任务列表
     */
    default List<ChanghuiUpgradeTaskDO> selectListByIds(Collection<Long> taskIds) {
        return selectList(new LambdaQueryWrapperX<ChanghuiUpgradeTaskDO>()
                .in(ChanghuiUpgradeTaskDO::getId, taskIds));
    }

    /**
     * 根据设备ID查询待执行的升级任务
     *
     * @param deviceId 设备ID
     * @return 待执行的升级任务列表
     */
    default List<ChanghuiUpgradeTaskDO> selectPendingByDeviceId(Long deviceId) {
        return selectList(new LambdaQueryWrapperX<ChanghuiUpgradeTaskDO>()
                .eq(ChanghuiUpgradeTaskDO::getDeviceId, deviceId)
                .eq(ChanghuiUpgradeTaskDO::getStatus, 0) // 只查询待执行状态
                .orderByAsc(ChanghuiUpgradeTaskDO::getCreateTime));
    }

    /**
     * 根据设备ID查询待执行或进行中的升级任务
     *
     * @param deviceId 设备ID
     * @return 待执行或进行中的升级任务
     */
    default ChanghuiUpgradeTaskDO selectPendingOrInProgressByDeviceId(Long deviceId) {
        return selectOne(new LambdaQueryWrapperX<ChanghuiUpgradeTaskDO>()
                .eq(ChanghuiUpgradeTaskDO::getDeviceId, deviceId)
                .in(ChanghuiUpgradeTaskDO::getStatus, 0, 1) // 待执行或进行中
                .orderByDesc(ChanghuiUpgradeTaskDO::getCreateTime)
                .last("LIMIT 1"));
    }

    /**
     * 分页查询升级任务
     *
     * @param stationCode     测站编码（模糊查询）
     * @param firmwareId      固件ID
     * @param status          状态
     * @param createTimeStart 创建时间开始
     * @param createTimeEnd   创建时间结束
     * @param pageNo          页码
     * @param pageSize        每页大小
     * @return 分页结果
     */
    default PageResult<ChanghuiUpgradeTaskDO> selectPage(String stationCode, Long firmwareId,
                                                          Integer status, LocalDateTime createTimeStart,
                                                          LocalDateTime createTimeEnd,
                                                          Integer pageNo, Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam, new LambdaQueryWrapperX<ChanghuiUpgradeTaskDO>()
                .likeIfPresent(ChanghuiUpgradeTaskDO::getStationCode, stationCode)
                .eqIfPresent(ChanghuiUpgradeTaskDO::getFirmwareId, firmwareId)
                .eqIfPresent(ChanghuiUpgradeTaskDO::getStatus, status)
                .geIfPresent(ChanghuiUpgradeTaskDO::getCreateTime, createTimeStart)
                .leIfPresent(ChanghuiUpgradeTaskDO::getCreateTime, createTimeEnd)
                .orderByDesc(ChanghuiUpgradeTaskDO::getId));
    }

    /**
     * 查询超时的待执行升级任务
     *
     * @param timeoutBefore 超时时间点（创建时间早于此时间的任务视为超时）
     * @return 超时的任务列表
     */
    default List<ChanghuiUpgradeTaskDO> selectTimeoutPendingTasks(LocalDateTime timeoutBefore) {
        return selectList(new LambdaQueryWrapperX<ChanghuiUpgradeTaskDO>()
                .eq(ChanghuiUpgradeTaskDO::getStatus, 0) // 待执行状态
                .lt(ChanghuiUpgradeTaskDO::getCreateTime, timeoutBefore));
    }

}
