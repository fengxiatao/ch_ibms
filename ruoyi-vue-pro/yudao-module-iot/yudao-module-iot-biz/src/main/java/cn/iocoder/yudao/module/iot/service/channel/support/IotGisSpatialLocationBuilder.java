package cn.iocoder.yudao.module.iot.service.channel.support;

import cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.AreaMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.BuildingMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.CampusMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.FloorMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 旧 GIS 园区/楼栋/楼层/区域 校验并拼接位置文案（与 {@code IotDeviceChannelServiceImpl} 原逻辑一致），供 IoT 通道与 IBMS 通道共用。
 */
@Component
public class IotGisSpatialLocationBuilder {

    @Resource
    private CampusMapper campusMapper;
    @Resource
    private BuildingMapper buildingMapper;
    @Resource
    private FloorMapper floorMapper;
    @Resource
    private AreaMapper areaMapper;

    /**
     * 校验层级关系并返回「园区/楼栋/楼层[/区域]」展示路径。
     */
    public String buildValidatedLocationPath(Long campusId, Long buildingId, Long floorId, Long areaId) {
        CampusDO campus = campusMapper.selectById(campusId);
        BuildingDO building = buildingMapper.selectById(buildingId);
        FloorDO floor = floorMapper.selectById(floorId);

        if (campus == null) {
            throw exception(CAMPUS_NOT_EXISTS);
        }
        if (building == null) {
            throw exception(BUILDING_NOT_EXISTS);
        }
        if (floor == null) {
            throw exception(FLOOR_NOT_EXISTS);
        }
        if (!building.getCampusId().equals(campus.getId())) {
            throw exception(BUILDING_NOT_BELONG_TO_CAMPUS);
        }
        if (!floor.getBuildingId().equals(building.getId())) {
            throw exception(FLOOR_NOT_BELONG_TO_BUILDING);
        }

        String loc = String.format("%s/%s/%s", campus.getName(), building.getName(), floor.getName());

        if (areaId != null) {
            AreaDO area = areaMapper.selectById(areaId);
            if (area == null) {
                throw exception(AREA_NOT_EXISTS);
            }
            if (!floorId.equals(area.getFloorId())) {
                throw exception(AREA_NOT_BELONG_TO_FLOOR);
            }
            if (area.getBuildingId() != null && !buildingId.equals(area.getBuildingId())) {
                throw exception(AREA_NOT_BELONG_TO_BUILDING);
            }
            String areaName = area.getName();
            loc = loc + "/" + (areaName != null ? areaName : "区域");
        }

        return loc;
    }
}
