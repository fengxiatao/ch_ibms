package com.jokeep.interceptors;

import com.jokeep.annotations.NoAuthentication;
import com.jokeep.annotations.RequestDecrypt;
import com.jokeep.context.RequestContextKit;
import com.jokeep.exception.CustomException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestInterceptor implements HandlerInterceptor {
    //请求进入Controller前调用
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handle) throws Exception {
        //if(handle instanceof ResourceHttpRequestHandler) return true;
        if (handle instanceof HandlerMethod) {
            //Object controller = methodHandler.getBean();
            HandlerMethod methodHandler = (HandlerMethod) handle;
            RequestDecrypt requestDecrypt = methodHandler.getMethodAnnotation(RequestDecrypt.class);
            //解密指定字段
            if (requestDecrypt != null && (requestDecrypt.value() && requestDecrypt.fields().length > 0)) {
                //从登录信息中取AES key
                RequestContextKit.begin(httpServletRequest, httpServletResponse, requestDecrypt.fields());
            } else if (requestDecrypt == null) {
                //默认情况全加密，默认加密的参数__data
                RequestContextKit.begin(httpServletRequest, httpServletResponse, "__data");
            } else {
                RequestContextKit.begin(httpServletRequest, httpServletResponse);
            }
            NoAuthentication noAuthentication = methodHandler.getMethodAnnotation(NoAuthentication.class);
            if ((noAuthentication == null || !noAuthentication.value()) && !RequestContextKit.checkLogin()) {
                throw new CustomException("您没有登录或登录已过期，请登录后重试", 1);
            }
            return true;
        }
        return true;
    }

    //Controller的方法调用之后执行
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handle, ModelAndView modelAndView) throws Exception {
        //System.out.println("postHandle被调用");
    }

    //在请求处理后
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handle, Exception e) throws Exception {
        //在请求处理后调用
        if (handle instanceof HandlerMethod) {
            RequestContextKit.end();
        }
    }

}
