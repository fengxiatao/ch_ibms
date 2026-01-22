package com.jokeep.context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jokeep.utils.AESKit;
import com.jokeep.utils.StrKit;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Log4j2
public class RequestContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JSONObject requestParam = null;
    private JSONObject requestHeader = null;
    private Object requestBody = null;
    private List<MultipartFile> multipartFiles = null;
    private JSONObject userSession = null;

    public RequestContext(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.init();
    }

    public void paramDecrypt(String allDataFields, String aesKey) {
        this.decryptParams(new String[]{allDataFields}, aesKey);
    }

    public void paramDecrypt(String[] fields, String aesKey) {
        this.decryptParams(fields, aesKey);
    }

    public HttpServletRequest getRequest() {
        return this.request;
    }

    public HttpServletResponse getResponse() {
        return this.response;
    }

    public JSONObject getRequestParam() {
        return this.requestParam;
    }

    public JSONObject getRequestHeader() {
        return this.requestHeader;
    }

    public List<MultipartFile> getMultipartFiles() {
        return this.multipartFiles;
    }

    public <T> T getRequestBody() {
        if (this.requestBody == null)
            return (T) new JSONObject();
        else
            return (T) this.requestBody;
    }

    public void setUserSession(JSONObject userSession) {
        this.userSession = userSession;
    }

    public JSONObject getUserSession() {
        return this.userSession;
    }


    private void init() {
        this.requestParam = getParameters(this.getRequest());
        this.requestHeader = getHeaderParameters(this.getRequest());
        this.requestBody = getStreamParameters(this.getRequest());
        this.multipartFiles = getAllMultipartFiles(this.getRequest());
    }

    private static JSONObject getHeaderParameters(HttpServletRequest request) {
        Enumeration headerNames = request.getHeaderNames();
        JSONObject result = new JSONObject();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            result.put(key, value);
        }
        return result;
    }

    private static JSONObject getParameters(HttpServletRequest request) {
        Enumeration enu = request.getParameterNames();
        JSONObject result = new JSONObject();
        while (enu.hasMoreElements()) {
            String paraName = (String) enu.nextElement();
            result.put(paraName, request.getParameter(paraName));
        }
        return result;
    }

    private static List<MultipartFile> getAllMultipartFiles(ServletRequest request) {
        List<MultipartFile> result = new ArrayList<>();
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("multipart/form-data")) {
            HttpServletRequest req = (HttpServletRequest) request;
            MultipartResolver res = new CommonsMultipartResolver(request.getServletContext());
            if (res.isMultipart(req)) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) req;
                Map<String, MultipartFile> files = multipartRequest.getFileMap();
                Iterator<String> iterator = files.keySet().iterator();
                while (iterator.hasNext()) {
                    String formKey = (String) iterator.next();
                    MultipartFile multipartFile = multipartRequest.getFile(formKey);
                    result.add(multipartFile);
                }
            }
        }
        return result;
    }

    private static Object getStreamParameters(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            //流参数
            InputStream stream = getInputStream(request);
            if (stream != null) {
                try {
                    String jsonStr = StrKit.streamToString(stream);
                    if (jsonStr != null && !jsonStr.equals("")) {
                        return JSON.parse(jsonStr);
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        return null;
    }

    private static InputStream getInputStream(ServletRequest request) {
        InputStream stream = null;
        try {
            stream = request.getInputStream();
        } catch (IOException e) {
            log.error(e);
        }
        return stream;
    }

    private void decryptRequestBody(JSONObject params, String field, String aesKey) {
        if (params != null && params.size() > 0 && params.containsKey(field)) {
            String dataStr = params.getString("__data");
            Object result = null;
            //解密data数据
            try {
                dataStr = AESKit.aesDecrypt(dataStr, aesKey);
                result = JSON.parse(dataStr);
            } catch (Exception e) {
                log.error(e);
            }
            //如果参数名为__data，表示requestBody的所有参数通过加密提交的，解密后赋值给requestBody
            if (field.equals("__data"))
                this.requestBody = result;
            else
                params.put(field, result);
        }
    }

    private void decryptParams(String[] fields, String aesKey) {
        if (fields != null && fields.length > 0) {
            for (String field : fields) {
                this.decryptRequestBody(this.requestParam, field, aesKey);
                this.decryptRequestBody(this.requestHeader, field, aesKey);
                if (this.requestBody instanceof JSONObject)
                    this.decryptRequestBody((JSONObject) this.requestBody, field, aesKey);
            }
        }
    }
}
