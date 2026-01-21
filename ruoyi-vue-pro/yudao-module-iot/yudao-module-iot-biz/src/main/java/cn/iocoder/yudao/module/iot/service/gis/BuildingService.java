package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building.BuildingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building.BuildingSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 建筑 Service 接口
 *
 * @author IBMS Team
 */
public interface BuildingService {

    /**
     * 创建建筑
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBuilding(@Valid BuildingSaveReqVO createReqVO);

    /**
     * 更新建筑
     *
     * @param updateReqVO 更新信息
     */
    void updateBuilding(@Valid BuildingSaveReqVO updateReqVO);

    /**
     * 删除建筑
     *
     * @param id 编号
     */
    void deleteBuilding(Long id);

    /**
     * 获得建筑
     *
     * @param id 编号
     * @return 建筑
     */
    BuildingDO getBuilding(Long id);

    /**
     * 获得建筑分页
     *
     * @param pageReqVO 分页查询
     * @return 建筑分页
     */
    PageResult<BuildingDO> getBuildingPage(BuildingPageReqVO pageReqVO);

    /**
     * 获得建筑列表
     *
     * @return 建筑列表
     */
    List<BuildingDO> getBuildingList();

    /**
     * 根据园区ID获取建筑列表
     *
     * @param campusId 园区ID
     * @return 建筑列表
     */
    List<BuildingDO> getBuildingListByCampusId(Long campusId);

    /**
     * 获取园区下的建筑数量
     *
     * @param campusId 园区ID
     * @return 建筑数量
     */
    Long getBuildingCountByCampusId(Long campusId);

    /**
     * 获取所有配置了 jobConfig 的建筑
     *
     * @return 建筑列表（包含 id 和 jobConfig 字段）
     */
    List<BuildingDO> getBuildingWithJobConfig();

    /**
     * 更新建筑的定时任务配置
     *
     * @param id 建筑ID
     * @param jobConfig 任务配置JSON
     */
    void updateBuildingJobConfig(Long id, String jobConfig);

}

