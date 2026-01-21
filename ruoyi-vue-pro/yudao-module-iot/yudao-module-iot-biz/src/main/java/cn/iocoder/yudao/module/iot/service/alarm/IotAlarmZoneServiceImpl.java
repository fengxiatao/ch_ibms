package cn.iocoder.yudao.module.iot.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZoneCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZonePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZoneUpdateReqVO;
import cn.iocoder.yudao.module.iot.convert.alarm.IotAlarmZoneConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmPartitionDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmHostMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmPartitionMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmZoneMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 报警主机防区 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotAlarmZoneServiceImpl implements IotAlarmZoneService {

    @Resource
    private IotAlarmZoneMapper alarmZoneMapper;
    
    @Resource
    private IotAlarmHostMapper alarmHostMapper;
    
    @Resource
    private IotAlarmPartitionMapper alarmPartitionMapper;

    @Override
    public Long createAlarmZone(IotAlarmZoneCreateReqVO createReqVO) {
        // 插入
        IotAlarmZoneDO alarmZone = IotAlarmZoneConvert.INSTANCE.convert(createReqVO);
        
        // 如果未指定分区ID，自动分配到该主机的第一个分区
        if (alarmZone.getPartitionId() == null && alarmZone.getHostId() != null) {
            IotAlarmPartitionDO partition = alarmPartitionMapper.selectOne(
                    new LambdaQueryWrapper<IotAlarmPartitionDO>()
                            .eq(IotAlarmPartitionDO::getHostId, alarmZone.getHostId())
                            .orderByAsc(IotAlarmPartitionDO::getPartitionNo)
                            .last("LIMIT 1")
            );
            if (partition != null) {
                alarmZone.setPartitionId(partition.getId());
                log.info("[createAlarmZone][自动分配防区到分区] hostId={}, zoneNo={}, partitionId={}", 
                        alarmZone.getHostId(), alarmZone.getZoneNo(), partition.getId());
            } else {
                log.warn("[createAlarmZone][未找到分区，无法自动分配] hostId={}, zoneNo={}", 
                        alarmZone.getHostId(), alarmZone.getZoneNo());
            }
        }
        
        alarmZoneMapper.insert(alarmZone);
        // 返回
        return alarmZone.getId();
    }

    @Override
    public void updateAlarmZone(IotAlarmZoneUpdateReqVO updateReqVO) {
        // 校验存在
        validateAlarmZoneExists(updateReqVO.getId());
        // 更新
        IotAlarmZoneDO updateObj = IotAlarmZoneConvert.INSTANCE.convert(updateReqVO);
        alarmZoneMapper.updateById(updateObj);
    }

    @Override
    public void deleteAlarmZone(Long id) {
        // 校验存在
        validateAlarmZoneExists(id);
        // 删除
        alarmZoneMapper.deleteById(id);
    }

    private void validateAlarmZoneExists(Long id) {
        if (alarmZoneMapper.selectById(id) == null) {
            throw exception(ALARM_ZONE_NOT_EXISTS);
        }
    }

    @Override
    public IotAlarmZoneDO getAlarmZone(Long id) {
        return alarmZoneMapper.selectById(id);
    }

    @Override
    public List<IotAlarmZoneDO> getAlarmZoneList(Collection<Long> ids) {
        return alarmZoneMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<IotAlarmZoneDO> getAlarmZonePage(IotAlarmZonePageReqVO pageReqVO) {
        return alarmZoneMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotAlarmZoneDO> getZoneListByHostId(Long hostId) {
        return alarmZoneMapper.selectListByHostId(hostId);
    }

    @Override
    public IotAlarmZoneDO getZoneByHostIdAndZoneNo(Long hostId, Integer zoneNo) {
        return alarmZoneMapper.selectByHostIdAndZoneNo(hostId, zoneNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncZones(Long hostId, List<IotAlarmZoneDO> zones) {
        log.info("[syncZones][同步防区] hostId={}, zoneCount={}", hostId, zones.size());
        
        for (IotAlarmZoneDO zone : zones) {
            // 查询是否已存在
            IotAlarmZoneDO existingZone = getZoneByHostIdAndZoneNo(hostId, zone.getZoneNo());
            
            if (existingZone == null) {
                // 新建防区
                zone.setHostId(hostId);
                if (zone.getZoneName() == null) {
                    zone.setZoneName("防区" + zone.getZoneNo());
                }
                
                // 设置所属分区ID（如果未设置，则分配到第一个分区）
                if (zone.getPartitionId() == null) {
                    IotAlarmPartitionDO partition = alarmPartitionMapper.selectOne(
                            new LambdaQueryWrapper<IotAlarmPartitionDO>()
                                    .eq(IotAlarmPartitionDO::getHostId, hostId)
                                    .orderByAsc(IotAlarmPartitionDO::getPartitionNo)
                                    .last("LIMIT 1")
                    );
                    if (partition != null) {
                        zone.setPartitionId(partition.getId());
                        log.info("[syncZones][分配防区到分区] hostId={}, zoneNo={}, partitionId={}", 
                                hostId, zone.getZoneNo(), partition.getId());
                    }
                }
                
                alarmZoneMapper.insert(zone);
                log.info("[syncZones][新建防区] hostId={}, zoneNo={}, zoneName={}, partitionId={}", 
                        hostId, zone.getZoneNo(), zone.getZoneName(), zone.getPartitionId());
            } else {
                // 更新防区状态
                IotAlarmZoneDO updateObj = new IotAlarmZoneDO();
                updateObj.setId(existingZone.getId());
                updateObj.setZoneStatus(zone.getZoneStatus());
                updateObj.setOnlineStatus(zone.getOnlineStatus());
                alarmZoneMapper.updateById(updateObj);
                log.debug("[syncZones][更新防区] hostId={}, zoneNo={}, status={}", 
                        hostId, zone.getZoneNo(), zone.getZoneStatus());
            }
        }
    }

    @Override
    public void updateZoneStatus(Long hostId, Integer zoneNo, String zoneStatus, Integer alarmStatus) {
        IotAlarmZoneDO zone = getZoneByHostIdAndZoneNo(hostId, zoneNo);
        if (zone == null) {
            log.warn("[updateZoneStatus][防区不存在] hostId={}, zoneNo={}", hostId, zoneNo);
            return;
        }

        IotAlarmZoneDO updateObj = new IotAlarmZoneDO();
        updateObj.setId(zone.getId());
        updateObj.setZoneStatus(zoneStatus);
        updateObj.setOnlineStatus(1); // 在线
        
        // 如果是报警状态，更新报警信息
        if (alarmStatus != null && alarmStatus > 0) {
            updateObj.setAlarmCount(zone.getAlarmCount() != null ? zone.getAlarmCount() + 1 : 1);
            updateObj.setLastAlarmTime(LocalDateTime.now());
        }
        
        alarmZoneMapper.updateById(updateObj);
        log.info("[updateZoneStatus][更新防区状态] hostId={}, zoneNo={}, status={}, alarmStatus={}", 
                hostId, zoneNo, zoneStatus, alarmStatus);
    }

    @Override
    public void recordZoneAlarm(Long hostId, Integer zoneNo) {
        IotAlarmZoneDO zone = getZoneByHostIdAndZoneNo(hostId, zoneNo);
        if (zone == null) {
            log.warn("[recordZoneAlarm][防区不存在] hostId={}, zoneNo={}", hostId, zoneNo);
            return;
        }

        IotAlarmZoneDO updateObj = new IotAlarmZoneDO();
        updateObj.setId(zone.getId());
        updateObj.setAlarmCount(zone.getAlarmCount() != null ? zone.getAlarmCount() + 1 : 1);
        updateObj.setLastAlarmTime(LocalDateTime.now());
        
        alarmZoneMapper.updateById(updateObj);
        log.info("[recordZoneAlarm][记录防区报警] hostId={}, zoneNo={}, alarmCount={}", 
                hostId, zoneNo, updateObj.getAlarmCount());
    }

    @Override
    @TenantIgnore // 忽略租户隔离：RocketMQ 消费者调用时，未传递租户上下文
    public void updateZoneStatusByDeviceIdAndZoneNo(Long deviceId, String areaNo, String zoneNo, String status) {
        // 1. 根据 deviceId 查找主机
        IotAlarmHostDO host = alarmHostMapper.selectOne(IotAlarmHostDO::getDeviceId, deviceId);
        if (host == null) {
            log.warn("[updateZoneStatusByDeviceIdAndZoneNo][主机不存在] deviceId={}", deviceId);
            return;
        }
        
        // 2. 查找防区（使用字符串类型的zoneNo）
        IotAlarmZoneDO zone = alarmZoneMapper.selectOne(
                IotAlarmZoneDO::getHostId, host.getId(),
                IotAlarmZoneDO::getZoneNo, zoneNo
        );
        
        if (zone == null) {
            log.warn("[updateZoneStatusByDeviceIdAndZoneNo][防区不存在] hostId={}, zoneNo={}", 
                    host.getId(), zoneNo);
            return;
        }
        
        // 3. 更新防区状态
        IotAlarmZoneDO updateObj = new IotAlarmZoneDO();
        updateObj.setId(zone.getId());
        updateObj.setZoneStatus(status);
        
        // 根据状态更新枚举字段
        if ("ALARM".equals(status)) {
            updateObj.setAlarmStatus(1);  // 报警中
            updateObj.setAlarmCount(zone.getAlarmCount() != null ? zone.getAlarmCount() + 1 : 1);
            updateObj.setLastAlarmTime(LocalDateTime.now());
        } else if ("NORMAL".equals(status)) {
            updateObj.setAlarmStatus(0);  // 正常
        }
        
        alarmZoneMapper.updateById(updateObj);
        
        log.info("[updateZoneStatusByDeviceIdAndZoneNo][防区状态已更新] zoneId={}, deviceId={}, zoneNo={}, status={}", 
                zone.getId(), deviceId, zoneNo, status);
    }
}
