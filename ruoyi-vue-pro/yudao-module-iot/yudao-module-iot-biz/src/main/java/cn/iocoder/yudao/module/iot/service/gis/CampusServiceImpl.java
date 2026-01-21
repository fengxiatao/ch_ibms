package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.campus.CampusPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.campus.CampusSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.gis.CampusConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.CampusMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 园区 Service 实现类
 *
 * @author IBMS Team
 */
@Service
@Validated
@Slf4j
public class CampusServiceImpl implements CampusService {

    @Autowired
    private CampusMapper campusMapper;

    @Autowired
    private BuildingService buildingService;

    @Override
    public Long createCampus(CampusSaveReqVO createReqVO) {
        // 校验园区编码唯一性
        validateCampusCodeUnique(null, createReqVO.getCode());

        // 插入
        CampusDO campus = CampusConvert.INSTANCE.convert(createReqVO);
        campusMapper.insert(campus);

        // 返回
        return campus.getId();
    }

    @Override
    public void updateCampus(CampusSaveReqVO updateReqVO) {
        // 校验存在
        validateCampusExists(updateReqVO.getId());
        // 校验园区编码唯一性
        validateCampusCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        CampusDO updateObj = CampusConvert.INSTANCE.convert(updateReqVO);
        campusMapper.updateById(updateObj);
    }

    @Override
    public void deleteCampus(Long id) {
        // 校验存在
        validateCampusExists(id);

        // 校验是否有关联的建筑
        Long buildingCount = buildingService.getBuildingCountByCampusId(id);
        if (buildingCount != null && buildingCount > 0) {
            throw exception(CAMPUS_HAS_BUILDINGS);
        }

        // 删除
        campusMapper.deleteById(id);
    }

    private void validateCampusExists(Long id) {
        if (campusMapper.selectById(id) == null) {
            throw exception(CAMPUS_NOT_EXISTS);
        }
    }

    private void validateCampusCodeUnique(Long id, String code) {
        CampusDO campus = campusMapper.selectOne(CampusDO::getCode, code);
        if (campus == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的园区
        if (id == null) {
            throw exception(CAMPUS_CODE_DUPLICATE);
        }
        if (!campus.getId().equals(id)) {
            throw exception(CAMPUS_CODE_DUPLICATE);
        }
    }

    @Override
    public CampusDO getCampus(Long id) {
        return campusMapper.selectById(id);
    }

    @Override
    public PageResult<CampusDO> getCampusPage(CampusPageReqVO pageReqVO) {
        return campusMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CampusDO> getCampusList() {
        return campusMapper.selectList();
    }

    @Override
    public List<CampusDO> getCampusWithJobConfig() {
        return campusMapper.selectCampusWithJobConfig();
    }

    @Override
    public void updateCampusJobConfig(Long id, String jobConfig) {
        // 验证园区存在
        validateCampusExists(id);
        
        CampusDO updateObj = new CampusDO();
        updateObj.setId(id);
        updateObj.setJobConfig(jobConfig);
        campusMapper.updateById(updateObj);
    }

}

