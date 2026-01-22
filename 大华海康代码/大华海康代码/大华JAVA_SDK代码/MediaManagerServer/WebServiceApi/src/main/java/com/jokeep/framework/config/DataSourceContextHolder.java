package com.jokeep.framework.config;

import com.jokeep.context.SpringContextKit;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@DependsOn("multipleDataSource")
public class DataSourceContextHolder {

    static MultipleDataSource multipleDataSource;

    static {
        multipleDataSource = (MultipleDataSource) SpringContextKit.getBean("multipleDataSource");
    }
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    private static final AtomicInteger COUNTER = new AtomicInteger(-1);

    private static void set(String beanName) {
        CONTEXT_HOLDER.set(beanName);
    }

    public static String get() {
        return CONTEXT_HOLDER.get();
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    public static void master() {
        if(get()==null) {
            set(multipleDataSource.getMaster().getBeanName());
        }
    }

    public static void slave() {
        if(get()==null) {
            //轮询
            int len = multipleDataSource.getSlaves().size();
            if (len == 0) {
                master();
            } else {
                if (COUNTER.get() > 99999)
                    COUNTER.set(-1);
                int index = COUNTER.incrementAndGet() % len;
                set(multipleDataSource.getSlaves().get(index).getBeanName());
            }
        }
    }
}
