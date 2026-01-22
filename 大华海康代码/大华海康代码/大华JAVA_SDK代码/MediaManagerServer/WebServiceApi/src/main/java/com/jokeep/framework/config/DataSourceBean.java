package com.jokeep.framework.config;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.sql.DataSource;

@Data
@AllArgsConstructor
public class DataSourceBean {
    String beanName;
    DataSource dataSource;
}
