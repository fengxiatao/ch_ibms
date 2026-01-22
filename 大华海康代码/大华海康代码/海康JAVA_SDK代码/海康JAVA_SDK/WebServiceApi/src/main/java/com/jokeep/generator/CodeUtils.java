package com.jokeep.generator;


import com.baomidou.mybatisplus.generator.config.po.TableField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeUtils {
    private String[] commonFields= new String[]{"F_IsDeleted","F_CreateUserID","F_CreateDeptID","F_CreateUnitID","F_CreateTime","F_UpdateUserID","F_UpdateDeptID","F_UpdateUnitID","F_UpdateTime","F_DeleteUserID","F_DeleteDeptID","F_DeleteUnitID","F_DeleteTime"};
    //首字母小写
    public String lowerFirst(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    //首字母大写
    public String upperFirst(String s) {
        if (Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    //取表主键字段对象（可能组合主键）
    public List<TableField> getPrimaryKeys(List<TableField> list) {
        List<TableField> result = new ArrayList<>();
        for (TableField field : list) {
            if(field.isKeyFlag())
                result.add(field);
        }
        return result;
    }

    //取指定属性字段名的表字段对象
    public TableField getLogicDeletedField(List<TableField> list,String fieldName){
        for (TableField field : list) {
            if(field.getPropertyName().equals(fieldName))
                return field;
        }
        return null;
    }

    public String fieldAlias(String fieldsStr,String alias){
        String[] fields=fieldsStr.split(",");
        StringBuilder builder=new StringBuilder();
        for (String field:fields){
            if(builder.length()>0)
                builder.append(",");
            builder.append(alias+"."+field.trim());
        }
        return builder.toString();
    }

    public boolean isCommonField(String fieldName){
        return Arrays.asList(commonFields).contains(fieldName);
    }

    public String propertyTypeConvert(String propertyType){
        if(propertyType.equals("LocalDateTime"))
            return "Date";
        if(propertyType.equals("LocalDate"))
            return "Date";
        return propertyType;
    }
}
