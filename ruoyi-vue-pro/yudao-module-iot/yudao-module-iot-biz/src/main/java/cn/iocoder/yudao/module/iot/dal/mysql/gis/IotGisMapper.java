package cn.iocoder.yudao.module.iot.dal.mysql.gis;

import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * IoT GIS Mapper
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author IBMS Team
 */
@Mapper
public interface IotGisMapper {

    /**
     * 统计园区数量
     */
    Integer countCampus();

    /**
     * 统计建筑数量
     */
    Integer countBuilding();

    /**
     * 统计楼层数量
     */
    Integer countFloor();

    /**
     * 统计房间数量
     */
    Integer countRoom();

    /**
     * 按状态统计设备数量
     * 返回列表，每个元素包含 status 和 count 字段
     */
    List<Map<String, Object>> countDeviceByStatus();

    /**
     * 获取图层边界
     */
    Map<String, BigDecimal> getLayerBounds(@Param("layer") String layer);

    /**
     * 搜索要素
     */
    List<IotGisFeatureRespVO> searchFeatures(@Param("req") IotGisSearchReqVO searchReqVO);

    /**
     * 搜索要素数量
     */
    Long searchFeaturesCount(@Param("req") IotGisSearchReqVO searchReqVO);

    /**
     * 获取附近的设备
     */
    List<IotGisDeviceRespVO> getNearbyDevices(@Param("req") IotGisNearbyReqVO nearbyReqVO);

    /**
     * 获取边界范围内的设备
     */
    List<IotGisDeviceRespVO> getDevicesInBounds(@Param("req") IotGisDevicesInBoundsReqVO boundsReqVO);

    /**
     * 获取设备位置信息
     */
    IotGisDeviceLocationRespVO getDeviceLocation(@Param("deviceId") Long deviceId);

    /**
     * 更新设备位置
     */
    void updateDeviceLocation(@Param("req") IotGisUpdateLocationReqVO updateReqVO);

    /**
     * 获取设备的空间关系
     */
    IotGisSpatialRelationRespVO getSpatialRelation(@Param("deviceId") Long deviceId);

    /**
     * 获取热力图数据
     */
    List<IotGisHeatmapPointRespVO> getHeatmapData(@Param("req") IotGisHeatmapReqVO heatmapReqVO);

    /**
     * 获取用于聚合的设备数据
     */
    List<IotGisDeviceRespVO> getDevicesForCluster(@Param("req") IotGisClusterReqVO clusterReqVO);

    /**
     * 计算两点之间的距离
     */
    BigDecimal calculateDistance(@Param("lon1") BigDecimal lon1,
                                  @Param("lat1") BigDecimal lat1,
                                  @Param("lon2") BigDecimal lon2,
                                  @Param("lat2") BigDecimal lat2);

    /**
     * 获取图层要素列表
     */
    List<IotGisFeatureRespVO> getLayerFeatures(@Param("req") IotGisLayerFeaturesReqVO reqVO);

    /**
     * 获取图层要素数量
     */
    Long getLayerFeaturesCount(@Param("req") IotGisLayerFeaturesReqVO reqVO);

    /**
     * 根据建筑编码查询建筑信息
     */
    IotGisBuildingRespVO getBuildingByCode(@Param("code") String code);
}

