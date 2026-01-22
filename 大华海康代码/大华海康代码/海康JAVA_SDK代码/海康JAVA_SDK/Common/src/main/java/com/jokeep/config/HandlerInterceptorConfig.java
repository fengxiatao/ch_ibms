package com.jokeep.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.jokeep.interceptors.RequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class HandlerInterceptorConfig implements WebMvcConfigurer {
    //注册拦截请求
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestInterceptor())
                .excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**")
                .excludePathPatterns("/error/**","/error.*");
           /*.addPathPatterns("/api/**");
            .excludePathPatterns("/setting/**","/error.*")
            .excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**");
         */
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //资源访问权限
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    //添加json转换器（默认情况返回为字符串会报错）
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new FastJsonHttpMessageConverter());
    }
}
