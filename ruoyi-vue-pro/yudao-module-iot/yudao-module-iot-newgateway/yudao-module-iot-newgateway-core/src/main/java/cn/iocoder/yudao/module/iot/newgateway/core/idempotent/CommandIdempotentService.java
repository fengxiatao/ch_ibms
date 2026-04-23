package cn.iocoder.yudao.module.iot.newgateway.core.idempotent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 命令幂等服务
 * <p>
 * 基于 Redis 实现分布式幂等保护，防止消息重复消费导致命令重复下发。
 * 同时提供本地缓存兜底，避免 Redis 故障时完全失去幂等保护。
 * </p>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>消息队列重复投递</li>
 *     <li>消费者重试机制</li>
 *     <li>网络抖动导致的重复请求</li>
 * </ul>
 *
 * <p>设计原则：</p>
 * <ul>
 *     <li>Redis 优先：正常情况下使用 Redis 分布式锁</li>
 *     <li>本地兜底：Redis 不可用时降级到本地缓存</li>
 *     <li>快速失败：不阻塞，只做标记检查</li>
 * </ul>
 *
 * @author IoT Gateway Team
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CommandIdempotentService {

    /**
     * Redis Key 前缀
     */
    private static final String KEY_PREFIX = "iot:gateway:cmd:idempotent:";

    /**
     * 默认幂等过期时间（60秒）
     * <p>
     * 设计考虑：
     * - 命令执行时间一般在 5 秒内
     * - 60 秒足够覆盖超时重试场景
     * - 不宜过长，避免内存占用
     * </p>
     */
    private static final Duration DEFAULT_TTL = Duration.ofSeconds(60);

    /**
     * 本地缓存最大容量（防止 OOM）
     */
    private static final int LOCAL_CACHE_MAX_SIZE = 10000;

    /**
     * Redis 模板（可能为 null，如果未配置 Redis）
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 本地缓存（兜底 + 二级去重）
     * <p>
     * Key: requestId, Value: 过期时间戳
     * </p>
     */
    private final ConcurrentMap<String, Long> localCache = new ConcurrentHashMap<>();

    /**
     * 尝试获取幂等锁
     * <p>
     * 如果返回 true，表示这是首次处理该命令，可以继续执行。
     * 如果返回 false，表示该命令已被处理过或正在处理中，应跳过。
     * </p>
     *
     * @param requestId 请求ID（全局唯一）
     * @return true=可以执行，false=重复命令
     */
    public boolean tryAcquire(String requestId) {
        return tryAcquire(requestId, DEFAULT_TTL);
    }

    /**
     * 尝试获取幂等锁（自定义过期时间）
     *
     * @param requestId 请求ID
     * @param ttl       过期时间
     * @return true=可以执行，false=重复命令
     */
    public boolean tryAcquire(String requestId, Duration ttl) {
        if (requestId == null || requestId.isEmpty()) {
            log.warn("[Idempotent] requestId 为空，跳过幂等检查");
            return true; // requestId 为空时不做幂等检查，允许执行
        }

        // 1. 先检查本地缓存（快速去重）
        if (isInLocalCache(requestId)) {
            log.debug("[Idempotent] 本地缓存命中，重复命令: requestId={}", requestId);
            return false;
        }

        // 2. 尝试 Redis 分布式锁
        boolean acquired = tryRedisAcquire(requestId, ttl);

        // 3. 获取成功后，同步更新本地缓存
        if (acquired) {
            addToLocalCache(requestId, ttl);
        }

        return acquired;
    }

    /**
     * 释放幂等锁（可选，用于命令执行失败时允许重试）
     * <p>
     * 注意：正常情况下不需要调用此方法，锁会自动过期。
     * 仅在需要允许重试的场景下使用。
     * </p>
     *
     * @param requestId 请求ID
     */
    public void release(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            return;
        }

        // 从本地缓存移除
        localCache.remove(requestId);

        // 从 Redis 移除
        try {
            if (stringRedisTemplate != null) {
                String key = KEY_PREFIX + requestId;
                stringRedisTemplate.delete(key);
                log.debug("[Idempotent] 幂等锁已释放: requestId={}", requestId);
            }
        } catch (Exception e) {
            log.warn("[Idempotent] 释放 Redis 幂等锁失败: requestId={}", requestId, e);
        }
    }

    /**
     * 尝试获取 Redis 分布式锁
     */
    private boolean tryRedisAcquire(String requestId, Duration ttl) {
        try {
            if (stringRedisTemplate == null) {
                log.debug("[Idempotent] Redis 未配置，降级到本地缓存");
                return true; // Redis 未配置时，由本地缓存负责
            }

            String key = KEY_PREFIX + requestId;
            Boolean success = stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, "1", ttl);

            if (Boolean.TRUE.equals(success)) {
                log.debug("[Idempotent] Redis 幂等锁获取成功: requestId={}", requestId);
                return true;
            } else {
                log.info("[Idempotent] Redis 幂等锁已存在，重复命令: requestId={}", requestId);
                return false;
            }
        } catch (Exception e) {
            log.warn("[Idempotent] Redis 幂等检查异常，降级到本地缓存: requestId={}", requestId, e);
            // Redis 异常时，本地缓存已经在调用方做过检查了
            return true;
        }
    }

    /**
     * 检查是否在本地缓存中
     */
    private boolean isInLocalCache(String requestId) {
        Long expireTime = localCache.get(requestId);
        if (expireTime == null) {
            return false;
        }

        // 检查是否过期
        if (System.currentTimeMillis() > expireTime) {
            localCache.remove(requestId);
            return false;
        }

        return true;
    }

    /**
     * 添加到本地缓存
     */
    private void addToLocalCache(String requestId, Duration ttl) {
        // 清理过期条目（简单实现，避免内存泄漏）
        if (localCache.size() >= LOCAL_CACHE_MAX_SIZE) {
            cleanExpiredEntries();
        }

        long expireTime = System.currentTimeMillis() + ttl.toMillis();
        localCache.put(requestId, expireTime);
    }

    /**
     * 清理过期条目
     */
    private void cleanExpiredEntries() {
        long now = System.currentTimeMillis();
        localCache.entrySet().removeIf(entry -> entry.getValue() < now);

        // 如果清理后仍然超过容量，强制清理一半（LRU 简化版）
        if (localCache.size() >= LOCAL_CACHE_MAX_SIZE) {
            int removeCount = LOCAL_CACHE_MAX_SIZE / 2;
            localCache.keySet().stream()
                    .limit(removeCount)
                    .forEach(localCache::remove);
            log.warn("[Idempotent] 本地缓存强制清理: removed={}", removeCount);
        }
    }

    /**
     * 获取当前缓存统计（用于监控）
     *
     * @return 本地缓存条目数
     */
    public int getLocalCacheSize() {
        return localCache.size();
    }
}


