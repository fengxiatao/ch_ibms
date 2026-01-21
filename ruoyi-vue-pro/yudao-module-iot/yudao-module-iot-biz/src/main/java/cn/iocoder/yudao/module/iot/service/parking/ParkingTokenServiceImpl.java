package cn.iocoder.yudao.module.iot.service.parking;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 停车场小程序Token Service 实现类
 * 使用Redis存储Token
 *
 * @author changhui
 */
@Service
@Slf4j
public class ParkingTokenServiceImpl implements ParkingTokenService {

    /**
     * Token前缀
     */
    private static final String TOKEN_PREFIX = "parking:token:";

    /**
     * 用户Token映射前缀
     */
    private static final String USER_TOKEN_PREFIX = "parking:user_token:";

    /**
     * Token有效期（7天）
     */
    private static final long TOKEN_EXPIRE_DAYS = 7;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String createToken(Long userId, String openid) {
        // 生成Token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 删除旧Token（如果存在）
        String oldToken = stringRedisTemplate.opsForValue().get(USER_TOKEN_PREFIX + userId);
        if (oldToken != null) {
            stringRedisTemplate.delete(TOKEN_PREFIX + oldToken);
        }

        // 存储Token -> UserId映射
        stringRedisTemplate.opsForValue().set(
                TOKEN_PREFIX + token,
                String.valueOf(userId),
                TOKEN_EXPIRE_DAYS,
                TimeUnit.DAYS
        );

        // 存储UserId -> Token映射（用于单点登录）
        stringRedisTemplate.opsForValue().set(
                USER_TOKEN_PREFIX + userId,
                token,
                TOKEN_EXPIRE_DAYS,
                TimeUnit.DAYS
        );

        log.info("[createToken] 创建Token成功，userId: {}", userId);
        return token;
    }

    @Override
    public Long validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        String userIdStr = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (userIdStr == null) {
            return null;
        }

        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            log.error("[validateToken] Token解析失败", e);
            return null;
        }
    }

    @Override
    public String refreshToken(String token) {
        Long userId = validateToken(token);
        if (userId == null) {
            return null;
        }

        // 获取openid（这里简化处理，实际可以从Redis或数据库获取）
        String openid = "";

        // 删除旧Token
        deleteToken(token);

        // 创建新Token
        return createToken(userId, openid);
    }

    @Override
    public void deleteToken(String token) {
        Long userId = validateToken(token);
        if (userId != null) {
            stringRedisTemplate.delete(TOKEN_PREFIX + token);
            stringRedisTemplate.delete(USER_TOKEN_PREFIX + userId);
            log.info("[deleteToken] 删除Token成功，userId: {}", userId);
        }
    }
}
