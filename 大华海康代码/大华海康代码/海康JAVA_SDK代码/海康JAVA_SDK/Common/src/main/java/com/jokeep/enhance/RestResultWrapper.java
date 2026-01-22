package com.jokeep.enhance;

import com.alibaba.fastjson.JSON;
import com.jokeep.annotations.FirsthandResponse;
import com.jokeep.annotations.ResponseEncrypt;
import com.jokeep.context.RequestContextKit;
import com.jokeep.utils.AESKit;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

/**
 * 此注解针对controller层的类做增强功能，即对加了@RestController注解的类进行处理
 */
@Log4j2
@ControllerAdvice(annotations = RestController.class)
public class RestResultWrapper implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        ResponseEncrypt responseEncrypt = methodParameter.getMethod().getAnnotation(ResponseEncrypt.class);
        FirsthandResponse firsthandResponse = methodParameter.getMethod().getAnnotation(FirsthandResponse.class);
        if (firsthandResponse != null && firsthandResponse.value()) {
            return o;
        } else {
            ResultVo result;
            if (o instanceof ResultVo) {
                result = (ResultVo) o;
            } else if (o instanceof Map && ((Map) o).containsKey("result") && ((Map) o).get("result") instanceof ResultVo) {
                result = (ResultVo) ((Map) o).get("result");
            } else if (o instanceof String) {
                result = new ResultVo(0, "Ok", o);
            } else {
                result = new ResultVo(0, "Ok", o);
            }
            return this.responseDataEncrypt(result, responseEncrypt);
        }
    }

    private ResultVo responseDataEncrypt(ResultVo result, ResponseEncrypt responseEncrypt) {
        try {
            if (responseEncrypt != null && !responseEncrypt.value()) {
                return result;
            } else if (responseEncrypt != null && responseEncrypt.fields() != null && responseEncrypt.fields().length > 0) {
                //加密指定字段
                Object aesKeyObj = RequestContextKit.getLoginUserInfo("__AesKey");
                String aesKey = aesKeyObj == null ? "" : aesKeyObj.toString();
                if (result.getData() instanceof Map) {
                    Map data = (Map) result.getData();
                    for (String field : responseEncrypt.fields()) {
                        if (data.containsKey(field))
                            data.put(field, AESKit.aesEncrypt(JSON.toJSONString(data.get(field)), aesKey));
                    }
                }
                return result;
            } else {
                //默认加密所有数据
                Object aesKeyObj = RequestContextKit.getLoginUserInfo("__AesKey");
                String aesKey = aesKeyObj == null ? "" : aesKeyObj.toString();
                Object data = result.getData();
                if (data != null && StringUtils.isNotEmpty(aesKey))
                    result.setData(AESKit.aesEncrypt(JSON.toJSONString(data), aesKey));
                return result;
            }
        } catch (Exception e) {
            log.error(e);
        }
        return result;
    }
}
