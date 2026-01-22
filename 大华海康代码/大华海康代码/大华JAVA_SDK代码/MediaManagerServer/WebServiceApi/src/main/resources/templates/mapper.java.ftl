package ${package.Mapper};

import ${package.Entity}.${entity};
import ${superMapperClassPackage};
import org.apache.ibatis.annotations.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.conditions.Wrapper;

import java.util.Map;
<#if mapperAnnotation>
import org.apache.ibatis.annotations.Mapper;
</#if>

/**
 * <p>
 * ${table.comment!} Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
<#if mapperAnnotation>
@Mapper
</#if>
<#if kotlin>
interface ${table.mapperName} : ${superMapperClass}<${entity}>
<#else>
public interface ${table.mapperName} extends ${superMapperClass}<${entity}> {

 <#if (util.getPrimaryKeys(table.fields)?size ==1)>
  /**
  * 取指定ID的${table.comment}数据
  * @param id
  * ${table.name} 主键ID
  * @return
  * 返回指定ID的${table.comment}数据
  */
  <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
  @Select("SELECT ${util.fieldAlias(table.fieldNames,"t")} FROM ${table.name} t WHERE ${primaryField.name}=<#noparse>#{id}</#noparse>")
  Map get(@Param("id") ${primaryField.propertyType} id);
 </#if>

  /**
  * ${table.comment}分页取数
  * @param page
  * 分页参数；
  * @param wrapper
  * 条件参数；
  * @return
  * 返回分页数据
  */
  @Select("SELECT ${util.fieldAlias(table.fieldNames,"t")} FROM ${table.name} t <#noparse>${ew.customSqlSegment}</#noparse>")
  IPage<Map> page(IPage<Map> page,@Param(Constants.WRAPPER) Wrapper<Map> wrapper);
 <#if (util.getPrimaryKeys(table.fields)?size ==1)>

  /**
  * 删除指定ID的${table.comment}数据
  * @param id
  * ${table.name} 主键ID
  */
  <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
  @Delete("DELETE FROM ${table.name} WHERE ${primaryField.name}=<#noparse>#{id}</#noparse>")
  void delete(@Param("id") ${primaryField.propertyType} id);
 </#if>
 <#if (util.getPrimaryKeys(table.fields)?size ==1)>

  /**
  * 批量删除指定ID的${table.comment}数据
  * @param ids
  * ${table.name} 主键ID数组
  */
  <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
  <#if !(util.getLogicDeletedField(table.fields,logicDeleteFieldName))??>
  @Delete("<script>DELETE FROM ${table.name} WHERE ${primaryField.name} IN <foreach collection='ids' item='id' open='(' separator=',' close=')'><#noparse>#{id}</#noparse></foreach> </script>")
  void batchDelete(@Param("ids") ${primaryField.propertyType}[] ids);
  </#if>
 </#if>
}
</#if>
