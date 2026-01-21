package cn.iocoder.yudao.module.iot.convert.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building.BuildingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building.BuildingSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import cn.hutool.core.util.StrUtil;

import java.util.List;

/**
 * 建筑 Convert
 *
 * @author IBMS Team
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BuildingConvert {

    BuildingConvert INSTANCE = Mappers.getMapper(BuildingConvert.class);

    BuildingDO convert(BuildingSaveReqVO bean);

    BuildingRespVO convert(BuildingDO bean);

    List<BuildingRespVO> convertList(List<BuildingDO> list);

    PageResult<BuildingRespVO> convertPage(PageResult<BuildingDO> page);

    /**
     * ✅ 修复 PostGIS geometry 字段：空字符串 → null
     * 
     * PostgreSQL 的 geometry 类型只接受有效的 WKT/WKB 或 NULL，
     * 空字符串会导致 "parse error - invalid geometry" 错误
     */
    @AfterMapping
    default void normalizeGeometryFields(@MappingTarget BuildingDO target) {
        // 处理 geom 字段
        if (StrUtil.isBlank(target.getGeom())) {
            target.setGeom(null);
        }
    }

}

