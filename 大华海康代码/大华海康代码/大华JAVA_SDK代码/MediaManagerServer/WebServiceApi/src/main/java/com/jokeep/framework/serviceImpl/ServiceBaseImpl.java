package com.jokeep.framework.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jokeep.context.RequestContextKit;
import com.jokeep.context.SpringContextKit;
import com.jokeep.framework.entity.BaseEntity;
import com.jokeep.framework.mapper.MapperBase;
import com.jokeep.framework.service.ServiceBase;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceBaseImpl<M extends MapperBase<T>, T> extends ServiceImpl<M, T> implements ServiceBase {
    //新增时附加操作用户信息的字段对照关系
    private final static Map<String, String> INSERT_OPERATOR_FIELDS = new HashMap() {{
        put("F_CreateUserID", "F_UserID");
        put("F_CreateUserName", "F_UserName");
        put("F_CreateDeptID", "F_DepartmentID");
        put("F_CreateDeptName", "F_DepartmentName");
        put("F_CreateUnitID", "F_MainUnitID");
        put("F_CreateUintName", "F_UnitFullName");
        put("F_UpdateUserID", "F_UserID");
        put("F_UpdateUserName", "F_UserName");
        put("F_UpdateDeptID", "F_DepartmentID");
        put("F_UpdateDeptName", "F_DepartmentName");
        put("F_UpdateUnitID", "F_MainUnitID");
        put("F_UpdateUnitName", "F_UnitFullName");
    }};
    //修改时附加操作用户信息的字段对照关系
    private final static Map<String, String> UPDATE_OPERATOR_FIELDS = new HashMap() {{
        put("F_UpdateUserID", "F_UserID");
        put("F_UpdateUserName", "F_UserName");
        put("F_UpdateDeptID", "F_DepartmentID");
        put("F_UpdateDeptName", "F_DepartmentName");
        put("F_UpdateUnitID", "F_MainUnitID");
        put("F_UpdateUnitName", "F_UnitFullName");
    }};

    //修改时附加操作用户信息的字段对照关系
    private final static Map<String, String> DELETE_OPERATOR_FIELDS = new HashMap() {{
        put("F_DeleteUserID", "F_UserID");
        put("F_DeleteUserName", "F_UserName");
        put("F_DeleteDeptID", "F_DepartmentID");
        put("F_DeleteDeptName", "F_DepartmentName");
        put("F_DeleteUnitID", "F_MainUnitID");
        put("F_DeleteUnitName", "F_UnitFullName");
    }};

    @Autowired
    MapperBase mapperBase;

    //分页存储过程
    @Override
    @Transactional
    public Map<String, Object> procedurePage(String querySql, int pageIndex, int pageSize, String fields,
                                             String orderBy) {
        Map<String, Object> params = new HashMap<>();
        params.put("QueryStr", querySql);
        params.put("PageSize", pageSize);
        params.put("PageCurrent", pageIndex);
        params.put("FdShow", fields);
        params.put("FdOrder", orderBy);
        List<Map<String, Object>> list = mapperBase.procedurePage(params);
        Map<String, Object> page = new HashMap<>();
        page.put("pageIndex", pageIndex);
        page.put("pageSize", pageSize);
        page.put("list", list);
        page.put("rowCount", params.get("RecordCount"));
        page.put("pageCount", params.get("PageCount"));
        return page;
    }

    @SneakyThrows
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public <T extends BaseEntity, M extends BaseMapper> void logicDelete(Object id, Class<M> mapperClass,
                                                                         Class<T> entityClass) {
        BaseMapper mapper = SpringContextKit.getBean(mapperClass);
        T t = entityClass.newInstance();
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            TableId tableId = field.getAnnotation(TableId.class);
            if (tableId != null) {
                field.setAccessible(true);
                Object value = id;
                field.set(t, this.convertType(value, field.getType()));
            }
        }
        mapper.deleteById(t);
        Map<String, Object> map = new HashMap<>();
        t = this.mapToEntity(map, entityClass, OperateEnum.Delete);
        mapper.updateById(t);
    }

    //根据map给实体类赋值
    protected <T> T mapToEntity(Map<String, Object> map, Class<T> dClass, OperateEnum operateEnum) throws IllegalAccessException,
            InstantiationException {
        //取当前登录信息
        JSONObject loginUser = operateEnum != OperateEnum.Other ? RequestContextKit.getLoginUser() : null;
        //根据不同操作将操作人信息附加到对应操作上
        switch (operateEnum) {
            case Insert:
                this.insertMergeOperatorInfo(map, loginUser);
                break;
            case Update:
                this.updateMergeOperatorInfo(map, loginUser);
                break;
            case Delete:
                this.deleteMergeOperatorInfo(map, loginUser);
                break;
            case Other:
                break;
        }
        T t = dClass.newInstance();
        Field[] fields = dClass.getDeclaredFields();
        for (Field field : fields) {
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField != null) {
                String fieldName = tableField.value();
                if (map.containsKey(fieldName)) {
                    field.setAccessible(true);
                    Object value = map.containsKey(fieldName) ? map.get(fieldName) : null;
                    field.set(t, this.convertType(value, field.getType()));
                }
            }
            TableId tableId = field.getAnnotation(TableId.class);
            if (tableId != null) {
                String fieldName = tableId.value();
                if (map.containsKey(fieldName)) {
                    field.setAccessible(true);
                    Object value = map.containsKey(fieldName) ? map.get(fieldName) : null;
                    field.set(t, this.convertType(value, field.getType()));
                }
            }
        }

        return t;
    }

    private Object convertType(Object value, Class<?> clazz) {
        if (value == null) return null;
        if (value instanceof String && !clazz.isPrimitive()) {
            if (clazz != String.class && (value.equals("")))
                return null;
        }
        return ConvertUtils.convert(value, clazz);
    }

    private void updateMergeOperatorInfo(Map<String, Object> map, Map<String, Object> operator) {
        if (operator == null || map == null) return;
        for (Map.Entry<String, String> entry : UPDATE_OPERATOR_FIELDS.entrySet()) {
            if (operator.containsKey(entry.getValue()))
                map.put(entry.getKey(), operator.get(entry.getValue()));
        }
        Date currentTime = new Date();
        map.put("F_UpdateTime", currentTime);
    }

    private void insertMergeOperatorInfo(Map<String, Object> map, Map<String, Object> operator) {
        if (operator == null || map == null) return;
        for (Map.Entry<String, String> entry : INSERT_OPERATOR_FIELDS.entrySet()) {
            if (operator.containsKey(entry.getValue()))
                map.put(entry.getKey(), operator.get(entry.getValue()));
        }
        Date currentTime = new Date();
        map.put("F_CreateTime", currentTime);
        map.put("F_UpdateTime", currentTime);
    }

    private void deleteMergeOperatorInfo(Map<String, Object> map, Map<String, Object> operator) {
        if (operator == null || map == null) return;
        for (Map.Entry<String, String> entry : DELETE_OPERATOR_FIELDS.entrySet()) {
            if (operator.containsKey(entry.getValue()))
                map.put(entry.getKey(), operator.get(entry.getValue()));
        }
        Date currentTime = new Date();
        map.put("F_DeleteTime", currentTime);
    }


    public enum OperateEnum {
        Insert,
        Update,
        Delete,
        Other
    }
}
