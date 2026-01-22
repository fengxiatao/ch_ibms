package ${package.Controller};

import java.util.Map;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ${package.Service}. ${table.serviceName};
<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>

/**
 * <p>
 * ${table.comment!} 前端控制器
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@RequestMapping("<#if package.ModuleName?? && package.ModuleName != "">/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass} {
<#else>
public class ${table.controllerName} {
</#if>
    @Autowired
    ${table.serviceName} ${util.lowerFirst(table.serviceName)};

    /**
    * 新增${table.comment}
    * @param map
    * ${table.name}新增数据
    * @return
    */
    @ApiOperation(value = "${table.comment}", notes = "新增${table.comment}")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public void add(@RequestBody Map map) {
        JSONObject params = (JSONObject) this.requestBody();
        ${util.lowerFirst(table.serviceName)}.add(params);
    }

    /**
    * 修改${table.comment}
    * @param map
    * ${table.name}修改数据
    * @return
    */
    @ApiOperation(value = "${table.comment}", notes = "修改${table.comment}")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public void update(@RequestBody Map map) {
        JSONObject params = (JSONObject) this.requestBody();
        ${util.lowerFirst(table.serviceName)}.update(params);
    }
    <#if (util.getPrimaryKeys(table.fields)?size ==1)>

    /**
    * 取指定ID的${table.comment}数据
    * @param id
    * ${table.name} 主键ID
    * @return
    * 返回指定id ${table.comment}数据
    */
    @ApiOperation(value = "${table.comment}", notes = "取${table.comment}")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
    public Map get(@RequestParam ${primaryField.propertyType} id) {
        return ${util.lowerFirst(table.serviceName)}.get(id);
    }
    </#if>

    /**
    * ${table.comment}分页取数
    * @param map
    * ${table.name} 分页取数参数，pageIndex=页码；pageSize=每页记录数；
    * @return
    * 返回分页数据
    */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public IPage<Map> page(@RequestBody Map map){
        JSONObject params = (JSONObject) this.requestBody();
        IPage<Map> page = ${util.lowerFirst(table.serviceName)}.page(params);
        return page;
    }
 <#if (util.getPrimaryKeys(table.fields)?size ==1)>

    /**
    * 删除指定ID的${table.comment}数据
    * @param id
    * ${table.name} 主键ID
    */
    @ApiOperation(value = "${table.comment}", notes = "删除${table.comment}")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
    public void delete(@RequestParam ${primaryField.propertyType} id) {
        ${util.lowerFirst(table.serviceName)}.delete(id);
    }
 </#if>
<#if (util.getPrimaryKeys(table.fields)?size ==1)>

    /**
    * 批量删除指定ID的${table.comment}数据
    * @param map
    * ${table.name} 主键ID
    */
    @ApiOperation(value = "${table.comment}", notes = "批量删除${table.comment}")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.POST)
    <#assign primaryField=util.getPrimaryKeys(table.fields)[0]/>
    public void batchDelete(@RequestBody Map map) {
        JSONObject params = (JSONObject) this.requestBody();
        ${primaryField.propertyType}[] ids=params.getObject("ids",${primaryField.propertyType}[].class);
        ${util.lowerFirst(table.serviceName)}.batchDelete(ids);
    }
</#if>

}
</#if>
