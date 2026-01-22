package com.jokeep.framework.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Slave {
    boolean value() default true;
}
