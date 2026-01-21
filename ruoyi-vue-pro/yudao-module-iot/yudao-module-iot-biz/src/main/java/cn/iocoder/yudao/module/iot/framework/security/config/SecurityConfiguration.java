package cn.iocoder.yudao.module.iot.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * IoT 模块的 Security 配置
 * 
 * 配置 WebSocket 等端点的访问权限
 *
 * @author 芋道源码
 */
@Slf4j
@Configuration(proxyBeanMethods = false, value = "iotSecurityConfiguration")
public class SecurityConfiguration {

    @Bean("iotAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                log.info("[IoT Security] 配置 WebSocket 端点白名单: /ws/iot/**");
                
                // 所有 IoT WebSocket 端点统一使用 /ws/iot 前缀
                // 允许匿名访问（在 WebSocket 连接建立时会验证 userId）
                registry.requestMatchers("/ws/iot").permitAll()
                        .requestMatchers("/ws/iot/**").permitAll();
                
                log.info("[IoT Security] ✅ WebSocket 端点白名单配置完成");
            }

        };
    }

}

