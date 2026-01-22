package com.jokeep.framework.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSONObject;
import com.jokeep.context.SpringContextKit;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@DependsOn("springContextKit")
public class DbConfig {

    @Autowired
    private Environment env;
    @Autowired
    private DbConfigProperty dbConfigProperty;

    @Bean(name = "transactionManager")
    @DependsOn("dataSource")
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    @Bean("dataSource")
    @DependsOn("multipleDataSource")
    public DataSource dataSource(@Qualifier("multipleDataSource") MultipleDataSource multipleDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(multipleDataSource.getMaster().getBeanName(), multipleDataSource.getMaster().getDataSource());
        List<DataSourceBean> slaves = multipleDataSource.getSlaves();
        for (DataSourceBean dataSourceBean : slaves) {
            targetDataSources.put(dataSourceBean.getBeanName(), dataSourceBean.getDataSource());
        }
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(multipleDataSource.getMaster().getDataSource());
        routingDataSource.setTargetDataSources(targetDataSources);
        return routingDataSource;
    }

    @Bean(name = "multipleDataSource")
    public MultipleDataSource initDataSource() {
        JSONObject properties = dbConfigProperty.getDruid();
        MultipleDataSource result = new MultipleDataSource();
        for (String key : properties.keySet()) {
            JSONObject dataSourceProperty = properties.getJSONObject(key);
            String beanName = key + "$DataSource";
            DataSource dataSource = this.buildDataSource(beanName, dataSourceProperty);
            if (key.toLowerCase().equals("master")) {
                DataSourceBean dataSourceBean=new DataSourceBean(beanName,dataSource);
                result.setMaster(dataSourceBean);
            }
            else {
                DataSourceBean dataSourceBean=new DataSourceBean(beanName,dataSource);
                result.getSlaves().add(dataSourceBean);
            }
        }
        return result;
    }

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @SneakyThrows
    private DataSource buildDataSource(String beanName, JSONObject properties) {
        properties.put("password", properties.getString("password"));
        String url = System.getProperty("dbUrl");
        String user = System.getProperty("dbUser");
        String password = System.getProperty("dbPassword");
        if (url != null && !url.trim().equals(""))
            properties.put("url", url);
        if (user != null && !user.trim().equals(""))
            properties.put("username", user.trim());
        if (password != null && !password.trim().equals(""))
            properties.put("password", password.trim());

        properties.put("initialSize", properties.getString("initialSize"));
        properties.put("minIdle", properties.getString("minIdle"));
        properties.put("maxActive", properties.getString("maxActive"));
        properties.put("maxWait", properties.getString("maxWait"));
        properties.put("timeBetweenEvictionRunsMillis", properties.getString("timeBetweenEvictionRunsMillis"));
        properties.put("minEvictableIdleTimeMillis", properties.getString("minEvictableIdleTimeMillis"));
        properties.put("testWhileIdle", properties.getString("testWhileIdle"));
        properties.put("testOnBorrow", properties.getString("testOnBorrow"));
        properties.put("testOnReturn", properties.getString("testOnReturn"));
        properties.put("poolPreparedStatements", properties.getString("poolPreparedStatements"));
        properties.put("maxPoolPreparedStatementPerConnectionSize", properties.getString("maxPoolPreparedStatementPerConnectionSize"));
        if(properties.containsKey("webStatFilter")) {
            JSONObject webStatFilter = properties.getJSONObject("webStatFilter");
            webStatFilter.put("enabled", webStatFilter.getString("enabled"));
            webStatFilter.put("urlPattern", webStatFilter.getString("urlPattern"));
            webStatFilter.put("sessionStatEnable", webStatFilter.getString("sessionStatEnable"));
            webStatFilter.put("sessionStatMaxCount", webStatFilter.getString("sessionStatMaxCount"));
            webStatFilter.put("profileEnable", webStatFilter.getString("profileEnable"));
        }
        if(properties.containsKey("statViewServlet")) {
            JSONObject statViewServlet = properties.getJSONObject("statViewServlet");
            statViewServlet.put("enabled", statViewServlet.getString("enabled"));
            statViewServlet.put("resetEnable", statViewServlet.getString("resetEnable"));
            statViewServlet.put("loginUsername", statViewServlet.getString("loginUsername"));
            statViewServlet.put("loginPassword", statViewServlet.getString("loginPassword"));
        }
        DruidDataSource dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
        SpringContextKit.registerBean(beanName,dataSource);
        return dataSource;
    }
}
