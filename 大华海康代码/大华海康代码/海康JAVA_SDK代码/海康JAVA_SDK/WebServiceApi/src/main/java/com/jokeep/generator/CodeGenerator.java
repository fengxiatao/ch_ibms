package com.jokeep.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.jokeep.framework.controller.BaseController;
import com.jokeep.framework.entity.BaseEntity;
import com.jokeep.framework.mapper.MapperBase;
import com.jokeep.framework.serviceImpl.ServiceBaseImpl;

import java.util.*;

public class CodeGenerator {
    public static void main(String[] args) {
        try {
            List<String> tables = new ArrayList<>();
            tables.add("T_Enterprise");

            FastAutoGenerator.create("jdbc:sqlserver://192.168.1.20;DatabaseName=JokeepFramework","sa","jokeep@sql2020")
                    .globalConfig(builder -> {
                        builder.author("jsw")               //作者
                                .outputDir(System.getProperty("user.dir")+"\\WebServiceApi\\src\\main\\java")    //输出路径(写到java目录)
                                .enableSwagger()           //开启swagger
                                .commentDate("yyyy-MM-dd");
                                //.fileOverride(); //开启覆盖之前生成的文件
                    })
                    .packageConfig(builder -> {
                        builder.parent("com.jokeep.business")
                                //.moduleName("api")
                                .entity("entity")
                                .service("service")
                                .serviceImpl("serviceImpl")
                                .controller("controller")
                                .mapper("mapper")
                                .xml("mapper")
                                .pathInfo(Collections.singletonMap(OutputFile.mapperXml,System.getProperty("user.dir")+"\\WebServiceApi\\src\\main\\resources\\mapper\\business"));
                    })
                    .strategyConfig(builder -> {
                        //builder.addInclude(tables)
                        builder.addExclude("TB_MODULE","TB_PARAM","TB_SYSTEM","TB_WEBAPI","V_Sys_AuthorizeUser","V_Sys_DepartmentUser","V_Sys_RoleUser","V_Sys_UserList")
                                .addTablePrefix("T_")
                                .addFieldPrefix("F_")
                                .serviceBuilder()
                                .formatServiceFileName("%sService")
                                .superServiceImplClass(ServiceBaseImpl.class)
                                .formatServiceImplFileName("%sServiceImpl")
                                .entityBuilder()
                                .superClass(BaseEntity.class)
                                .enableLombok()
                                .logicDeleteColumnName("F_IsDeleted")
                                .enableTableFieldAnnotation()
                                .controllerBuilder()
                                .enableHyphenStyle()
                                .superClass(BaseController.class)
                                .formatFileName("%sController")
                                .enableRestStyle()
                                .mapperBuilder()
                                .superClass(MapperBase.class)
                                .formatMapperFileName("%sMapper")
                                .enableMapperAnnotation()
                                .formatXmlFileName("%sMapper");
                    })
                    .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                    .injectionConfig(builder -> {
                        builder.customMap(new HashMap<String,Object>(){{
                            put("util",new CodeUtils());//代码处理工具类
                        }});
                    })
                    .execute();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    // 处理 all 情况
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }
}
