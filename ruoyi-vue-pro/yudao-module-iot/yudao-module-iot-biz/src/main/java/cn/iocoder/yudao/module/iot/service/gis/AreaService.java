package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.AreaRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.PathRequestVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.PathStepRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO;

import java.util.List;
import java.util.Map;

/**
 * 区域 Service 接口
 *
 * @author ruoyi
 */
public interface AreaService {

    /**
     * 根据楼层ID查询所有区域
     *
     * @param floorId 楼层ID
     * @return 区域列表
     */
    List<AreaRespVO> getAreasByFloorId(Long floorId);

    /**
     * 根据建筑ID查询所有区域
     *
     * @param buildingId 建筑ID
     * @return 区域列表
     */
    List<AreaRespVO> getAreasByBuildingId(Long buildingId);

    /**
     * 根据楼层ID和区域类型查询
     *
     * @param floorId  楼层ID
     * @param areaType 区域类型
     * @return 区域列表
     */
    List<AreaRespVO> getAreasByFloorIdAndType(Long floorId, String areaType);

    /**
     * 获取区域详情
     *
     * @param id 区域ID
     * @return 区域信息
     */
    AreaRespVO getAreaById(Long id);

    /**
     * 获取楼层可视化数据（JSON格式）
     *
     * @param floorId 楼层ID
     * @return JSON字符串（包含区域、设备、聚类等信息）
     */
    String getFloorVisualizationData(Long floorId);

    /**
     * 查找最短路径
     *
     * @param request 路径请求
     * @return 路径步骤列表
     */
    List<PathStepRespVO> findShortestPath(PathRequestVO request);

    /**
     * 查找附近可导航区域
     *
     * @param areaId          区域ID
     * @param maxDistance     最大距离（米）
     * @param includeVertical 是否包含垂直连接（电梯、楼梯）
     * @return 附近区域列表
     */
    List<Map<String, Object>> findNearbyAreas(Long areaId, Double maxDistance, Boolean includeVertical);

    /**
     * 分析区域可达性
     *
     * @param areaId  区域ID
     * @param maxHops 最大跳数
     * @return 可达区域列表
     */
    List<Map<String, Object>> analyzeReachability(Long areaId, Integer maxHops);

    /**
     * 设备定位到区域
     *
     * @param deviceId 设备ID
     * @return 区域ID
     */
    Long locateDeviceToArea(Long deviceId);

    /**
     * 获取所有配置了 jobConfig 的区域
     *
     * @return 区域列表（包含 id 和 jobConfig 字段）
     */
    List<AreaDO> getAreaWithJobConfig();

    /**
     * 更新区域的定时任务配置
     *
     * @param id 区域ID
     * @param jobConfig 任务配置JSON
     */
    void updateAreaJobConfig(Long id, String jobConfig);

}











