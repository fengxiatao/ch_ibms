package com.jokeep.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface NoAuthentication {
    boolean value() default true;//true表示无需认证；false表示必须认证
}
