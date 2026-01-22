package com.jokeep.framework.extend;

import com.alibaba.fastjson.JSONObject;
import com.jokeep.context.RequestContextKit;
import com.jokeep.context.SpringContextKit;
import com.jokeep.enhance.SessionExtend;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Component
public class SessionExtendImpl implements SessionExtend {
    @Override
    public JSONObject getLoginUserSession() {
        JSONObject loginUser;
        HttpSession session = RequestContextKit.getRequest().getSession(false);
        Object user = session == null ? null : session.getAttribute("loginUser");
        if (null != user) {
            loginUser = (JSONObject) user;
        } else {
            String sessionId = getSessionId();
            if (StringUtils.isNotEmpty(sessionId)) {
                IUserSessionService sysUserService = SpringContextKit.getBean(IUserSessionService.class);
                loginUser = sysUserService.userLoginSession(sessionId);
            } else {
                loginUser = null;
            }
        }
        return loginUser;
    }

    @Override
    public void setLoginUserInfo(Map<String, Object> user) {
        if (!(user instanceof JSONObject))
            user = new JSONObject(user);
        RequestContextKit.getRequest().getSession(true).setAttribute("loginUser", user);
    }

    @Override
    public void removeSession(String key, SessionStatus sessionStatus) {
        HttpSession session = RequestContextKit.getRequest().getSession();
        if (null != session) {
            session.removeAttribute(key);
            sessionStatus.setComplete();
        }
        IUserSessionService sysUserService = SpringContextKit.getBean(IUserSessionService.class);
        sysUserService.deleteUserLoginSession(getSessionId());
    }

    @Override
    public String getSessionId() {
        return RequestContextKit.getSessionId();
    }
}
