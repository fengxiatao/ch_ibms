package com.jokeep.filters;

import com.jokeep.context.ReadHttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        ServletRequest requestWrapper = null;

        this.originCross(request,response);

        if (request.getMethod().equals( RequestMethod.OPTIONS.toString())){
            return;
        }

        if (req instanceof HttpServletRequest && (req.getContentType() != null && !req.getContentType().startsWith("multipart/form-data"))) {
            //clone请求数据
            requestWrapper = new ReadHttpServletRequestWrapper(request);
        }
        if (requestWrapper == null)
            filterChain.doFilter(request, response);
        else
            filterChain.doFilter(requestWrapper, response);
    }

    @Override
    public void destroy() {

    }

    //处理跨域问题
    private void originCross(HttpServletRequest request,HttpServletResponse response){
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Headers", "Authorization,Origin, X-Requested-With, Content-Type,Accept,Access-Token,F_SessionID");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
        response.setHeader("Access-Control-Allow-Origin", origin);
        // 是否容许浏览器携带用户身份信息（cookie）
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Expose-Headers", "*");
    }
}
