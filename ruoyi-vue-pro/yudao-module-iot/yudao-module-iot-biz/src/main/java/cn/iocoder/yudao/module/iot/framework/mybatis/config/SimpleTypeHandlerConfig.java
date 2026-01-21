package cn.iocoder.yudao.module.iot.framework.mybatis.config;

import cn.iocoder.yudao.module.iot.framework.mybatis.handler.SimpleObjectTypeHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Configuration;

/**
 * 简单类型处理器配置
 * 注册自定义的类型处理器，避免 JSON 解析错误
 *
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Configuration
public class SimpleTypeHandlerConfig {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void registerTypeHandlers() {
        try {
            TypeHandlerRegistry registry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();

            // 先确保 Boolean 类型使用正确的处理器（优先级高）
            registry.register(Boolean.class, org.apache.ibatis.type.BooleanTypeHandler.class);
            registry.register(boolean.class, org.apache.ibatis.type.BooleanTypeHandler.class);
            
            // 注册简单的 Object 类型处理器，避免 JSON 解析问题
            // 由于 Boolean 已经注册，不会被 Object 处理器覆盖
            registry.register(java.lang.Object.class, new SimpleObjectTypeHandler());

            log.info("类型处理器注册完成：Boolean使用默认处理器，Object使用简单字符串处理器");
        } catch (Exception e) {
            log.error("类型处理器注册失败", e);
            // 不抛出异常，避免影响系统启动
        }
    }
}
