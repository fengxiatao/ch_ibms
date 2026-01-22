package com.jokeep.enhance;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.support.SessionStatus;

import java.util.Map;

public interface SessionExtend {
    JSONObject getLoginUserSession();

    void setLoginUserInfo(Map<String, Object> user);

    void removeSession(String key, SessionStatus sessionStatus);

    String getSessionId();
}
