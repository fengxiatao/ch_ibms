package com.jokeep.context;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jokeep.enhance.SessionExtend;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.List;

@Log4j2
public class RequestContextKit {
    private static ThreadLocal<RequestContext> requestThread = new ThreadLocal<>();

    public static HttpServletRequest getRequest() {
        return requestThread.get().getRequest();
    }

    public static HttpServletResponse getResponse() {
        return requestThread.get().getResponse();
    }

    public static JSONObject getRequestParam() {
        return requestThread.get().getRequestParam();
    }

    public static JSONObject getRequestHeader() {
        return requestThread.get().getRequestHeader();
    }

    public static List<MultipartFile> getMultipartFiles() {
        return requestThread.get().getMultipartFiles();
    }

    public static <T> T getRequestBody() {
        return requestThread.get().getRequestBody();
    }

    public static void begin(ServletRequest req, ServletResponse res) {
        RequestContext context = new RequestContext((HttpServletRequest) req, (HttpServletResponse) res);
        requestThread.set(context);
    }

    public static void begin(ServletRequest req, ServletResponse res, String[] fields) {
        RequestContext context = new RequestContext((HttpServletRequest) req, (HttpServletResponse) res);
        requestThread.set(context);
        Object aesKeyObj = RequestContextKit.getLoginUserInfo("__AesKey");
        String aesKey = aesKeyObj == null ? "" : aesKeyObj.toString();
        //如果加密，处理解密
        context.paramDecrypt(fields, aesKey);
        //如果进行了加密解密处理，将解密后的数据赋值给BodyReaderHttpServletRequestWrapper
        if (req instanceof ReadHttpServletRequestWrapper) {
            String json = JSON.toJSONString(getRequestBody());
            ((ReadHttpServletRequestWrapper) req).setCacheBody(json.getBytes(Charset.forName("UTF-8")));
        }
    }

    public static void begin(ServletRequest req, ServletResponse res, String allDataFields) {
        RequestContext context = new RequestContext((HttpServletRequest) req, (HttpServletResponse) res);
        requestThread.set(context);
        Object aesKeyObj = RequestContextKit.getLoginUserInfo("__AesKey");
        String aesKey = aesKeyObj == null ? "" : aesKeyObj.toString();
        //如果加密，处理解密
        context.paramDecrypt(allDataFields, aesKey);
        //如果进行了加密解密处理，将解密后的数据赋值给BodyReaderHttpServletRequestWrapper
        if (req instanceof ReadHttpServletRequestWrapper) {
            String json = JSON.toJSONString(getRequestBody());
            ((ReadHttpServletRequestWrapper) req).setCacheBody(json.getBytes(Charset.forName("UTF-8")));
        }
    }

    public static void end() {
        requestThread.remove();
    }

    public static boolean checkLogin() {
        return null != getLoginUser();
    }

    public static Object getLoginUserInfo(String key) {
        if (null != getLoginUser()) {
            JSONObject loginUser = getLoginUser();
            return loginUser.get(key);
        }
        return null;
    }

    public static JSONObject getLoginUser() {
        JSONObject user = requestThread.get().getUserSession();
        if (user == null) {
            SessionExtend sessionExtend = SpringContextKit.getBean(SessionExtend.class);
            user = sessionExtend.getLoginUserSession();
            requestThread.get().setUserSession(user);
        }
        return user;
    }

    public static void setLoginUserInfo(JSONObject user) {
        SessionExtend sessionExtend = SpringContextKit.getBean(SessionExtend.class);
        sessionExtend.setLoginUserInfo(user);
        requestThread.get().setUserSession(user);
    }

    public static void removeSession(String key, SessionStatus sessionStatus) {
        SessionExtend sessionExtend = SpringContextKit.getBean(SessionExtend.class);
        sessionExtend.removeSession(key, sessionStatus);
    }

    public static String getSessionId() {
        JSONObject header = getRequestHeader();
        return header.containsKey("f_sessionid") ? header.getString("f_sessionid") : "";
    }

    public static String getRealIp() {
        HttpServletRequest request = getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }
}
