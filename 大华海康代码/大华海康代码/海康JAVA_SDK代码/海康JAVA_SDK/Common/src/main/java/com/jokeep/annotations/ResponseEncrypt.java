package com.jokeep.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseEncrypt {
    boolean value() default true;
    String[] fields() default {};
}