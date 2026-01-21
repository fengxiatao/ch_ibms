package cn.iocoder.yudao.module.iot.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventHandleReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventStatsVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmEventDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmEventMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmHostMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmZoneMapper;
import cn.iocoder.yudao.module.iot.websocket.AlertWebSocketHandler;
import cn.iocoder.yudao.module.iot.websocket.IotWebSocketHandler;
import cn.iocoder.yudao.module.iot.websocket.message.AlarmEventMessage;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 报警事件 Service 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class IotAlarmEventServiceImpl implements IotAlarmEventService {

    @Resource
    private IotAlarmEventMapper alarmEventMapper;

    @Resource
    private IotAlarmHostMapper alarmHostMapper;

    @Resource
    private IotAlarmZoneMapper alarmZoneMapper;

    @Resource
    private AlertWebSocketHandler alertWebSocketHandler;

    @Resource
    private IotWebSocketHandler iotWebSocketHandler;

    @Override
    public PageResult<IotAlarmEventDO> getEventPage(IotAlarmEventPageReqVO pageReqVO) {
        return alarmEventMapper.selectEventPage(pageReqVO, 
                pageReqVO.getHostId(), 
                pageReqVO.getEventType(), 
                pageReqVO.getIsHandled(), 
                pageReqVO.getCreateTime());
    }

    @Override
    public PageResult<IotAlarmEventRespVO> getEventPageVO(IotAlarmEventPageReqVO pageReqVO) {
        PageResult<IotAlarmEventDO> page = getEventPage(pageReqVO);
        if (page == null || page.getList() == null || page.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), page != null ? page.getTotal() : 0L);
        }

        List<IotAlarmEventDO> list = page.getList();
        Map<Long, String> hostNameMap = buildHostNameMap(list);
        Map<String, String> zoneNameMap = buildZoneNameMap(list);

        List<IotAlarmEventRespVO> voList = list.stream().map(e -> {
            IotAlarmEventRespVO vo = new IotAlarmEventRespVO();
            // 手动映射，避免 Convert 丢失 hostName/zoneName
            vo.setId(e.getId());
            vo.setHostId(e.getHostId());
            vo.setHostName(hostNameMap.get(e.getHostId()));
            vo.setEventCode(e.getEventCode());
            vo.setEventType(e.getEventType());
            vo.setEventLevel(e.getEventLevel());
            vo.setAreaNo(e.getAreaNo());
            vo.setZoneNo(e.getZoneNo());
            if (e.getHostId() != null && e.getZoneNo() != null) {
                vo.setZoneName(zoneNameMap.get(e.getHostId() + "-" + e.getZoneNo()));
            }
            vo.setUserNo(e.getUserNo());
            vo.setSequence(e.getSequence());
            vo.setEventDesc(e.getEventDesc());
            vo.setRawData(e.getRawData());
            vo.setIsNewEvent(e.getIsNewEvent());
            vo.setIsHandled(e.getIsHandled());
            vo.setHandledBy(e.getHandledBy());
            vo.setHandledTime(e.getHandledTime());
            vo.setHandleRemark(e.getHandleRemark());
            vo.setCreateTime(e.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, page.getTotal());
    }

    @Override
    public IotAlarmEventRespVO getEvent(Long id) {
        IotAlarmEventDO e = alarmEventMapper.selectById(id);
        if (e == null) {
            return null;
        }
        Map<Long, String> hostNameMap = buildHostNameMap(List.of(e));
        Map<String, String> zoneNameMap = buildZoneNameMap(List.of(e));
        IotAlarmEventRespVO vo = new IotAlarmEventRespVO();
        vo.setId(e.getId());
        vo.setHostId(e.getHostId());
        vo.setHostName(hostNameMap.get(e.getHostId()));
        vo.setEventCode(e.getEventCode());
        vo.setEventType(e.getEventType());
        vo.setEventLevel(e.getEventLevel());
        vo.setAreaNo(e.getAreaNo());
        vo.setZoneNo(e.getZoneNo());
        if (e.getHostId() != null && e.getZoneNo() != null) {
            vo.setZoneName(zoneNameMap.get(e.getHostId() + "-" + e.getZoneNo()));
        }
        vo.setUserNo(e.getUserNo());
        vo.setSequence(e.getSequence());
        vo.setEventDesc(e.getEventDesc());
        vo.setRawData(e.getRawData());
        vo.setIsNewEvent(e.getIsNewEvent());
        vo.setIsHandled(e.getIsHandled());
        vo.setHandledBy(e.getHandledBy());
        vo.setHandledTime(e.getHandledTime());
        vo.setHandleRemark(e.getHandleRemark());
        vo.setCreateTime(e.getCreateTime());
        return vo;
    }

    @Override
    public List<IotAlarmEventRespVO> getEventListForExport(IotAlarmEventPageReqVO reqVO) {
        List<IotAlarmEventDO> list = alarmEventMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAlarmEventDO>()
                        .eqIfPresent(IotAlarmEventDO::getHostId, reqVO.getHostId())
                        .eqIfPresent(IotAlarmEventDO::getEventType, reqVO.getEventType())
                        .eqIfPresent(IotAlarmEventDO::getEventLevel, reqVO.getEventLevel())
                        .eqIfPresent(IotAlarmEventDO::getIsHandled, reqVO.getIsHandled())
                        .eqIfPresent(IotAlarmEventDO::getAreaNo, reqVO.getAreaNo())
                        .eqIfPresent(IotAlarmEventDO::getZoneNo, reqVO.getZoneNo())
                        .betweenIfPresent(IotAlarmEventDO::getCreateTime, reqVO.getCreateTime())
                        .orderByDesc(IotAlarmEventDO::getCreateTime)
        );
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> hostNameMap = buildHostNameMap(list);
        Map<String, String> zoneNameMap = buildZoneNameMap(list);
        return list.stream().map(e -> {
            IotAlarmEventRespVO vo = new IotAlarmEventRespVO();
            vo.setId(e.getId());
            vo.setHostId(e.getHostId());
            vo.setHostName(hostNameMap.get(e.getHostId()));
            vo.setEventCode(e.getEventCode());
            vo.setEventType(e.getEventType());
            vo.setEventLevel(e.getEventLevel());
            vo.setAreaNo(e.getAreaNo());
            vo.setZoneNo(e.getZoneNo());
            if (e.getHostId() != null && e.getZoneNo() != null) {
                vo.setZoneName(zoneNameMap.get(e.getHostId() + "-" + e.getZoneNo()));
            }
            vo.setUserNo(e.getUserNo());
            vo.setSequence(e.getSequence());
            vo.setEventDesc(e.getEventDesc());
            vo.setRawData(e.getRawData());
            vo.setIsNewEvent(e.getIsNewEvent());
            vo.setIsHandled(e.getIsHandled());
            vo.setHandledBy(e.getHandledBy());
            vo.setHandledTime(e.getHandledTime());
            vo.setHandleRemark(e.getHandleRemark());
            vo.setCreateTime(e.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public IotAlarmEventStatsVO getEventStats() {
        IotAlarmEventStatsVO stats = new IotAlarmEventStatsVO();
        
        // 今日时间范围
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        
        // 1. 统计紧急报警数量（未处理的 CRITICAL 级别）
        Long urgentCount = alarmEventMapper.selectCount(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAlarmEventDO>()
                        .eq(IotAlarmEventDO::getEventLevel, "CRITICAL")
                        .eq(IotAlarmEventDO::getIsHandled, false)
        );
        stats.setUrgentCount(urgentCount);
        
        // 2. 统计今日报警数量（ALARM 类型）
        Long todayAlarmCount = alarmEventMapper.selectCount(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAlarmEventDO>()
                        .eq(IotAlarmEventDO::getEventType, "ALARM")
                        .between(IotAlarmEventDO::getCreateTime, todayStart, todayEnd)
        );
        stats.setTodayCount(todayAlarmCount);
        
        // 3. 统计活跃主机数量（今日有事件的主机）
        List<IotAlarmEventDO> todayEvents = alarmEventMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAlarmEventDO>()
                        .select(IotAlarmEventDO::getHostId)
                        .between(IotAlarmEventDO::getCreateTime, todayStart, todayEnd)
                        .groupBy(IotAlarmEventDO::getHostId)
        );
        stats.setActiveHosts((long) todayEvents.size());
        
        // 4. 计算处理率
        Long totalCount = alarmEventMapper.selectCount();
        Long handledCount = alarmEventMapper.selectCount(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAlarmEventDO>()
                        .eq(IotAlarmEventDO::getIsHandled, true)
        );
        double processedRate = totalCount > 0 ? (handledCount * 100.0 / totalCount) : 0.0;
        stats.setProcessedRate(Math.round(processedRate * 10) / 10.0); // 保留一位小数
        
        // ========== 顶部统计条数据 ==========
        
        // 5. 今日事件总数
        Long todayTotal = alarmEventMapper.selectCount(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAlarmEventDO>()
                        .between(IotAlarmEventDO::getCreateTime, todayStart, todayEnd)
        );
        stats.setTotal(todayTotal);
        
        // 6. 今日报警事件数量（eventType = ALARM）
        stats.setAlarm(todayAlarmCount);
        
        // 7. 今日恢复事件数量（eventType = RESTORE）
        Long todayRestoreCount = alarmEventMapper.selectCount(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAlarmEventDO>()
                        .eq(IotAlarmEventDO::getEventType, "RESTORE")
                        .between(IotAlarmEventDO::getCreateTime, todayStart, todayEnd)
        );
        stats.setRestore(todayRestoreCount);
        
        // 8. 今日其他事件数量（非 ALARM 和 RESTORE）
        Long todayOtherCount = todayTotal - todayAlarmCount - todayRestoreCount;
        stats.setOther(todayOtherCount >= 0 ? todayOtherCount : 0L);
        
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleEvent(IotAlarmEventHandleReqVO reqVO) {
        // 1. 校验事件是否存在
        IotAlarmEventDO event = alarmEventMapper.selectById(reqVO.getId());
        if (event == null) {
            throw new IllegalArgumentException("报警事件不存在");
        }
        
        // 2. 更新事件状态
        IotAlarmEventDO updateObj = IotAlarmEventDO.builder()
                .id(reqVO.getId())
                .isHandled(true)
                .handledBy(String.valueOf(SecurityFrameworkUtils.getLoginUserId()))
                .handledTime(LocalDateTime.now())
                .handleRemark(reqVO.getHandleRemark())
                .build();
        alarmEventMapper.updateById(updateObj);
        
        log.info("[handleEvent][事件处理成功] eventId={}, handledBy={}", 
                reqVO.getId(), updateObj.getHandledBy());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ignoreEvent(Long id, String remark) {
        IotAlarmEventDO event = alarmEventMapper.selectById(id);
        if (event == null) {
            throw new IllegalArgumentException("报警事件不存在");
        }
        String handleRemark = "[IGNORED]" + (remark != null && !remark.isEmpty() ? (" " + remark) : "");
        IotAlarmEventDO updateObj = IotAlarmEventDO.builder()
                .id(id)
                .isHandled(true)
                .handledBy(String.valueOf(SecurityFrameworkUtils.getLoginUserId()))
                .handledTime(LocalDateTime.now())
                .handleRemark(handleRemark)
                .build();
        alarmEventMapper.updateById(updateObj);
        log.info("[ignoreEvent][事件已忽略] eventId={}, handledBy={}", id, updateObj.getHandledBy());
    }

    private Map<Long, String> buildHostNameMap(List<IotAlarmEventDO> events) {
        List<Long> hostIds = events.stream()
                .map(IotAlarmEventDO::getHostId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (hostIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // selectBatchIds 在部分 MyBatis-Plus 版本会提示 deprecated，这里不影响功能，统一走 BaseMapperX 的 selectList 条件查询
        List<IotAlarmHostDO> hosts = alarmHostMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAlarmHostDO>()
                        .in(IotAlarmHostDO::getId, hostIds)
        );
        Map<Long, String> map = new HashMap<>();
        if (hosts != null) {
            for (IotAlarmHostDO h : hosts) {
                map.put(h.getId(), h.getHostName());
            }
        }
        return map;
    }

    private Map<String, String> buildZoneNameMap(List<IotAlarmEventDO> events) {
        // key: hostId-zoneNo
        List<Long> hostIds = events.stream()
                .map(IotAlarmEventDO::getHostId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (hostIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<IotAlarmZoneDO> zones = alarmZoneMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAlarmZoneDO>()
                        .in(IotAlarmZoneDO::getHostId, hostIds)
        );
        Map<String, String> map = new HashMap<>();
        if (zones != null) {
            for (IotAlarmZoneDO z : zones) {
                if (z.getHostId() != null && z.getZoneNo() != null) {
                    map.put(z.getHostId() + "-" + z.getZoneNo(), z.getZoneName());
                }
            }
        }
        return map;
    }

    @Override
    @TenantIgnore // 忽略租户隔离：RocketMQ 消费者调用时，未传递租户上下文
    @Transactional(rollbackFor = Exception.class)
    public void saveEvent(IotAlarmEventDO eventDO) {
        // 1. 保存事件到数据库
        alarmEventMapper.insert(eventDO);
        log.info("[saveEvent][报警事件保存成功] eventId={}, hostId={}, eventType={}, eventCode={}", 
                eventDO.getId(), eventDO.getHostId(), eventDO.getEventType(), eventDO.getEventCode());
        
        // 2. 通过 WebSocket 推送给前端
        try {
            // 查询主机信息，补充主机名称
            IotAlarmHostDO host = alarmHostMapper.selectById(eventDO.getHostId());
            String hostName = host != null ? host.getHostName() : "未知主机";
            
            // 构建推送消息
            AlarmEventMessage message = AlarmEventMessage.builder()
                    .id(eventDO.getId())
                    .hostId(eventDO.getHostId())
                    .hostName(hostName)
                    .eventCode(eventDO.getEventCode())
                    .eventType(eventDO.getEventType())
                    .level(eventDO.getEventLevel())
                    .title(eventDO.getEventDesc())
                    .areaNo(eventDO.getAreaNo())
                    .zoneNo(eventDO.getZoneNo())
                    .isNewEvent(eventDO.getIsNewEvent())
                    .createTime(eventDO.getCreateTime())
                    .build();
            
            // 通过 IotWebSocketHandler 广播给所有在线用户（前端连接的是 /ws/iot）
            iotWebSocketHandler.pushAlarmEvent(message);
            
            // 同时也通过 AlertWebSocketHandler 推送（兼容旧的连接方式）
            AlertWebSocketHandler.WebSocketMessage wsMessage = new AlertWebSocketHandler.WebSocketMessage(
                    "alarm_event", "新报警事件", message);
            alertWebSocketHandler.broadcastMessage(wsMessage);
            
            log.info("[saveEvent][报警事件推送成功] eventId={}", eventDO.getId());
        } catch (Exception e) {
            log.error("[saveEvent][报警事件推送失败] eventId={}", eventDO.getId(), e);
            // 推送失败不影响保存，只记录日志
        }
    }
    
    /**
     * 报警事件 WebSocket 推送消息
     * @deprecated 使用 {@link AlarmEventMessage} 代替
     */
    @lombok.Data
    @lombok.Builder
    @Deprecated
    public static class AlarmEventMessageOld {
        private Long eventId;
        private Long hostId;
        private String hostName;
        private String eventCode;
        private String eventType;
        private String eventLevel;
        private String eventDesc;
        private Integer areaNo;
        private Integer zoneNo;
        private Boolean isNewEvent;
        private LocalDateTime createTime;
    }
}
