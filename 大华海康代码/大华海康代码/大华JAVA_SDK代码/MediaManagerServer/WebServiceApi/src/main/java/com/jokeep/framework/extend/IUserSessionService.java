package com.jokeep.framework.extend;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

public interface IUserSessionService {
    @Cacheable(value = "myCache", key = "#sessionId")
    JSONObject userLoginSession(String sessionId);

    @CacheEvict(value = "myCache", key = "#sessionId")
    void deleteUserLoginSession(String sessionId);
}
