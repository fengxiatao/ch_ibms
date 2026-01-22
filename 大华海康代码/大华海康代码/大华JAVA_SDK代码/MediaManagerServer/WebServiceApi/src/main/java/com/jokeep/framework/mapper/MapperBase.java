package com.jokeep.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

@Mapper
public interface MapperBase<T> extends BaseMapper<T> {

    @Select("{call P_PageList(#{QueryStr,mode=IN,jdbcType=VARCHAR},#{PageSize,mode=IN,jdbcType=INTEGER},#{PageCurrent,mode=IN,jdbcType=INTEGER},#{FdShow,mode=IN,jdbcType=VARCHAR},#{FdOrder,mode=IN,jdbcType=VARCHAR},#{RecordCount,mode=OUT,jdbcType=INTEGER},#{PageCount,mode=OUT,jdbcType=INTEGER})}")
    @Options(statementType = StatementType.CALLABLE)
    List<Map<String, Object>> procedurePage(Map<String,Object> params);


}
