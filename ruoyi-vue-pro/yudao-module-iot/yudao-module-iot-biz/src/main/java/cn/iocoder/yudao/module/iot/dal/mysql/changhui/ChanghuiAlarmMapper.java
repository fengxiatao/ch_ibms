package cn.iocoder.yudao.module.iot.dal.mysql.changhui;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiAlarmDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 长辉设备报警 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiAlarmMapper extends BaseMapperX<ChanghuiAlarmDO> {

    /**
     * 分页查询报警
     *
     * @param stationCode 测站编码（可选）
     * @param deviceId    设备ID（可选）
     * @param alarmType   报警类型（可选）
     * @param status      状态（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @param pageNo      页码
     * @param pageSize    每页大小
     * @return 分页结果
     */
    default PageResult<ChanghuiAlarmDO> selectPage(String stationCode, Long deviceId, String alarmType,
                                                    Integer status, LocalDateTime startTime, LocalDateTime endTime,
                                                    Integer pageNo, Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam, new LambdaQueryWrapperX<ChanghuiAlarmDO>()
                .eqIfPresent(ChanghuiAlarmDO::getStationCode, stationCode)
                .eqIfPresent(ChanghuiAlarmDO::getDeviceId, deviceId)
                .eqIfPresent(ChanghuiAlarmDO::getAlarmType, alarmType)
                .eqIfPresent(ChanghuiAlarmDO::getStatus, status)
                .geIfPresent(ChanghuiAlarmDO::getAlarmTime, startTime)
                .leIfPresent(ChanghuiAlarmDO::getAlarmTime, endTime)
                .orderByDesc(ChanghuiAlarmDO::getAlarmTime));
    }

    /**
     * 查询报警列表
     *
     * @param stationCode 测站编码（可选）
     * @param deviceId    设备ID（可选）
     * @param alarmType   报警类型（可选）
     * @param status      状态（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @return 报警列表
     */
    default List<ChanghuiAlarmDO> selectList(String stationCode, Long deviceId, String alarmType,
                                              Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<ChanghuiAlarmDO>()
                .eqIfPresent(ChanghuiAlarmDO::getStationCode, stationCode)
                .eqIfPresent(ChanghuiAlarmDO::getDeviceId, deviceId)
                .eqIfPresent(ChanghuiAlarmDO::getAlarmType, alarmType)
                .eqIfPresent(ChanghuiAlarmDO::getStatus, status)
                .geIfPresent(ChanghuiAlarmDO::getAlarmTime, startTime)
                .leIfPresent(ChanghuiAlarmDO::getAlarmTime, endTime)
                .orderByDesc(ChanghuiAlarmDO::getAlarmTime));
    }

    /**
     * 统计未确认报警数量
     *
     * @return 未确认报警数量
     */
    default Long selectUnacknowledgedCount() {
        return selectCount(new LambdaQueryWrapperX<ChanghuiAlarmDO>()
                .eq(ChanghuiAlarmDO::getStatus, 0));
    }

    /**
     * 统计指定设备的未确认报警数量
     *
     * @param stationCode 测站编码
     * @return 未确认报警数量
     */
    default Long selectUnacknowledgedCountByStationCode(String stationCode) {
        return selectCount(new LambdaQueryWrapperX<ChanghuiAlarmDO>()
                .eq(ChanghuiAlarmDO::getStationCode, stationCode)
                .eq(ChanghuiAlarmDO::getStatus, 0));
    }

    /**
     * 删除设备的所有报警
     *
     * @param deviceId 设备ID
     * @return 删除数量
     */
    default int deleteByDeviceId(Long deviceId) {
        return delete(ChanghuiAlarmDO::getDeviceId, deviceId);
    }

    /**
     * 删除设备的所有报警（根据测站编码）
     *
     * @param stationCode 测站编码
     * @return 删除数量
     */
    default int deleteByStationCode(String stationCode) {
        return delete(ChanghuiAlarmDO::getStationCode, stationCode);
    }

}
