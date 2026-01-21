package cn.iocoder.yudao.framework.mybatis.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Configuration;

/**
 * 布尔值类型处理器配置
 * 
 * 确保 Boolean 类型正确映射到 MySQL 的 BIT(1) 字段
 * 
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Configuration
public class BooleanTypeHandlerConfig {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void registerBooleanTypeHandler() {
        TypeHandlerRegistry registry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();

        // 注册布尔值处理器，确保高优先级
        registry.register(Boolean.class, org.apache.ibatis.type.BooleanTypeHandler.class);
        registry.register(boolean.class, org.apache.ibatis.type.BooleanTypeHandler.class);
        
        // 同时注册到具体的 JDBC 类型
        registry.register(Boolean.class, JdbcType.BIT, org.apache.ibatis.type.BooleanTypeHandler.class);
        registry.register(boolean.class, JdbcType.BIT, org.apache.ibatis.type.BooleanTypeHandler.class);
        registry.register(Boolean.class, JdbcType.BOOLEAN, org.apache.ibatis.type.BooleanTypeHandler.class);
        registry.register(boolean.class, JdbcType.BOOLEAN, org.apache.ibatis.type.BooleanTypeHandler.class);

        log.info("布尔值类型处理器注册完成，确保 Boolean 类型正确处理，不受 Object 处理器影响");
    }
}
