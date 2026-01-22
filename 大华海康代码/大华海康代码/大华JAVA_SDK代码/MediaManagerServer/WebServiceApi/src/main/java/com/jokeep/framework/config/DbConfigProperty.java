package com.jokeep.framework.config;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix="spring.datasource")
public class DbConfigProperty {
    JSONObject druid;
}
