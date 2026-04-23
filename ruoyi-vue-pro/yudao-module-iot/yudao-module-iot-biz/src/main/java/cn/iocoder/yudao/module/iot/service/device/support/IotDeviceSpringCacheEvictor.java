package cn.iocoder.yudao.module.iot.service.device.support;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * 与 {@link cn.iocoder.yudao.module.iot.service.device.IotDeviceServiceImpl} 中 Spring Cache 约定一致，用于在绕过 Service 直接改库时清理设备缓存。
 */
@Component
public class IotDeviceSpringCacheEvictor {

    @Resource
    private CacheManager cacheManager;

    public void evictIotDeviceCaches(IotDeviceDO device) {
        if (device == null) {
            return;
        }
        Cache cache = cacheManager.getCache(RedisKeyConstants.DEVICE);
        if (cache == null) {
            return;
        }
        if (device.getId() != null) {
            cache.evict(device.getId());
        }
        if (StrUtil.isNotBlank(device.getProductKey()) && StrUtil.isNotBlank(device.getDeviceName())) {
            cache.evict(device.getProductKey() + "_" + device.getDeviceName());
        }
        if (StrUtil.isNotBlank(device.getDeviceKey())) {
            cache.evict("key_" + device.getDeviceKey());
        }
    }
}
