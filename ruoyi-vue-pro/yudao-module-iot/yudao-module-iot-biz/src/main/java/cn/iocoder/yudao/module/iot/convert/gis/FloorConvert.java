package cn.iocoder.yudao.module.iot.convert.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.floor.FloorRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.floor.FloorSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import cn.hutool.core.util.StrUtil;

import java.util.List;

/**
 * 楼层 Convert
 *
 * @author IBMS Team
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FloorConvert {

    FloorConvert INSTANCE = Mappers.getMapper(FloorConvert.class);

    FloorDO convert(FloorSaveReqVO bean);

    FloorRespVO convert(FloorDO bean);

    List<FloorRespVO> convertList(List<FloorDO> list);

    PageResult<FloorRespVO> convertPage(PageResult<FloorDO> page);

    /**
     * ✅ 修复 PostGIS geometry 字段：空字符串 → null
     * 
     * PostgreSQL 的 geometry 类型只接受有效的 WKT/WKB 或 NULL，
     * 空字符串会导致 "parse error - invalid geometry" 错误
     */
    @AfterMapping
    default void normalizeGeometryFields(@MappingTarget FloorDO target) {
        // 处理 geom 字段
        if (StrUtil.isBlank(target.getGeom())) {
            target.setGeom(null);
        }
    }

}

