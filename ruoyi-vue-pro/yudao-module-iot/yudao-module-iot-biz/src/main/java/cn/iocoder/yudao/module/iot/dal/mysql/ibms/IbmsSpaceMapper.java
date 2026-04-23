package cn.iocoder.yudao.module.iot.dal.mysql.ibms;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsSpaceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * IBMS 空间 Mapper
 */
@Mapper
public interface IbmsSpaceMapper extends BaseMapperX<IbmsSpaceDO> {

    /**
     * 按 {@code extra} 中与旧 GIS 主键对齐的字段查找空间行。
     * <p>
     * 约定键名（数字，与 {@code iot_gis_*} 表主键一致）：{@code gisAreaId}、{@code gisFloorId}、{@code gisBuildingId}、{@code gisCampusId}。
     * 需在 {@code ibms_space.extra} 中维护映射后，批量指派才可反查 {@code space_id}。
     * </p>
     */
    @Select("SELECT * FROM ibms_space WHERE tenant_id = #{tenantId} AND deleted = 0 "
            + "AND extra IS NOT NULL AND extra != '' AND JSON_VALID(extra) "
            + "AND JSON_EXTRACT(extra, CONCAT('$.', #{key})) IS NOT NULL "
            + "AND CAST(JSON_UNQUOTE(JSON_EXTRACT(extra, CONCAT('$.', #{key}))) AS UNSIGNED) = #{value} "
            + "LIMIT 1")
    IbmsSpaceDO selectByExtraGisId(@Param("tenantId") Long tenantId, @Param("key") String key, @Param("value") Long value);
}

