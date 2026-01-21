package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.alarm.AccessAlarmPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.AccessAlarmDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AccessAlarmMapper extends BaseMapperX<AccessAlarmDO> {

    default PageResult<AccessAlarmDO> selectPage(AccessAlarmPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AccessAlarmDO>()
                .eqIfPresent(AccessAlarmDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(AccessAlarmDO::getAlarmLevel, reqVO.getAlarmLevel())
                .eqIfPresent(AccessAlarmDO::getHandleStatus, reqVO.getHandleStatus())
                .betweenIfPresent(AccessAlarmDO::getAlarmTime, reqVO.getAlarmTime())
                .orderByDesc(AccessAlarmDO::getId));
    }

    /**
     * 查询告警类型统计（按告警类型分组）
     */
    @Select("<script>" +
            "SELECT " +
            "  alarm_type as alarmType, " +
            "  COUNT(*) as count " +
            "FROM iot_access_alarm " +
            "WHERE deleted = 0 " +
            "  <if test='startTime != null'> AND alarm_time &gt;= #{startTime} </if>" +
            "  <if test='endTime != null'> AND alarm_time &lt;= #{endTime} </if>" +
            "GROUP BY alarm_type " +
            "ORDER BY count DESC" +
            "</script>")
    List<Map<String, Object>> selectAlarmTypeStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

}

