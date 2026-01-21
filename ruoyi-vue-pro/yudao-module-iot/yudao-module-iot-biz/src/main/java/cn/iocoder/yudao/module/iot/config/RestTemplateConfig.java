package cn.iocoder.yudao.module.iot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 * 
 * @author 长辉信息科技有限公司
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * 创建 RestTemplate Bean
     * 
     * @return RestTemplate 实例
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 设置连接超时时间（毫秒）
        factory.setConnectTimeout(5000);
        
        // 设置读取超时时间（毫秒）
        factory.setReadTimeout(10000);
        
        return new RestTemplate(factory);
    }
}
