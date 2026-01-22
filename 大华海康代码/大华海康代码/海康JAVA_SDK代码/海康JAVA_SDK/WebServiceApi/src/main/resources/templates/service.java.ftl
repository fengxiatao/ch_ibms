package ${package.Service};
import java.util.Map;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import ${package.Entity}.${entity};
import ${superServiceClassPackage};

/**
 * <p>
 * ${table.comment!} 服务类
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
<#if kotlin>
interface ${table.serviceName} : ${superServiceClass}<${entity}>
<#else>
public interface ${table.serviceName} extends ${superServiceClass}<${entity}> {

 /**
 * 新增${table.comment}
 * @param map
 * ${table.name}新增数据
 * @return
 */
 void add(Map map);

 /**
 * 修改${table.comment}
 * @param map
 * ${table.name}修改数据
 * @return
 */
 void update(Map map);
 <#if (util.getPrimaryKeys(table.fields)?size ==1)>

  /**
  * 取指定ID的${table.comment}数据
  * @param id
  * ${table.name} 主键ID
  * @return
  * 返回指定ID的${table.comment}数据
  */
  <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
  Map get(${primaryField.propertyType} id);
 </#if>

 /**
 * ${table.comment}分页取数
 * @param map
 * ${table.name} 分页取数参数，pageIndex=页码；pageSize=每页记录数；
 * @return
 * 返回分页数据
 */
 IPage<Map> page(JSONObject map);
 <#if (util.getPrimaryKeys(table.fields)?size ==1)>

  /**
 * 删除指定ID的${table.comment}数据
 * @param id
 * ${table.name} 主键ID
 */
 <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
 void delete(${primaryField.propertyType} id);
 </#if>
 <#if (util.getPrimaryKeys(table.fields)?size ==1)>

  /**
  * 批量删除指定ID的${table.comment}数据
  * @param ids
  * ${table.name} 主键ID数组
  */
  <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
  void batchDelete(${primaryField.propertyType}[] ids);
 </#if>
 <#if (util.getPrimaryKeys(table.fields)?size ==1)>

  /**
  * 逻辑删除指定ID的${table.comment}数据
  * @param id
  * ${table.name} 主键ID
  */
  <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
  void logicDelete(${primaryField.propertyType} id);
 </#if>
 <#if (util.getPrimaryKeys(table.fields)?size ==1)>

  /**
  * 批量删除指定ID的${table.comment}数据
  * @param ids
  * ${table.name} 主键ID数组
  */
  <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
  void batchLogicDelete(${primaryField.propertyType}[] ids);
 </#if>
}
</#if>
