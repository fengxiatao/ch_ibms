package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessEventLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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

}