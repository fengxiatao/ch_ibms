package cn.iocoder.yudao.module.iot.dal.mysql.changhui;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiDataDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 长辉设备数据 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiDataMapper extends BaseMapperX<ChanghuiDataDO> {

    /**
     * 查询最新的单个指标数据
     *
     * @param stationCode 测站编码
     * @param indicator   指标类型
     * @return 最新数据
     */
    default ChanghuiDataDO selectLatestByStationCodeAndIndicator(String stationCode, String indicator) {
        return selectOne(new LambdaQueryWrapperX<ChanghuiDataDO>()
                .eq(ChanghuiDataDO::getStationCode, stationCode)
                .eq(ChanghuiDataDO::getIndicator, indicator)
                .orderByDesc(ChanghuiDataDO::getTimestamp)
                .last("LIMIT 1"));
    }

    /**
     * 查询最新的多个指标数据
     *
     * @param stationCode 测站编码
     * @param indicators  指标类型列表
     * @return 最新数据列表
     */
    default List<ChanghuiDataDO> selectLatestByStationCodeAndIndicators(String stationCode, List<String> indicators) {
        // 使用子查询获取每个指标的最新数据
        // 由于MyBatis-Plus不支持复杂子查询，这里使用简单方式：查询每个指标的最新记录
        return selectList(new LambdaQueryWrapperX<ChanghuiDataDO>()
                .eq(ChanghuiDataDO::getStationCode, stationCode)
                .in(ChanghuiDataDO::getIndicator, indicators)
                .orderByDesc(ChanghuiDataDO::getTimestamp));
    }

    /**
     * 查询设备所有指标的最新数据
     *
     * @param stationCode 测站编码
     * @return 最新数据列表
     */
    default List<ChanghuiDataDO> selectLatestByStationCode(String stationCode) {
        return selectList(new LambdaQueryWrapperX<ChanghuiDataDO>()
                .eq(ChanghuiDataDO::getStationCode, stationCode)
                .orderByDesc(ChanghuiDataDO::getTimestamp));
    }

    /**
     * 查询设备所有指标的最新数据（根据设备ID）
     *
     * @param deviceId 设备ID
     * @return 最新数据列表
     */
    default List<ChanghuiDataDO> selectLatestByDeviceId(Long deviceId) {
        return selectList(new LambdaQueryWrapperX<ChanghuiDataDO>()
                .eq(ChanghuiDataDO::getDeviceId, deviceId)
                .orderByDesc(ChanghuiDataDO::getTimestamp));
    }

    /**
     * 分页查询历史数据
     *
     * @param stationCode 测站编码（可选）
     * @param deviceId    设备ID（可选）
     * @param indicator   指标类型（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @param pageNo      页码
     * @param pageSize    每页大小
     * @return 分页结果
     */
    default PageResult<ChanghuiDataDO> selectPage(String stationCode, Long deviceId, String indicator,
                                                   LocalDateTime startTime, LocalDateTime endTime,
                                                   Integer pageNo, Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam, new LambdaQueryWrapperX<ChanghuiDataDO>()
                .eqIfPresent(ChanghuiDataDO::getStationCode, stationCode)
                .eqIfPresent(ChanghuiDataDO::getDeviceId, deviceId)
                .eqIfPresent(ChanghuiDataDO::getIndicator, indicator)
                .geIfPresent(ChanghuiDataDO::getTimestamp, startTime)
                .leIfPresent(ChanghuiDataDO::getTimestamp, endTime)
                .orderByDesc(ChanghuiDataDO::getTimestamp));
    }

    /**
     * 查询历史数据列表（不分页，用于导出）
     *
     * @param stationCode 测站编码（可选）
     * @param deviceId    设备ID（可选）
     * @param indicator   指标类型（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @return 数据列表
     */
    default List<ChanghuiDataDO> selectList(String stationCode, Long deviceId, String indicator,
                                             LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<ChanghuiDataDO>()
                .eqIfPresent(ChanghuiDataDO::getStationCode, stationCode)
                .eqIfPresent(ChanghuiDataDO::getDeviceId, deviceId)
                .eqIfPresent(ChanghuiDataDO::getIndicator, indicator)
                .geIfPresent(ChanghuiDataDO::getTimestamp, startTime)
                .leIfPresent(ChanghuiDataDO::getTimestamp, endTime)
                .orderByDesc(ChanghuiDataDO::getTimestamp));
    }

    /**
     * 删除设备的所有数据
     *
     * @param deviceId 设备ID
     * @return 删除数量
     */
    default int deleteByDeviceId(Long deviceId) {
        return delete(ChanghuiDataDO::getDeviceId, deviceId);
    }

    /**
     * 删除设备的所有数据（根据测站编码）
     *
     * @param stationCode 测站编码
     * @return 删除数量
     */
    default int deleteByStationCode(String stationCode) {
        return delete(ChanghuiDataDO::getStationCode, stationCode);
    }

}
