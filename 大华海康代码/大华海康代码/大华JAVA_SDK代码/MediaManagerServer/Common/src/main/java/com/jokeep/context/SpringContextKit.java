package com.jokeep.context;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 获取spring容器，以访问容器中定义的其他bean
 */
@Component("springContextKit")
public class SpringContextKit implements ApplicationContextAware {
    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    @Autowired
    DefaultListableBeanFactory defaultListableBeanFactory;

    // Spring应用上下文环境
    private static ApplicationContext applicationContext;

    /**
     * 实现ApplicationContextAware接口的回调方法，设置上下文环境
     *
     * @param applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextKit.applicationContext = applicationContext;
    }

    /**
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取对象
     * 这里重写了bean方法，起主要作用
     *
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    public static <T> T getBean(Class<T> clasz) {
        return getApplicationContext().getBean(clasz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 通过name获取 Bean.
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return getApplicationContext().getBeansOfType(clazz);
    }

    public static String getActiveProfile() {
        return getApplicationContext().getEnvironment().getActiveProfiles()[0];
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param key 配置项key
     */
    public static String getEnvironmentProperty(String key) {
        return getApplicationContext().getEnvironment().getProperty(key);
    }

    /**
     * 移除指定bean
     *
     * @param beanName
     */
    public static void removeBean(String beanName) {
        if (SpringContextKit.existBean(beanName)) {
            ConfigurableApplicationContext configurableApplicationContext =
                    (ConfigurableApplicationContext) SpringContextKit.getApplicationContext();
            //获取BeanFactory
            DefaultListableBeanFactory defaultListableBeanFactory =
                    (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
            defaultListableBeanFactory.removeBeanDefinition(beanName);
        }
    }

    /**
     * 注册bean
     *
     * @param beanName
     * @param clasz
     * @param properties
     */
    public static void registerBean(String beanName, Class<?> clasz, Map<String, Object> properties,
                                    String destroyMethodName, String initMethodName, String setFactoryMethodName) {
        if (!SpringContextKit.existBean(beanName)) {
            ConfigurableApplicationContext configurableApplicationContext =
                    (ConfigurableApplicationContext) SpringContextKit.getApplicationContext();
            //获取BeanFactory
            DefaultListableBeanFactory defaultListableBeanFactory =
                    (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
            //创建bean信息.
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clasz);
            if (properties != null && properties.size() > 0) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    beanDefinitionBuilder.addPropertyValue(entry.getKey(), entry.getValue());
                }
            }
            if (StringUtils.isNotEmpty(initMethodName))
                beanDefinitionBuilder.setInitMethodName(initMethodName);
            if (StringUtils.isNotEmpty(destroyMethodName))
                beanDefinitionBuilder.setDestroyMethodName(destroyMethodName);
            if (StringUtils.isNotEmpty(setFactoryMethodName))
                beanDefinitionBuilder.setFactoryMethod(setFactoryMethodName);
            defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
            //configurableApplicationContext.refresh();
        }
    }

    /**
     * 替换bean
     *
     * @param beanName
     * @param bean
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static void replaceBean(String beanName, Object bean) throws NoSuchFieldException, IllegalAccessException {
        if (SpringContextKit.existBean(beanName)) {
            ConfigurableApplicationContext context = (ConfigurableApplicationContext) getApplicationContext();
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
            //反射获取Factory中的singletonObjects 将该名称下的bean进行替换
            Field singletonObjects = DefaultSingletonBeanRegistry.class.getDeclaredField("singletonObjects");
            singletonObjects.setAccessible(true);
            Map<String, Object> map = (Map<String, Object>) singletonObjects.get(beanFactory);
            map.put(beanName, bean);
        }
    }

    public static boolean existBean(String beanName) {
        //return getApplicationContext().containsBean(beanName);
        return getApplicationContext().containsBeanDefinition(beanName) && getApplicationContext().containsBean(beanName);
    }

    //将已创建的对象注册到spring Ioc容器
    public static void registerBean(String beanName, Object bean) {
        if (!SpringContextKit.existBean(beanName)) {
            ConfigurableApplicationContext configurableApplicationContext =
                    (ConfigurableApplicationContext) SpringContextKit.getApplicationContext();
            //获取BeanFactory
            DefaultListableBeanFactory defaultListableBeanFactory =
                    (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
            //获取AutowireBeanFactory
            AutowireCapableBeanFactory autowireCapableBeanFactory =
                    configurableApplicationContext.getAutowireCapableBeanFactory();
            defaultListableBeanFactory.registerSingleton(beanName, bean);
            autowireCapableBeanFactory.autowireBean(bean);
        }
    }
}