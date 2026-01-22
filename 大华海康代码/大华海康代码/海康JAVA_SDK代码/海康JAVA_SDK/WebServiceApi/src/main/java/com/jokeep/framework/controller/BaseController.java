package com.jokeep.framework.controller;

import com.alibaba.fastjson.JSONObject;
import com.jokeep.context.RequestContextKit;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class BaseController {
    protected JSONObject requestParam() {
        return RequestContextKit.getRequestParam();
    }

    protected Object requestBody() {
        return RequestContextKit.getRequestBody();
    }

    protected JSONObject requestHeader() {
        return RequestContextKit.getRequestHeader();
    }

    protected List<MultipartFile> multipartFiles() {
        return RequestContextKit.getMultipartFiles();
    }

    protected Object getLoginUserInfo(String key) {
        return RequestContextKit.getLoginUserInfo(key);
    }
}

