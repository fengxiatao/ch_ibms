package com.jokeep.common;

import com.jokeep.context.SpringContextKit;

import java.util.UUID;

public class IdBuilder {
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    public static long getSnowId(){
        return ((SnowFlake)SpringContextKit.getBean("snowFlake")).nextId();
    }
}
