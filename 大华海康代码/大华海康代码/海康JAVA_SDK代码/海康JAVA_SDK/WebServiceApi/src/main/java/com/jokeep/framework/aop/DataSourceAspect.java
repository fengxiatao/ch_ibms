package com.jokeep.framework.aop;

import com.jokeep.framework.config.DataSourceContextHolder;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(0)
public class DataSourceAspect {
    /**
     * 需要读的方法,切面
     */
    @Pointcut("@annotation(com.jokeep.framework.annotations.Slave)" +
            "&& execution(* com.jokeep.framework.service.*.*(..))")
    public void readPointcut() {

    }

    /**
     * 写切面
     */
    @Pointcut("(!@annotation(com.jokeep.framework.annotations.Slave)" +
            "||@annotation(org.springframework.transaction.annotation.Transactional))"+
            "&& execution(* com.jokeep.framework.service.*.*(..))")
    public void writePointcut() {

    }

    @Before("readPointcut()")
    public void read() {
        DataSourceContextHolder.slave();
    }

    @Before("writePointcut()")
    public void write() {
        DataSourceContextHolder.master();
    }

    @After("readPointcut()")
    public void readAfter() {
        DataSourceContextHolder.clear();
    }

    @After("writePointcut()")
    public void writeAfter() {
        DataSourceContextHolder.clear();
    }
}
