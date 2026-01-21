package cn.iocoder.yudao.module.iot.convert.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.campus.CampusRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.campus.CampusSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import cn.hutool.core.util.StrUtil;

import java.util.List;

/**
 * 园区 Convert
 *
 * @author IBMS Team
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CampusConvert {

    CampusConvert INSTANCE = Mappers.getMapper(CampusConvert.class);

    CampusDO convert(CampusSaveReqVO bean);

    CampusRespVO convert(CampusDO bean);

    List<CampusRespVO> convertList(List<CampusDO> list);

    PageResult<CampusRespVO> convertPage(PageResult<CampusDO> page);

    /**
     * ✅ 修复 PostGIS geometry 字段：空字符串 → null
     * 
     * PostgreSQL 的 geometry 类型只接受有效的 WKT/WKB 或 NULL，
     * 空字符串会导致 "parse error - invalid geometry" 错误
     */
    @AfterMapping
    default void normalizeGeometryFields(@MappingTarget CampusDO target) {
        // 处理 geom 字段
        if (StrUtil.isBlank(target.getGeom())) {
            target.setGeom(null);
        }
        
        // 处理 centerPoint 字段（如果有）
        if (StrUtil.isBlank(target.getCenterPoint())) {
            target.setCenterPoint(null);
        }
    }

}

