package com.jokeep.utils;

import lombok.SneakyThrows;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class PathKit {

    public static String getContextPath() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getServletContext().getContextPath();
    }

    public static String getRealPath() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getServletContext().getRealPath("/");
    }

    public static String getAppPath() throws UnsupportedEncodingException {
        /*
        ApplicationHome applicationHome = new ApplicationHome(PathKit.class);
        File file = applicationHome.getSource();
        File parentFile = file.getParentFile();
        String path = parentFile.getPath();
        if (path.endsWith("WEB-INF"))
            parentFile = parentFile.getParentFile();
        return parentFile.getPath();
         */
        String classesPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        if (classesPath.startsWith("/"))
            classesPath = classesPath.substring(1);
        File file = new File(classesPath);
        File parentFile = file.getParentFile();
        String path;
        if (isRunTomcat()) {
            //在tomcat中运行
            path = parentFile.getParentFile().getPath();
        } else {
            path = System.getProperty("user.dir");
        }
        return URLDecoder.decode(path, "UTF-8");
    }

    @SneakyThrows
    public static String getClassPath(){
        String classesPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        if (classesPath.startsWith("/"))
            classesPath = classesPath.substring(1);
        if(!classesPath.endsWith("/"))
            classesPath=classesPath+"/";
        return URLDecoder.decode(classesPath, "UTF-8");
    }

    //判断是否部署在tomcat中运行
    public static boolean isRunTomcat() {
        String classesPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        if (classesPath.startsWith("/"))
            classesPath = classesPath.substring(1);
        File file = new File(classesPath);
        File parentFile = file.getParentFile();
        if (parentFile.getPath().endsWith("WEB-INF/") || parentFile.getPath().endsWith("WEB-INF")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否jar模式运行
     *
     * @return
     */
    public static boolean isJar() {
        return getPath().endsWith(".jar");
    }

    /**
     * 获取jar包所在路径
     *
     * @return jar包所在路径
     */
    public static String getPath() {
        ApplicationHome home = new ApplicationHome(PathKit.class);
        String path = home.getSource().getPath();
        return path;
    }


    public static String getAppName() {
        String path = getRealPath();
        if (path.endsWith("\\"))
            path = path.substring(0, path.lastIndexOf("\\"));
        if (path.endsWith("/"))
            path = path.substring(0, path.lastIndexOf("/"));
        path = path.substring(path.lastIndexOf("\\") + 1, path.length());
        path = path.substring(path.lastIndexOf("/") + 1, path.length());
        return path;
    }
}
