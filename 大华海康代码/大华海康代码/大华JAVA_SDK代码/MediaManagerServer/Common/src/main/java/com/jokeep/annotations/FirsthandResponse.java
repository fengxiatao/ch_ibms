package com.jokeep.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface FirsthandResponse {
    //响应是否原始返回,为true不用ResultVo做包裹
    boolean value() default true;
}
