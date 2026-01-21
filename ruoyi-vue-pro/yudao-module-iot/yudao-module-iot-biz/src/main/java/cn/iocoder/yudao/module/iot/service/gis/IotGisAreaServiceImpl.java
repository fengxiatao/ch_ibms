package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.area.AreaPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.AreaMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 区域 Service 实现类
 *
 * @author IBMS Team
 */
@Service
@Validated
@Slf4j
public class IotGisAreaServiceImpl implements IotGisAreaService {

    @Autowired
    private AreaMapper areaMapper;

    @Override
    public Long createArea(AreaDO area) {
        areaMapper.insert(area);
        return area.getId();
    }

    @Override
    public void updateArea(AreaDO area) {
        areaMapper.updateById(area);
    }

    @Override
    public void deleteArea(Long id) {
        areaMapper.deleteById(id);
    }

    @Override
    public AreaDO getArea(Long id) {
        return areaMapper.selectById(id);
    }

    @Override
    public List<AreaDO> getAreaList() {
        return areaMapper.selectList();
    }

    @Override
    public PageResult<AreaDO> getAreaPage(AreaPageReqVO pageReqVO) {
        // ✅ 调用 Mapper 自定义的 selectPage 方法（单参数），而不是 BaseMapperX 的默认方法（双参数）
        return areaMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AreaDO> getAreaListByFloorId(Long floorId) {
        return areaMapper.selectList(AreaDO::getFloorId, floorId);
    }
}

