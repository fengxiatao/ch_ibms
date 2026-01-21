package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * IoT GIS 服务接口
 *
 * @author IBMS Team
 */
public interface IotGisService {

    /**
     * 获取 GIS 统计信息
     *
     * @return 统计信息
     */
    IotGisStatisticsRespVO getStatistics();

    /**
     * 获取图层边界
     *
     * @param layer 图层名称
     * @return 边界信息
     */
    IotGisLayerBoundsRespVO getLayerBounds(String layer);

    /**
     * 空间搜索
     *
     * @param searchReqVO 搜索条件
     * @return 要素列表
     */
    PageResult<IotGisFeatureRespVO> searchFeatures(IotGisSearchReqVO searchReqVO);

    /**
     * 获取附近的设备
     *
     * @param nearbyReqVO 附近查询条件
     * @return 设备列表
     */
    List<IotGisDeviceRespVO> getNearbyDevices(IotGisNearbyReqVO nearbyReqVO);

    /**
     * 获取边界范围内的设备
     *
     * @param boundsReqVO 边界范围条件
     * @return 设备列表
     */
    List<IotGisDeviceRespVO> getDevicesInBounds(IotGisDevicesInBoundsReqVO boundsReqVO);

    /**
     * 获取设备位置信息
     *
     * @param deviceId 设备ID
     * @return 位置信息
     */
    IotGisDeviceLocationRespVO getDeviceLocation(Long deviceId);

    /**
     * 更新设备位置
     *
     * @param updateReqVO 更新请求
     */
    void updateDeviceLocation(IotGisUpdateLocationReqVO updateReqVO);

    /**
     * 获取设备的空间关系
     *
     * @param deviceId 设备ID
     * @return 空间关系信息
     */
    IotGisSpatialRelationRespVO getSpatialRelation(Long deviceId);

    /**
     * 获取热力图数据
     *
     * @param heatmapReqVO 热力图请求
     * @return 热力图点列表
     */
    List<IotGisHeatmapPointRespVO> getHeatmapData(IotGisHeatmapReqVO heatmapReqVO);

    /**
     * 获取聚合数据
     *
     * @param clusterReqVO 聚合请求
     * @return 聚合结果列表
     */
    List<IotGisClusterRespVO> getClusterData(IotGisClusterReqVO clusterReqVO);

    /**
     * 测量两点之间的距离
     *
     * @param lon1 起点经度
     * @param lat1 起点纬度
     * @param lon2 终点经度
     * @param lat2 终点纬度
     * @return 距离信息
     */
    IotGisDistanceRespVO measureDistance(BigDecimal lon1, BigDecimal lat1, BigDecimal lon2, BigDecimal lat2);

    /**
     * 获取图层要素列表
     *
     * @param reqVO 查询条件
     * @return 要素列表
     */
    PageResult<IotGisFeatureRespVO> getLayerFeatures(IotGisLayerFeaturesReqVO reqVO);

    /**
     * 根据建筑编码查询建筑信息
     *
     * @param code 建筑编码
     * @return 建筑信息
     */
    IotGisBuildingRespVO getBuildingByCode(String code);
}



