package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.FloorDetailVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.FloorVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.floor.FloorPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;

import java.util.List;

/**
 * IoT GIS 楼层 Service 接口
 *
 * @author 芋道源码
 */
public interface IotGisFloorService {

    /**
     * 获取建筑的所有楼层
     *
     * @param buildingId 建筑ID
     * @return 楼层列表
     */
    List<FloorVO> getBuildingFloors(Long buildingId);

    /**
     * 获取楼层详情（包含房间和设备）
     *
     * @param floorId 楼层ID
     * @return 楼层详情
     */
    FloorDetailVO getFloorDetail(Long floorId);

    /**
     * 获取楼层基本信息
     *
     * @param floorId 楼层ID
     * @return 楼层信息
     */
    FloorVO getFloorInfo(Long floorId);

    // ========== 新增 CRUD 方法 ==========

    /**
     * 创建楼层
     *
     * @param floor 楼层信息
     * @return 楼层ID
     */
    Long createFloor(FloorDO floor);

    /**
     * 更新楼层
     *
     * @param floor 楼层信息
     */
    void updateFloor(FloorDO floor);

    /**
     * 删除楼层
     *
     * @param id 楼层ID
     */
    void deleteFloor(Long id);

    /**
     * 获取楼层
     *
     * @param id 楼层ID
     * @return 楼层信息
     */
    FloorDO getFloor(Long id);

    /**
     * 获取楼层列表
     *
     * @return 楼层列表
     */
    List<FloorDO> getFloorList();

    /**
     * 获取楼层分页
     *
     * @param pageReqVO 分页请求
     * @return 楼层分页
     */
    PageResult<FloorDO> getFloorPage(FloorPageReqVO pageReqVO);

    /**
     * 根据建筑ID获取楼层列表
     *
     * @param buildingId 建筑ID
     * @return 楼层列表
     */
    List<FloorDO> getFloorListByBuildingId(Long buildingId);

    /**
     * 获取所有配置了 jobConfig 的楼层
     *
     * @return 楼层列表（包含 id 和 jobConfig 字段）
     */
    List<FloorDO> getFloorWithJobConfig();

    /**
     * 更新楼层的定时任务配置
     *
     * @param id 楼层ID
     * @param jobConfig 任务配置JSON
     */
    void updateFloorJobConfig(Long id, String jobConfig);
}












