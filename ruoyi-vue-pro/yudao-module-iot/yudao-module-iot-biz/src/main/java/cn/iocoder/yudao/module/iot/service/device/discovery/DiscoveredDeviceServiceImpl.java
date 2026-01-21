package cn.iocoder.yudao.module.iot.service.device.discovery;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.service.device.discovery.dto.DiscoveredDeviceDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDiscoveredDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDiscoveredDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.device.DiscoveredDeviceStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发现设备服务实现
 * 
 * @author 长辉信息科技有限公司
 */
@Service("discoveredDeviceService")
@Slf4j
public class DiscoveredDeviceServiceImpl implements DiscoveredDeviceService {
    
    @Resource
    private IotDiscoveredDeviceMapper discoveredDeviceMapper;
    
    @Override
    @TenantIgnore
    public boolean saveDiscoveredDevice(DiscoveredDeviceDTO device, boolean added) {
        try {
            IotDiscoveredDeviceDO existing = discoveredDeviceMapper.selectByIp(device.getIpAddress());
            
            if (existing != null) {
                if (Boolean.TRUE.equals(existing.getActivated())) {
                    log.debug("[saveDiscoveredDevice][设备已激活，跳过: {}]", device.getIpAddress());
                    return false;
                }
                
                existing.setVendor(device.getVendor());
                existing.setModel(device.getModel());
                existing.setSerialNumber(device.getSerialNumber());
                existing.setDeviceType(device.getDeviceType());
                existing.setFirmwareVersion(device.getFirmwareVersion());
                existing.setDiscoveryTime(device.getDiscoveryTime());
                existing.setDiscoveryMethod(device.getDiscoveryMethod());
                
                discoveredDeviceMapper.updateById(existing);
                
                log.info("[saveDiscoveredDevice][更新发现记录: {} ({}), 上次发现: {}]", 
                    device.getIpAddress(), device.getVendor(), existing.getDiscoveryTime());
                
                return false;
            }
            
            IotDiscoveredDeviceDO record = convertToEntity(device, added);
            discoveredDeviceMapper.insert(record);
            
            log.info("[saveDiscoveredDevice][保存新发现记录: {} ({})]", 
                device.getIpAddress(), device.getVendor());
            
            return true;
            
        } catch (Exception e) {
            log.error("[saveDiscoveredDevice][保存失败: {}]", device.getIpAddress(), e);
            return false;
        }
    }
    
