package com.jokeep.framework.config;

import com.jokeep.framework.extend.ClientHttpRequestHeaderInterceptor;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {

    @Resource
    ClientHttpRequestHeaderInterceptor clientHttpRequestHeaderInterceptor;

    @ConditionalOnMissingBean(RestTemplate.class)
    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        restTemplate.setInterceptors(Collections.singletonList(clientHttpRequestHeaderInterceptor));
        return restTemplate;
    }

    /**
     * 使用HttpClient作为底层客户端
     * @return
     */
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        return new OkHttp3ClientHttpRequestFactory(okHttpClient);
    }

}
