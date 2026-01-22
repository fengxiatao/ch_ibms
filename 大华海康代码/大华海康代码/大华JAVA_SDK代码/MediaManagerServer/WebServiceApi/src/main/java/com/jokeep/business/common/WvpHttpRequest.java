package com.jokeep.business.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jokeep.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class WvpHttpRequest {
    private static final String ACCOUNT = "admin";
    private static final String PASSWORD = "21232f297a57a5a743894a0e4a801fc3";
    private static final String ROOT = "http://47.104.97.152:18080";
    private static final String USER_LOGIN = "/api/user/login";
    public static List<String> COOKIE = null;

    @Autowired
    RestTemplate restTemplate;

    public <T> T get(String path, Map<String, String> params, Map<String, Object> uriVariables, Class<T> clazz) {
        if (params != null && params.size() > 0) {
            String paramStr = this.buildGetParams(params);
            if (path.endsWith("?")) {
                path += paramStr;
            } else {
                path += "?" + paramStr;
            }
        }
        String url = ROOT + path;
        try {
            ResponseEntity<T> resp = uriVariables==null? restTemplate.getForEntity(url, clazz) :restTemplate.getForEntity(url, clazz, uriVariables);
            T result = resp.getBody();
            if (result instanceof JSONObject) {
                JSONObject json = (JSONObject) result;
                if (json.containsKey("code") && json.getInteger("code") == -1) {
                    this.login(ACCOUNT, PASSWORD);
                    return this.get(path, params, uriVariables, clazz);
                }
            }
            return result;
        } catch (HttpClientErrorException e) {
            JSONObject json = JSON.parseObject(e.getResponseBodyAsString());
            if (json.containsKey("code") && json.getInteger("code") == -1) {
                this.login(ACCOUNT, PASSWORD);
                return this.get(path, params, uriVariables, clazz);
            } else {
                throw e;
            }
        }
    }

    public <T> T post(String path, MultiValueMap<String, Object> data, HttpHeaders headers, Map<String, Object> uriVariables, Class<T> clazz) {
        if (headers == null)
            headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(data, headers);
        String url = ROOT + path;
        try {
             ResponseEntity<T> resp =uriVariables==null?  restTemplate.postForEntity(url, request, clazz): restTemplate.postForEntity(url, request, clazz, uriVariables);
            T result = resp.getBody();
            if (result instanceof JSONObject) {
                JSONObject json = (JSONObject) result;
                if (json.containsKey("code") && json.getInteger("code") == -1) {
                    this.login(ACCOUNT, PASSWORD);
                    return this.post(path, data, headers, uriVariables, clazz);
                }
            }
            return result;
        } catch (HttpClientErrorException e) {
            JSONObject json = JSON.parseObject(e.getResponseBodyAsString());
            if (json.containsKey("code") && json.getInteger("code") == -1) {
                this.login(ACCOUNT, PASSWORD);
                return this.post(path, data, headers, uriVariables, clazz);
            } else {
                throw e;
            }
        }
    }

    private String buildGetParams(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (builder.length() > 0)
                builder.append("&");
            builder.append(entry.getKey() + "=" + entry.getValue());
        }
        return builder.toString();
    }

    private void login(String account, String password) {
        // 设置请求参数
        Map<String, String> params = new HashMap<>();
        params.put("username", account);
        params.put("password", password);
        String url = ROOT + USER_LOGIN + "?" + this.buildGetParams(params);
        ResponseEntity<JSONObject> resp = restTemplate.getForEntity(url, JSONObject.class);
        List<String> cookie = resp.getHeaders().get("Set-Cookie");
        COOKIE = cookie;
    }
}