    @Override
    @TenantIgnore
    public List<DiscoveredDeviceDTO> getRecentDiscoveredDevices(Integer hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<IotDiscoveredDeviceDO> records = discoveredDeviceMapper.selectRecentDevices(since);
        return records.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @TenantIgnore
    public List<DiscoveredDeviceDTO> getUnaddedDevices() {
        List<IotDiscoveredDeviceDO> records = discoveredDeviceMapper.selectUnaddedDevices();
        return records.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private IotDiscoveredDeviceDO convertToEntity(DiscoveredDeviceDTO device, boolean added) {
        IotDiscoveredDeviceDO entity = new IotDiscoveredDeviceDO();
        entity.setIpAddress(device.getIpAddress());
        entity.setMac(device.getMac());
        entity.setVendor(device.getVendor());
        entity.setModel(device.getModel());
        entity.setSerialNumber(device.getSerialNumber());
        entity.setDeviceType(device.getDeviceType());
        entity.setFirmwareVersion(device.getFirmwareVersion());
        entity.setDiscoveryMethod(device.getDiscoveryMethod());
        entity.setDiscoveryTime(device.getDiscoveryTime());
        entity.setAdded(added);
        return entity;
    }
    
    private DiscoveredDeviceDTO convertToDTO(IotDiscoveredDeviceDO entity) {
        return DiscoveredDeviceDTO.builder()
            .ipAddress(entity.getIpAddress())
            .mac(entity.getMac())
            .vendor(entity.getVendor())
            .model(entity.getModel())
            .serialNumber(entity.getSerialNumber())
            .deviceType(entity.getDeviceType())
            .firmwareVersion(entity.getFirmwareVersion())
            .discoveryMethod(entity.getDiscoveryMethod())
            .discoveryTime(entity.getDiscoveryTime())
            .build();
    }
    
    @Override
    public void ignoreDevice(Long id, Integer ignoreDays, String reason) {
        try {
            IotDiscoveredDeviceDO device = discoveredDeviceMapper.selectById(id);
            if (device == null) {
                log.warn("[ignoreDevice][设备不存在: {}]", id);
                return;
            }
            
            device.setStatus(DiscoveredDeviceStatusEnum.IGNORED.getStatus());
            device.setIgnoredBy(SecurityFrameworkUtils.getLoginUserId());
            device.setIgnoredTime(LocalDateTime.now());
            device.setIgnoreReason(reason);
            
            if (ignoreDays != null && ignoreDays > 0) {
                device.setIgnoreUntil(LocalDateTime.now().plusDays(ignoreDays));
            } else {
                device.setIgnoreUntil(null);
            }
            
            discoveredDeviceMapper.updateById(device);
            
            log.info("[ignoreDevice][已忽略设备: {}, 操作人: {}, 天数: {}]", 
                device.getIpAddress(), device.getIgnoredBy(), ignoreDays);
            
        } catch (Exception e) {
            log.error("[ignoreDevice][忽略设备失败: {}]", id, e);
        }
    }
    
    @Override
    public void unignoreDevice(Long id) {
        try {
            IotDiscoveredDeviceDO device = discoveredDeviceMapper.selectById(id);
            if (device == null) {
                log.warn("[unignoreDevice][设备不存在: {}]", id);
                return;
            }
            
            device.setStatus(DiscoveredDeviceStatusEnum.DISCOVERED.getStatus());
            device.setIgnoredBy(null);
            device.setIgnoredTime(null);
            device.setIgnoreReason(null);
            device.setIgnoreUntil(null);
            
            discoveredDeviceMapper.updateById(device);
            
            log.info("[unignoreDevice][已取消忽略设备: {}]", device.getIpAddress());
            
        } catch (Exception e) {
            log.error("[unignoreDevice][取消忽略失败: {}]", id, e);
        }
    }
    
    @Override
    public void markAsPending(Long id) {
        try {
            IotDiscoveredDeviceDO device = discoveredDeviceMapper.selectById(id);
            if (device == null) {
                log.warn("[markAsPending][设备不存在: {}]", id);
                return;
            }
            
            device.setStatus(DiscoveredDeviceStatusEnum.PENDING.getStatus());
            discoveredDeviceMapper.updateById(device);
            
            log.info("[markAsPending][标记为待处理: {}]", device.getIpAddress());
            
        } catch (Exception e) {
            log.error("[markAsPending][标记失败: {}]", id, e);
        }
    }
    
    @Override
    @TenantIgnore
    public void markAsActivated(String ip, Long deviceId) {
        try {
            IotDiscoveredDeviceDO device = discoveredDeviceMapper.selectByIp(ip);
            if (device == null) {
                log.warn("[markAsActivated][设备记录不存在: ip={}]", ip);
                return;
            }
            
            device.setActivated(true);
            device.setActivatedDeviceId(deviceId);
            device.setActivatedTime(LocalDateTime.now());
            device.setActivatedBy(SecurityFrameworkUtils.getLoginUserId());
            device.setStatus(DiscoveredDeviceStatusEnum.ACTIVATED.getStatus());
            
            discoveredDeviceMapper.updateById(device);
            
            log.info("[markAsActivated][已标记设备为激活: ip={}, deviceId={}]", ip, deviceId);
            
        } catch (Exception e) {
            log.error("[markAsActivated][标记失败: ip={}]", ip, e);
        }
    }
    
    @Override
    @TenantIgnore
    public List<DiscoveredDeviceDTO> getUnactivatedDevices() {
        List<IotDiscoveredDeviceDO> records = discoveredDeviceMapper.selectUnactivatedDevices();
        return records.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}
