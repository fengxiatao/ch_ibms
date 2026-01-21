package cn.iocoder.yudao.module.iot.newgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IoT 新网关服务启动类
 * 
 * 基于插件化架构的新一代 IoT 网关服务。
 * 
 * 注意：Gateway 模块作为独立的设备网关服务，不需要 Security 功能，
 * 因此排除 Spring Security 和 Actuator Security 的自动配置。
 */
@SpringBootApplication(exclude = {
    SecurityAutoConfiguration.class,
    ManagementWebSecurityAutoConfiguration.class
})
@EnableScheduling  // 启用定时任务
public class IotNewGatewayServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotNewGatewayServerApplication.class, args);
    }
}
