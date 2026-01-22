package com.jokeep.config;

import com.jokeep.common.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

@Configuration
public class SnowFlakeConfig {
    @Autowired
    private Environment env;

    @Bean(name = "snowFlake")
    @Scope("singleton")
    public SnowFlake snowFlake() {
        return new SnowFlake(env.getProperty("snowFlake.dataCenterId", Long.class), env.getProperty("snowFlake.machineId", Long.class));
    }
}
