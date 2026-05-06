package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessEventLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 门禁事件日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessEventLogMapper extends BaseMapperX<IotAccessEventLogDO> {

    // 正常事件类型列表
    List<String> NORMAL_EVENT_TYPES = Arrays.asList(
        "CARD_SWIPE", "PASSWORD", "FINGERPRINT", "FACE_RECOGNIZE", 
        "QR_CODE", "REMOTE_OPEN", "BUTTON_OPEN"
    );
    
    // 报警事件类型列表
    List<String> ALARM_EVENT_TYPES = Arrays.asList(
        "DOOR_SENSOR_ALARM", "FORCED_OPEN", "DOOR_NOT_CLOSED", 
        "TAMPER_ALARM", "DURESS_ALARM"
    );
    
    // 异常事件类型列表
    List<String> ABNORMAL_EVENT_TYPES = Arrays.asList(
        "CARD_INVALID", "CARD_EXPIRED", "NO_PERMISSION", 
        "TIME_INVALID", "ANTI_PASSBACK", "VERIFY_FAILED"
    );

    default PageResult<IotAccessEventLogDO> selectPage(Long deviceId, Long channelId,
                                                        String eventType, Long personId,
                                                        LocalDateTime startTime, LocalDateTime endTime,
                                                        Integer pageNo, Integer pageSize) {
        cn.iocoder.yudao.framework.common.pojo.PageParam pageParam = new cn.iocoder.yudao.framework.common.pojo.PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam,
                new LambdaQueryWrapperX<IotAccessEventLogDO>()
                .eqIfPresent(IotAccessEventLogDO::getDeviceId, deviceId)
                .eqIfPresent(IotAccessEventLogDO::getChannelId, channelId)
                .eqIfPresent(IotAccessEventLogDO::getEventType, eventType)
                .eqIfPresent(IotAccessEventLogDO::getPersonId, personId)
                .geIfPresent(IotAccessEventLogDO::getEventTime, startTime)
                .leIfPresent(IotAccessEventLogDO::getEventTime, endTime)
                .orderByDesc(IotAccessEventLogDO::getEventTime));
    }

    /**
     * 扩展分页查询，支持事件类别和验证结果筛选
     */
    default PageResult<IotAccessEventLogDO> selectPageEx(Long deviceId, Long channelId,
                                                          String eventType, String eventCategory,
                                                          Long personId, Integer verifyResult,
                                                          LocalDateTime startTime, LocalDateTime endTime,
                                                          Integer pageNo, Integer pageSize) {
        cn.iocoder.yudao.framework.common.pojo.PageParam pageParam = new cn.iocoder.yudao.framework.common.pojo.PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        
        LambdaQueryWrapperX<IotAccessEventLogDO> wrapper = new LambdaQueryWrapperX<IotAccessEventLogDO>()
                .eqIfPresent(IotAccessEventLogDO::getDeviceId, deviceId)
                .eqIfPresent(IotAccessEventLogDO::getChannelId, channelId)
                .eqIfPresent(IotAccessEventLogDO::getEventType, eventType)
                .eqIfPresent(IotAccessEventLogDO::getPersonId, personId)
                .eqIfPresent(IotAccessEventLogDO::getVerifyResult, verifyResult)
                .geIfPresent(IotAccessEventLogDO::getEventTime, startTime)
                .leIfPresent(IotAccessEventLogDO::getEventTime, endTime);
        
        // 根据事件类别筛选
        if (eventCategory != null && !eventCategory.isEmpty() && eventType == null) {
            switch (eventCategory) {
                case "NORMAL":
                    wrapper.in(IotAccessEventLogDO::getEventType, NORMAL_EVENT_TYPES);
                    break;
                case "ALARM":
                    wrapper.in(IotAccessEventLogDO::getEventType, ALARM_EVENT_TYPES);
                    break;
                case "ABNORMAL":
                    wrapper.in(IotAccessEventLogDO::getEventType, ABNORMAL_EVENT_TYPES);
                    break;
            }
        }
        
        wrapper.orderByDesc(IotAccessEventLogDO::getEventTime);
        return selectPage(pageParam, wrapper);
    }

    default List<IotAccessEventLogDO> selectRecentList(Integer limit) {
        return selectList(new LambdaQueryWrapperX<IotAccessEventLogDO>()
                .orderByDesc(IotAccessEventLogDO::getEventTime)
                .last("LIMIT " + limit));
    }

    default List<IotAccessEventLogDO> selectRecentListByDeviceId(Long deviceId, Integer limit) {
        return selectList(new LambdaQueryWrapperX<IotAccessEventLogDO>()
                .eq(IotAccessEventLogDO::getDeviceId, deviceId)
                .orderByDesc(IotAccessEventLogDO::getEventTime)
                .last("LIMIT " + limit));
    }

    // ============================================================
    // M2-D 聚合查询（AccessDashboardServiceImpl 使用）
    // 全部基于 iot_access_event_log 真实表，替代历史 iot_access_record/iot_access_alarm 不存在表
    // ============================================================

    /** 时间区间内通行总次数（含正常/告警/异常） */
    @Select("SELECT COUNT(*) FROM iot_access_event_log WHERE deleted = 0 " +
            "AND event_time BETWEEN #{start} AND #{end}")
    Long countByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 时间区间内告警/异常事件次数 */
    @Select("<script>SELECT COUNT(*) FROM iot_access_event_log WHERE deleted = 0 " +
            "AND event_time BETWEEN #{start} AND #{end} " +
            "AND event_type IN " +
            "<foreach collection='alarmTypes' item='t' open='(' separator=',' close=')'>#{t}</foreach>" +
            "</script>")
    Long countAlarmsByTimeRange(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("alarmTypes") List<String> alarmTypes);

    /**
     * 按"日"聚合通行/告警趋势（用于 week/month/year 视图）
     * 返回字段：date(yyyy-MM-dd) / accessCount / alarmCount / inCount / outCount
     */
    @Select("<script>SELECT " +
            "  DATE(event_time) AS date, " +
            "  COUNT(*) AS accessCount, " +
            "  SUM(CASE WHEN event_type IN " +
            "    <foreach collection='alarmTypes' item='t' open='(' separator=',' close=')'>#{t}</foreach>" +
            "    THEN 1 ELSE 0 END) AS alarmCount, " +
            "  SUM(CASE WHEN direction = 1 THEN 1 ELSE 0 END) AS inCount, " +
            "  SUM(CASE WHEN direction = 2 THEN 1 ELSE 0 END) AS outCount " +
            "FROM iot_access_event_log WHERE deleted = 0 " +
            "AND event_time BETWEEN #{start} AND #{end} " +
            "GROUP BY DATE(event_time) ORDER BY DATE(event_time) ASC" +
            "</script>")
    List<Map<String, Object>> selectDailyTrend(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end,
                                                @Param("alarmTypes") List<String> alarmTypes);

    /**
     * 按"小时"聚合（用于 today 视图 24h 趋势）
     * 返回字段：hour(0-23) / accessCount / alarmCount / inCount / outCount
     */
    @Select("<script>SELECT " +
            "  HOUR(event_time) AS hour, " +
            "  COUNT(*) AS accessCount, " +
            "  SUM(CASE WHEN event_type IN " +
            "    <foreach collection='alarmTypes' item='t' open='(' separator=',' close=')'>#{t}</foreach>" +
            "    THEN 1 ELSE 0 END) AS alarmCount, " +
            "  SUM(CASE WHEN direction = 1 THEN 1 ELSE 0 END) AS inCount, " +
            "  SUM(CASE WHEN direction = 2 THEN 1 ELSE 0 END) AS outCount " +
            "FROM iot_access_event_log WHERE deleted = 0 " +
            "AND event_time BETWEEN #{start} AND #{end} " +
            "GROUP BY HOUR(event_time) ORDER BY HOUR(event_time) ASC" +
            "</script>")
    List<Map<String, Object>> selectHourlyTrend(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end,
                                                 @Param("alarmTypes") List<String> alarmTypes);

    /**
     * 设备 × 小时 热力图（指定日期）
     * 返回字段：deviceId / deviceName / hour / count
     */
    @Select("SELECT device_id AS deviceId, device_name AS deviceName, " +
            "  HOUR(event_time) AS hour, COUNT(*) AS cnt " +
            "FROM iot_access_event_log WHERE deleted = 0 " +
            "AND DATE(event_time) = #{date} " +
            "GROUP BY device_id, device_name, HOUR(event_time)")
    List<Map<String, Object>> selectHeatmap(@Param("date") LocalDate date);

    /**
     * 事件类型分布（用于通行类型饼图）
     * 返回字段：eventType / count
     */
    @Select("SELECT event_type AS eventType, COUNT(*) AS cnt " +
            "FROM iot_access_event_log WHERE deleted = 0 " +
            "AND event_time BETWEEN #{start} AND #{end} " +
            "GROUP BY event_type")
    List<Map<String, Object>> selectEventTypeDistribution(@Param("start") LocalDateTime start,
                                                           @Param("end") LocalDateTime end);

    /** 异常/告警事件最新 N 条 */
    @Select("<script>SELECT * FROM iot_access_event_log WHERE deleted = 0 " +
            "AND event_type IN " +
            "<foreach collection='alarmTypes' item='t' open='(' separator=',' close=')'>#{t}</foreach>" +
            "ORDER BY event_time DESC LIMIT #{limit}" +
            "</script>")
    List<IotAccessEventLogDO> selectAbnormalList(@Param("alarmTypes") List<String> alarmTypes,
                                                  @Param("limit") Integer limit);

}