package com.jokeep.framework.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jokeep.framework.entity.BaseEntity;

import java.util.Map;

public interface ServiceBase {
    Map<String, Object> procedurePage(String querySql, int pageIndex, int pageSize, String fields, String orderBy);

    <T extends BaseEntity,M extends BaseMapper> void logicDelete(Object id, Class<M> mapperClass, Class<T> entityClass) throws IllegalAccessException, InstantiationException;
}
