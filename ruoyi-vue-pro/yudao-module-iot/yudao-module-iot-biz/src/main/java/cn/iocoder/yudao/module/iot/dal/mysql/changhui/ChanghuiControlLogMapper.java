package cn.iocoder.yudao.module.iot.dal.mysql.changhui;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiControlLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 长辉设备控制日志 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiControlLogMapper extends BaseMapperX<ChanghuiControlLogDO> {

    /**
     * 分页查询控制日志
     *
     * @param stationCode 测站编码（可选）
     * @param deviceId    设备ID（可选）
     * @param controlType 控制类型（可选）
     * @param result      结果（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @param pageNo      页码
     * @param pageSize    每页大小
     * @return 分页结果
     */
    default PageResult<ChanghuiControlLogDO> selectPage(String stationCode, Long deviceId, String controlType,
                                                         Integer result, LocalDateTime startTime, LocalDateTime endTime,
                                                         Integer pageNo, Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam, new LambdaQueryWrapperX<ChanghuiControlLogDO>()
                .eqIfPresent(ChanghuiControlLogDO::getStationCode, stationCode)
                .eqIfPresent(ChanghuiControlLogDO::getDeviceId, deviceId)
                .eqIfPresent(ChanghuiControlLogDO::getControlType, controlType)
                .eqIfPresent(ChanghuiControlLogDO::getResult, result)
                .geIfPresent(ChanghuiControlLogDO::getOperateTime, startTime)
                .leIfPresent(ChanghuiControlLogDO::getOperateTime, endTime)
                .orderByDesc(ChanghuiControlLogDO::getOperateTime));
    }

    /**
     * 查询控制日志列表
     *
     * @param stationCode 测站编码（可选）
     * @param deviceId    设备ID（可选）
     * @param controlType 控制类型（可选）
     * @param result      结果（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @return 控制日志列表
     */
    default List<ChanghuiControlLogDO> selectList(String stationCode, Long deviceId, String controlType,
                                                   Integer result, LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<ChanghuiControlLogDO>()
                .eqIfPresent(ChanghuiControlLogDO::getStationCode, stationCode)
                .eqIfPresent(ChanghuiControlLogDO::getDeviceId, deviceId)
                .eqIfPresent(ChanghuiControlLogDO::getControlType, controlType)
                .eqIfPresent(ChanghuiControlLogDO::getResult, result)
                .geIfPresent(ChanghuiControlLogDO::getOperateTime, startTime)
                .leIfPresent(ChanghuiControlLogDO::getOperateTime, endTime)
                .orderByDesc(ChanghuiControlLogDO::getOperateTime));
    }

    /**
     * 查询最近的控制日志
     *
     * @param stationCode 测站编码
     * @param controlType 控制类型
     * @return 最近的控制日志
     */
    default ChanghuiControlLogDO selectLatest(String stationCode, String controlType) {
        return selectOne(new LambdaQueryWrapperX<ChanghuiControlLogDO>()
                .eq(ChanghuiControlLogDO::getStationCode, stationCode)
                .eq(ChanghuiControlLogDO::getControlType, controlType)
                .orderByDesc(ChanghuiControlLogDO::getOperateTime)
                .last("LIMIT 1"));
    }

    /**
     * 删除设备的所有控制日志
     *
     * @param deviceId 设备ID
     * @return 删除数量
     */
    default int deleteByDeviceId(Long deviceId) {
        return delete(ChanghuiControlLogDO::getDeviceId, deviceId);
    }

    /**
     * 删除设备的所有控制日志（根据测站编码）
     *
     * @param stationCode 测站编码
     * @return 删除数量
     */
    default int deleteByStationCode(String stationCode) {
        return delete(ChanghuiControlLogDO::getStationCode, stationCode);
    }

}
