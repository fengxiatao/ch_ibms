package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.SecurityOverviewCameraPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Nullable;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IoT 设备 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface IotDeviceMapper extends BaseMapperX<IotDeviceDO> {

    default PageResult<IotDeviceDO> selectPage(IotDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceDO>()
                .likeIfPresent(IotDeviceDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IotDeviceDO::getProductId, reqVO.getProductId())
                .eqIfPresent(IotDeviceDO::getDeviceType, reqVO.getDeviceType())
                .likeIfPresent(IotDeviceDO::getNickname, reqVO.getNickname())
                .eqIfPresent(IotDeviceDO::getState, reqVO.getStatus())
                .apply(ObjectUtil.isNotNull(reqVO.getGroupId()), "FIND_IN_SET(" + reqVO.getGroupId() + ",group_ids) > 0")
                // 空间过滤条件（使用apply直接写SQL避免Lombok编译问题）
                .apply(ObjectUtil.isNotNull(reqVO.getBuildingId()), "building_id = {0}", reqVO.getBuildingId())
                .apply(ObjectUtil.isNotNull(reqVO.getFloorId()), "floor_id = {0}", reqVO.getFloorId())
                .apply(ObjectUtil.isNotNull(reqVO.getAreaId()), "room_id = {0}", reqVO.getAreaId())  // 注意：前端areaId映射到room_id字段
                // 子系统过滤：根据菜单ID筛选（考虑设备配置继承机制）
                .apply(ObjectUtil.isNotNull(reqVO.getSubsystemCode()), buildSubsystemFilter(reqVO.getSubsystemCode()))
                .orderByDesc(IotDeviceDO::getId));
    }
    
    /**
     * 构建子系统筛选条件（考虑菜单继承和父子菜单）
     * 
     * <p>筛选逻辑：
     * 1. 查询 iot_subsystem 表获取子系统对应的 menu_id
     * 2. 设备明确配置了该菜单ID或其子菜单ID
     * 3. 或设备继承产品配置，且产品配置了该菜单ID或其子菜单ID
     * 
     * <p>支持两种 menu_ids 格式：
     * - 逗号分隔字符串: "5219,5139,5140"
     * - JSON 数组: "[5219,5139,5140]"
     * 
     * <p>支持父子菜单：
     * - 如果产品配置了子菜单（如 5139-实时预览），筛选父菜单（5133-视频监控）时也能匹配
     * 
     * @param subsystemCode 子系统代码（如 "security.video"）
     * @return SQL WHERE 条件
     */
    default String buildSubsystemFilter(String subsystemCode) {
        // SQL 子查询：根据 subsystemCode 获取 menu_id
        return String.format(
            "(" +
            "  EXISTS (" +
            "    SELECT 1 FROM iot_subsystem s " +
            "    WHERE s.code = '%s' " +
            "    AND (" +
            // 情况1：设备明确配置了该菜单或其子菜单（menuOverride=1）
            "      (menu_override = 1 AND (" +
            // 直接匹配该菜单ID
            "        JSON_CONTAINS(menu_ids, CAST(s.menu_id AS JSON)) " +
            "        OR FIND_IN_SET(s.menu_id, menu_ids) > 0 " +
            // 或匹配该菜单的任一子菜单
            "        OR EXISTS (" +
            "          SELECT 1 FROM system_menu m " +
            "          WHERE m.parent_id = s.menu_id " +
            "          AND (JSON_CONTAINS(menu_ids, CAST(m.id AS JSON)) OR FIND_IN_SET(m.id, menu_ids) > 0)" +
            "        )" +
            "      )) " +
            // 情况2：设备继承产品配置（menuOverride=0 或 menuIds为空）
            "      OR (" +
            "        (menu_override = 0 OR menu_ids IS NULL OR menu_ids = '') " +
            "        AND EXISTS (" +
            "          SELECT 1 FROM iot_product p " +
            "          WHERE p.id = iot_device.product_id " +
            "          AND (" +
            // 产品直接配置了该菜单ID
            "            JSON_CONTAINS(p.menu_ids, CAST(s.menu_id AS JSON)) " +
            "            OR FIND_IN_SET(s.menu_id, p.menu_ids) > 0 " +
            // 或产品配置了该菜单的任一子菜单
            "            OR EXISTS (" +
            "              SELECT 1 FROM system_menu m " +
            "              WHERE m.parent_id = s.menu_id " +
            "              AND (JSON_CONTAINS(p.menu_ids, CAST(m.id AS JSON)) OR FIND_IN_SET(m.id, p.menu_ids) > 0)" +
            "            )" +
            "          )" +
            "        )" +
            "      )" +
            "    )" +
            "  )" +
            ")",
            subsystemCode
        );
    }

    default IotDeviceDO selectByDeviceName(String deviceName) {
        return selectOne(IotDeviceDO::getDeviceName, deviceName);
    }

    default IotDeviceDO selectByProductKeyAndDeviceName(String productKey, String deviceName) {
        return selectOne(IotDeviceDO::getProductKey, productKey,
                IotDeviceDO::getDeviceName, deviceName);
    }

    default long selectCountByGatewayId(Long id) {
        return selectCount(IotDeviceDO::getGatewayId, id);
    }

    default Long selectCountByProductId(Long productId) {
        return selectCount(IotDeviceDO::getProductId, productId);
    }

    default List<IotDeviceDO> selectListByCondition(@Nullable Integer deviceType,
                                                    @Nullable Long productId) {
        return selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eqIfPresent(IotDeviceDO::getDeviceType, deviceType)
                .eqIfPresent(IotDeviceDO::getProductId, productId));
    }

    default List<IotDeviceDO> selectListByState(Integer state) {
        return selectList(IotDeviceDO::getState, state);
    }

    default List<IotDeviceDO> selectListByProductId(Long productId) {
        return selectList(IotDeviceDO::getProductId, productId);
    }

    default List<IotDeviceDO> selectListByGatewayId(Long gatewayId) {
        return selectList(IotDeviceDO::getGatewayId, gatewayId);
    }

    default Long selectCountByGroupId(Long groupId) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceDO>()
                .apply("FIND_IN_SET(" + groupId + ",group_ids) > 0"));
    }

    default Long selectCountByCreateTime(@Nullable LocalDateTime createTime) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceDO>()
                .geIfPresent(IotDeviceDO::getCreateTime, createTime));
    }

    default List<IotDeviceDO> selectByProductKeyAndDeviceNames(String productKey, Collection<String> deviceNames) {
        return selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getProductKey, productKey)
                .in(IotDeviceDO::getDeviceName, deviceNames));
    }

    default IotDeviceDO selectBySerialNumber(String serialNumber) {
        return selectOne(IotDeviceDO::getSerialNumber, serialNumber);
    }

    /**
     * 根据设备密钥查询设备
     *
     * @param deviceKey 设备密钥
     * @return 设备信息
     */
    default IotDeviceDO selectByDeviceKey(String deviceKey) {
        // 使用 LIMIT 1 避免 TooManyResultsException（当存在重复 deviceKey 时）
        return selectOne(new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getDeviceKey, deviceKey)
                .last("LIMIT 1"));
    }

    /**
     * 查询指定产品下的设备数量
     *
     * @return 产品编号 -> 设备数量的映射
     */
    default Map<Long, Integer> selectDeviceCountMapByProductId() {
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<IotDeviceDO>()
                .select("product_id AS productId", "COUNT(1) AS deviceCount")
                .groupBy("product_id"));
        return result.stream().collect(Collectors.toMap(
            map -> Long.valueOf(map.get("productId").toString()),
            map -> Integer.valueOf(map.get("deviceCount").toString())
        ));
    }

    /**
     * 查询各个状态下的设备数量
     *
     * @return 设备状态 -> 设备数量的映射
     */
    default Map<Integer, Long> selectDeviceCountGroupByState() {
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<IotDeviceDO>()
                .select("state", "COUNT(1) AS deviceCount")
                .groupBy("state"));
        return result.stream().collect(Collectors.toMap(
            map -> Integer.valueOf(map.get("state").toString()),
            map -> Long.valueOf(map.get("deviceCount").toString())
        ));
    }

    /**
     * 按设备类型和状态分组统计设备数量
     *
     * @return 设备类型 -> (设备状态 -> 设备数量) 的嵌套映射
     */
    default Map<Integer, Map<Integer, Long>> selectDeviceCountGroupByTypeAndState() {
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<IotDeviceDO>()
                .select("device_type AS deviceType", "state", "COUNT(1) AS deviceCount")
                .groupBy("device_type", "state"));
        
        Map<Integer, Map<Integer, Long>> typeStateMap = new java.util.HashMap<>();
        for (Map<String, Object> row : result) {
            Object deviceTypeObj = row.get("deviceType");
            Object stateObj = row.get("state");
            Object countObj = row.get("deviceCount");
            
            if (deviceTypeObj == null || stateObj == null || countObj == null) {
                continue;
            }
            
            Integer deviceType = Integer.valueOf(deviceTypeObj.toString());
            Integer state = Integer.valueOf(stateObj.toString());
            Long count = Long.valueOf(countObj.toString());
            
            typeStateMap.computeIfAbsent(deviceType, k -> new java.util.HashMap<>())
                    .put(state, count);
        }
        return typeStateMap;
    }

    /**
     * 查询所有配置了 jobConfig 的设备
     * 
     * @return 设备列表
     */
    default List<IotDeviceDO> selectDevicesWithJobConfig() {
        return selectList(new LambdaQueryWrapper<IotDeviceDO>()
                .isNotNull(IotDeviceDO::getJobConfig)
                .ne(IotDeviceDO::getJobConfig, ""));
    }

    /**
     * 根据状态查询设备数量
     *
     * @param status 设备状态
     * @return 设备数量
     */
    default Long selectCountByStatus(Integer status) {
        return selectCount(IotDeviceDO::getState, status);
    }

    /**
     * 查询安防概览设备列表（考虑菜单继承机制）
     * 
     * 查询规则：
     * 1. 设备明确配置了"安防概览"菜单（menuIds包含5219）
     * 2. 设备继承产品菜单（menuOverride=0 或 menuIds为空），且产品配置了"安防概览"菜单
     * 3. 设备config中features包含"安防概览"
     *
     * @param reqVO 分页请求参数
     * @return 分页结果
     */
    default PageResult<IotDeviceDO> selectSecurityOverviewDevices(SecurityOverviewCameraPageReqVO reqVO) {
        LambdaQueryWrapperX<IotDeviceDO> wrapper = new LambdaQueryWrapperX<IotDeviceDO>()
                // 只查询网络摄像头（product_id=3）
                .eq(IotDeviceDO::getProductId, 3L);
        
        // 如果只要在线设备
        if (reqVO.getOnlineOnly() != null && reqVO.getOnlineOnly()) {
            wrapper.eq(IotDeviceDO::getState, 1);
        }
        
        // 按在线时间倒序
        wrapper.orderByDesc(IotDeviceDO::getOnlineTime)
               .orderByDesc(IotDeviceDO::getId);
        
        return selectPage(reqVO, wrapper);
    }

    /**
     * 根据楼层ID和设备名称查询设备
     * <p>
     * 用于校验同一楼层内设备名称唯一性
     *
     * @param floorId 楼层ID
     * @param deviceName 设备名称
     * @return 设备信息，不存在则返回null
     */
    default IotDeviceDO selectByFloorIdAndDeviceName(Long floorId, String deviceName) {
        return selectOne(new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getFloorId, floorId)
                .eq(IotDeviceDO::getDeviceName, deviceName));
    }

    /**
     * 根据楼层ID和DXF实体ID查询设备
     * <p>
     * 用于识别DXF设备是否已导入
     *
     * @param floorId 楼层ID
     * @param dxfEntityId DXF实体ID
     * @return 设备信息，不存在则返回null
     */
    default IotDeviceDO selectByFloorIdAndDxfEntityId(Long floorId, String dxfEntityId) {
        return selectOne(new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getFloorId, floorId)
                .eq(IotDeviceDO::getDxfEntityId, dxfEntityId));
    }

    /**
     * 批量查询楼层的DXF设备
     * <p>
     * 用于快速匹配DXF图纸中的设备
     *
     * @param floorId 楼层ID
     * @return 包含DXF实体ID的设备列表
     */
    default List<IotDeviceDO> selectDxfDevicesByFloorId(Long floorId) {
        return selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getFloorId, floorId)
                .isNotNull(IotDeviceDO::getDxfEntityId)
                .ne(IotDeviceDO::getDxfEntityId, ""));
    }

}
