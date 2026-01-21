package cn.iocoder.yudao.framework.mybatis.core.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Boolean 到 Integer 的 TypeHandler
 * 用于 PostgreSQL 等严格类型检查的数据库
 * 将 Java 的 Boolean 类型映射为数据库的 SMALLINT/INTEGER 类型
 *
 * @author IBMS Team
 */
@MappedTypes(Boolean.class)
@MappedJdbcTypes({JdbcType.SMALLINT, JdbcType.INTEGER, JdbcType.TINYINT})
public class BooleanToIntTypeHandler extends BaseTypeHandler<Boolean> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType) throws SQLException {
        // Boolean -> Integer: true=1, false=0
        ps.setInt(i, parameter ? 1 : 0);
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        // Integer -> Boolean: 0=false, others=true
        return !rs.wasNull() && value != 0;
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return !rs.wasNull() && value != 0;
    }

    @Override
    public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return !cs.wasNull() && value != 0;
    }
}

