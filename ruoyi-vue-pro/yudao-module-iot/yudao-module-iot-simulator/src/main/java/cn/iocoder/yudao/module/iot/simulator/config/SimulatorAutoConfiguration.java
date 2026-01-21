package cn.iocoder.yudao.module.iot.simulator.config;

import cn.iocoder.yudao.module.iot.simulator.core.SimulatorManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 模拟器自动配置
 *
 * @author Kiro
 */
@Configuration
public class SimulatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SimulatorManager simulatorManager() {
        return new SimulatorManager();
    }
}
