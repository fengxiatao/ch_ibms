package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.AreaRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.PathRequestVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.PathStepRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.AreaMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 区域 Service 实现类
 *
 * @author ruoyi
 */
@Service
@Slf4j
public class AreaServiceImpl implements AreaService {

    @Autowired
    private AreaMapper areaMapper;

    @Override
    public List<AreaRespVO> getAreasByFloorId(Long floorId) {
        List<AreaDO> areas = areaMapper.selectListByFloorId(floorId);
        return BeanUtils.toBean(areas, AreaRespVO.class);
    }

    @Override
    public List<AreaRespVO> getAreasByBuildingId(Long buildingId) {
        List<AreaDO> areas = areaMapper.selectListByBuildingId(buildingId);
        return BeanUtils.toBean(areas, AreaRespVO.class);
    }

    @Override
    public List<AreaRespVO> getAreasByFloorIdAndType(Long floorId, String areaType) {
        List<AreaDO> areas = areaMapper.selectListByFloorIdAndType(floorId, areaType);
        return BeanUtils.toBean(areas, AreaRespVO.class);
    }

    @Override
    public AreaRespVO getAreaById(Long id) {
        AreaDO area = areaMapper.selectById(id);
        return BeanUtils.toBean(area, AreaRespVO.class);
    }

    @Override
    public String getFloorVisualizationData(Long floorId) {
        try {
            String jsonData = areaMapper.getFloorVisualizationData(floorId);
            log.debug("获取楼层{}可视化数据成功，数据长度：{}", floorId, jsonData != null ? jsonData.length() : 0);
            return jsonData;
        } catch (Exception e) {
            log.error("获取楼层{}可视化数据失败", floorId, e);
            throw new RuntimeException("获取楼层可视化数据失败：" + e.getMessage());
        }
    }

    @Override
    public List<PathStepRespVO> findShortestPath(PathRequestVO request) {
        try {
            List<Map<String, Object>> pathSteps = areaMapper.findShortestPath(
                request.getFromAreaId(),
                request.getToAreaId(),
                request.getAccessibleOnly() != null ? request.getAccessibleOnly() : false
            );

            List<PathStepRespVO> result = new ArrayList<>();
            for (Map<String, Object> step : pathSteps) {
                PathStepRespVO vo = new PathStepRespVO();
                vo.setAreaId(getLongValue(step, "area_id"));
                vo.setAreaName((String) step.get("area_name"));
                vo.setAreaType((String) step.get("area_type"));
                vo.setStepNumber(getIntegerValue(step, "step_number"));
                vo.setTotalCost(getBigDecimalValue(step, "total_cost"));
                vo.setInstruction((String) step.get("instruction"));
                result.add(vo);
            }

            log.debug("查找路径成功：从区域{}到区域{}，共{}步",
                request.getFromAreaId(), request.getToAreaId(), result.size());

            return result;
        } catch (Exception e) {
            log.error("查找路径失败：从区域{}到区域{}", request.getFromAreaId(), request.getToAreaId(), e);
            throw new RuntimeException("查找路径失败：" + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> findNearbyAreas(Long areaId, Double maxDistance, Boolean includeVertical) {
        try {
            return areaMapper.findNearbyAreas(
                areaId,
                maxDistance != null ? maxDistance : 50.0,
                includeVertical != null ? includeVertical : false
            );
        } catch (Exception e) {
            log.error("查找附近区域失败：区域ID={}", areaId, e);
            throw new RuntimeException("查找附近区域失败：" + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> analyzeReachability(Long areaId, Integer maxHops) {
        try {
            return areaMapper.analyzeReachability(
                areaId,
                maxHops != null ? maxHops : 5
            );
        } catch (Exception e) {
            log.error("分析区域可达性失败：区域ID={}", areaId, e);
            throw new RuntimeException("分析区域可达性失败：" + e.getMessage());
        }
    }

    @Override
    public Long locateDeviceToArea(Long deviceId) {
        try {
            Long areaId = areaMapper.locateDeviceToArea(deviceId);
            log.debug("设备{}定位到区域：{}", deviceId, areaId);
            return areaId;
        } catch (Exception e) {
            log.error("设备定位失败：设备ID={}", deviceId, e);
            throw new RuntimeException("设备定位失败：" + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).longValue();
        return Long.valueOf(value.toString());
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).intValue();
        return Integer.valueOf(value.toString());
    }

    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof Integer) return BigDecimal.valueOf((Integer) value);
        if (value instanceof Long) return BigDecimal.valueOf((Long) value);
        return new BigDecimal(value.toString());
    }

    @Override
    public List<AreaDO> getAreaWithJobConfig() {
        return areaMapper.selectAreaWithJobConfig();
    }

    @Override
    public void updateAreaJobConfig(Long id, String jobConfig) {
        AreaDO updateObj = new AreaDO();
        updateObj.setId(id);
        updateObj.setJobConfig(jobConfig);
        areaMapper.updateById(updateObj);
    }

}

