package com.jokeep.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestDecrypt {
    //请求数据是否加密，加密自动解密处理
    boolean value() default true;
    //如果请求数据加密，此字段不设置，则请求数据全部加密（只针对post提交的数据），如果设置，只针对指定的字段做解密处理
    String[] fields() default {};
}