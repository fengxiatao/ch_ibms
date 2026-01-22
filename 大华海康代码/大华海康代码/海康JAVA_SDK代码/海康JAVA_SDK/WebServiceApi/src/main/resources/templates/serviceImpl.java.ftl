package ${package.ServiceImpl};

import ${package.Entity}.${entity};
import ${package.Mapper}.${table.mapperName};
import ${package.Service}.${table.serviceName};
import ${superServiceImplClassPackage};
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import java.util.Map;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import com.jokeep.framework.annotations.Slave;
<#if (util.getPrimaryKeys(table.fields)?size ==1)>
import com.jokeep.common.IdBuilder;
</#if>
/**
 * <p>
 * ${table.comment!} 服务实现类
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@Service
<#if kotlin>
open class ${table.serviceImplName} : ${superServiceImplClass}<${table.mapperName}, ${entity}>(), ${table.serviceName} {

}
<#else>
public class ${table.serviceImplName} extends ${superServiceImplClass}<${table.mapperName}, ${entity}> implements ${table.serviceName} {

    @Autowired
    ${table.mapperName} ${util.lowerFirst(table.mapperName)};

    /**
    * 新增${table.comment}
    * @param map
    * ${table.name}新增数据
    * @return
    */
    @SneakyThrows
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void add(Map map){
    <#if (util.getPrimaryKeys(table.fields)?size ==1)>
        <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
        <#if (primaryField.propertyType =="String")>
        String id = IdBuilder.getUUID();
        <#else>
        long id = IdBuilder.getSnowId();
     </#if>
        map.put("${primaryField.name}",id);
    </#if>
         ${table.entityName} ${util.lowerFirst(table.entityName)} = this.mapToEntity(map, ${table.entityName}.class,OperateEnum.Insert);
         ${util.lowerFirst(table.mapperName)}.insert(${util.lowerFirst(table.entityName)});
    }

    /**
    * 修改${table.comment}
    * @param map
    * ${table.name}修改数据
    * @return
    */
    @SneakyThrows
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void update(Map map){
        ${table.entityName} ${util.lowerFirst(table.entityName)} = this.mapToEntity(map, ${table.entityName}.class,OperateEnum.Update);
        ${util.lowerFirst(table.mapperName)}.updateById(${util.lowerFirst(table.entityName)});
    }
    <#if (util.getPrimaryKeys(table.fields)?size ==1)>

    /**
    * 取指定ID的${table.comment}数据
    * @param id
    * ${table.name} 主键ID
    * @return
    * 返回指定ID的${table.comment}数据
    */
    <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
    @Slave
    @Override
    public Map get(${primaryField.propertyType} id) {
        return ${util.lowerFirst(table.mapperName)}.get(id);
    }
    </#if>

    /**
    * ${table.comment}分页取数
    * @param map
    * ${table.name} 分页取数参数，pageIndex=页码；pageSize=每页记录数；
    * @return
    * 返回分页数据
    */
    @Slave
    @Override
    public IPage<Map> page(JSONObject map) {
        int pageIndex=Integer.valueOf(map.getString("pageIndex"));
        int pageSize=Integer.valueOf(map.getString("pageSize"));
        IPage page=new Page<Map>(pageIndex, pageSize);
        QueryWrapper<Map> queryWrapper=new QueryWrapper<>();
        //取数条件
        //queryWrapper.eq("字段名",字段值);
        //……
        //排序字段
        //queryWrapper.orderByAsc("正序字段");
        //queryWrapper.orderByDesc("倒序字段");
        return ${util.lowerFirst(table.mapperName)}.page(page,queryWrapper);
    }
    <#if (util.getPrimaryKeys(table.fields)?size ==1)>

    /**
    * 删除指定ID的${table.comment}数据
    * @param id
    * ${table.name} 主键ID
    */
    <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void delete(${primaryField.propertyType} id){
        ${util.lowerFirst(table.mapperName)}.delete(id);
    }

    /**
    * 逻辑删除指定ID的${table.comment}数据
    * @param id
    * ${table.name} 主键ID
    */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void logicDelete(${primaryField.propertyType} id){
        this.logicDelete(id, ${table.mapperName}.class, ${entity}.class);
    }
    </#if>

    <#if (util.getPrimaryKeys(table.fields)?size ==1)>

    /**
    * 批量删除指定ID的${table.comment}数据
    * @param ids
    * ${table.name} 主键ID数组
    */
    <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void batchDelete(${primaryField.propertyType}[] ids){
        ${util.lowerFirst(table.mapperName)}.batchDelete(ids);
    }

    /**
    * 批量逻辑删除指定ID的${table.comment}数据
    * @param ids
    * ${table.name} 主键ID数组
    */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void batchLogicDelete(${primaryField.propertyType}[] ids){
        for(${primaryField.propertyType} id:ids){
            this.logicDelete(id, ${table.mapperName}.class, ${entity}.class);
        }
    }
    </#if>
}
</#if>
