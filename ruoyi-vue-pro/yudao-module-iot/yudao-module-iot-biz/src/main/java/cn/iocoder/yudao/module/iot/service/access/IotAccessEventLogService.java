package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessEventLogDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁事件日志 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAccessEventLogService {

    /**
     * 保存事件日志
     *
     * @param eventLog 事件日志
     * @return 事件ID
     */
    Long saveEventLog(IotAccessEventLogDO eventLog);

    /**
     * 获取事件日志
     *
     * @param id 事件ID
     * @return 事件日志
     */
    IotAccessEventLogDO getEventLog(Long id);

    /**
     * 获取事件日志分页
     *
     * @param deviceId    设备ID
     * @param channelId   通道ID
     * @param eventType   事件类型
     * @param personId    人员ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param pageNo      页码
     * @param pageSize    每页大小
     * @return 事件日志分页
     */
    PageResult<IotAccessEventLogDO> getEventLogPage(Long deviceId, Long channelId, String eventType,
                                                     Long personId, LocalDateTime startTime, LocalDateTime endTime,
                                                     Integer pageNo, Integer pageSize);

    /**
     * 获取事件日志分页（扩展版，支持更多筛选条件）
     *
     * @param deviceId      设备ID
     * @param channelId     通道ID
     * @param eventType     事件类型
     * @param eventCategory 事件类别（NORMAL/ALARM/ABNORMAL）
     * @param personId      人员ID
     * @param verifyResult  验证结果
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param pageNo        页码
     * @param pageSize      每页大小
     * @return 事件日志分页
     */
    PageResult<IotAccessEventLogDO> getEventLogPageEx(Long deviceId, Long channelId, String eventType,
                                                       String eventCategory, Long personId, Integer verifyResult,
                                                       LocalDateTime startTime, LocalDateTime endTime,
                                                       Integer pageNo, Integer pageSize);

    /**
     * 获取最近事件
     *
     * @param limit 数量限制
     * @return 事件列表
     */
    List<IotAccessEventLogDO> getRecentEvents(Integer limit);

    /**
     * 获取设备最近事件
     *
     * @param deviceId 设备ID
     * @param limit    数量限制
     * @return 事件列表
     */
    List<IotAccessEventLogDO> getDeviceRecentEvents(Long deviceId, Integer limit);

    /**
     * 记录事件
     *
     * @param deviceId       设备ID
     * @param deviceName     设备名称
     * @param channelId      通道ID
     * @param channelName    通道名称
     * @param eventType      事件类型
     * @param eventDesc      事件描述
     * @param personId       人员ID
     * @param personName     人员姓名
     * @param credentialType 凭证类型
     * @param credentialData 凭证数据
     * @param success        是否成功
     * @param failReason     失败原因
     * @return 事件ID
     */
    Long recordEvent(Long deviceId, String deviceName, Long channelId, String channelName,
                     String eventType, String eventDesc, Long personId, String personName,
                     String credentialType, String credentialData, Boolean success, String failReason);

    /**
     * 获取今日事件统计
     * Requirements: 7.1 - 今日总事件数、报警数、正常数、异常数
     *
     * @return 统计数据 [total, alarmCount, normalCount, abnormalCount]
     */
    EventStatistics getTodayStatistics();

    /**
     * 事件统计数据
     */
    record EventStatistics(long total, long alarmCount, long normalCount, long abnormalCount) {}

}
