package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building.BuildingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building.BuildingSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.gis.BuildingConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.BuildingMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.CampusMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.FloorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 建筑 Service 实现类
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author IBMS Team
 */
@Service
@Validated
@Slf4j
public class BuildingServiceImpl implements BuildingService {

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private CampusMapper campusMapper;

    @Autowired
    private FloorMapper floorMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBuilding(BuildingSaveReqVO createReqVO) {
        // 校验园区存在
        validateCampusExists(createReqVO.getCampusId());
        // 校验建筑编码唯一性
        validateBuildingCodeUnique(null, createReqVO.getCode());

        // 插入
        BuildingDO building = BuildingConvert.INSTANCE.convert(createReqVO);
        buildingMapper.insert(building);

        // 更新园区的建筑数量
        updateCampusBuildingCount(createReqVO.getCampusId());

        // 返回
        return building.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBuilding(BuildingSaveReqVO updateReqVO) {
        // 校验存在
        BuildingDO oldBuilding = validateBuildingExists(updateReqVO.getId());
        // 校验园区存在
        validateCampusExists(updateReqVO.getCampusId());
        // 校验建筑编码唯一性
        validateBuildingCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        BuildingDO updateObj = BuildingConvert.INSTANCE.convert(updateReqVO);
        buildingMapper.updateById(updateObj);

        // 如果园区变更，更新新旧园区的建筑数量
        if (!oldBuilding.getCampusId().equals(updateReqVO.getCampusId())) {
            updateCampusBuildingCount(oldBuilding.getCampusId());
            updateCampusBuildingCount(updateReqVO.getCampusId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBuilding(Long id) {
        // 校验存在
        BuildingDO building = validateBuildingExists(id);

        // 校验是否有关联的楼层
        Long floorCount = floorMapper.selectCount(cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO::getBuildingId, id);
        if (floorCount > 0) {
            throw exception(BUILDING_HAS_FLOORS);
        }

        // 删除
        buildingMapper.deleteById(id);

        // 更新园区的建筑数量
        updateCampusBuildingCount(building.getCampusId());
    }

    private BuildingDO validateBuildingExists(Long id) {
        BuildingDO building = buildingMapper.selectById(id);
        if (building == null) {
            throw exception(BUILDING_NOT_EXISTS);
        }
        return building;
    }

    private void validateCampusExists(Long campusId) {
        if (campusMapper.selectById(campusId) == null) {
            throw exception(CAMPUS_NOT_EXISTS);
        }
    }

    private void validateBuildingCodeUnique(Long id, String code) {
        BuildingDO building = buildingMapper.selectOne(BuildingDO::getCode, code);
        if (building == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的建筑
        if (id == null) {
            throw exception(BUILDING_CODE_DUPLICATE);
        }
        if (!building.getId().equals(id)) {
            throw exception(BUILDING_CODE_DUPLICATE);
        }
    }

    private void updateCampusBuildingCount(Long campusId) {
        // 注意：buildingCount字段不存在于数据库中，此方法已废弃
        // 如需获取建筑数量，请使用 getBuildingCountByCampusId() 方法动态查询
    }

    @Override
    public BuildingDO getBuilding(Long id) {
        return buildingMapper.selectById(id);
    }

    @Override
    public PageResult<BuildingDO> getBuildingPage(BuildingPageReqVO pageReqVO) {
        return buildingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<BuildingDO> getBuildingList() {
        return buildingMapper.selectList();
    }

    @Override
    public List<BuildingDO> getBuildingListByCampusId(Long campusId) {
        return buildingMapper.selectList(BuildingDO::getCampusId, campusId);
    }

    @Override
    public Long getBuildingCountByCampusId(Long campusId) {
        return buildingMapper.countByCampusId(campusId);
    }

    @Override
    public List<BuildingDO> getBuildingWithJobConfig() {
        return buildingMapper.selectBuildingWithJobConfig();
    }

    @Override
    public void updateBuildingJobConfig(Long id, String jobConfig) {
        // 验证建筑存在
        validateBuildingExists(id);
        
        BuildingDO updateObj = new BuildingDO();
        updateObj.setId(id);
        updateObj.setJobConfig(jobConfig);
        buildingMapper.updateById(updateObj);
    }

}

