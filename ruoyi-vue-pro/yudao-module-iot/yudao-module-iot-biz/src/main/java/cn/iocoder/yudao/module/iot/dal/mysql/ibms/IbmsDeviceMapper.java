package cn.iocoder.yudao.module.iot.dal.mysql.ibms;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDevicePageReqVO;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IBMS 设备 Mapper
 */
@Mapper
public interface IbmsDeviceMapper extends BaseMapperX<IbmsDeviceDO> {

    default PageResult<IbmsDeviceDO> selectPage(IbmsDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsDeviceDO>()
                .likeIfPresent(IbmsDeviceDO::getName, reqVO.getKeyword())
                .likeIfPresent(IbmsDeviceDO::getDeviceCode, reqVO.getKeyword())
                .eqIfPresent(IbmsDeviceDO::getGroupCode, reqVO.getGroupCode())
                .eqIfPresent(IbmsDeviceDO::getSystemCode, reqVO.getSystemCode())
                .eqIfPresent(IbmsDeviceDO::getDeviceTypeCode, reqVO.getDeviceTypeCode())
                .eqIfPresent(IbmsDeviceDO::getBrand, reqVO.getBrand())
                .eqIfPresent(IbmsDeviceDO::getAccessType, reqVO.getAccessType())
                .eqIfPresent(IbmsDeviceDO::getIbmsProductId, reqVO.getIbmsProductId())
                .betweenIfPresent(IbmsDeviceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IbmsDeviceDO::getId));
    }

    default IbmsDeviceDO selectByDeviceCode(String deviceCode) {
        return selectOne(IbmsDeviceDO::getDeviceCode, deviceCode);
    }

    /**
     * 按租户 + IP 查找设备（用于发现激活幂等：同 IP 更新同一台账）
     */
    default IbmsDeviceDO selectByTenantIdAndIp(Long tenantId, String ip) {
        if (tenantId == null || ip == null || ip.isEmpty()) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .eq(IbmsDeviceDO::getTenantId, tenantId)
                .eq(IbmsDeviceDO::getIp, ip));
    }

    /**
     * 网关 MQTT 认证：产品 Key + 设备名称（与 {@code ibms_device} 列一致）。
     */
    default IbmsDeviceDO selectByProductKeyAndName(String productKey, String name) {
        if (StrUtil.hasBlank(productKey, name)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .eq(IbmsDeviceDO::getProductKey, productKey.trim())
                .eq(IbmsDeviceDO::getName, name.trim()));
    }

    /**
     * 报警主机等使用的 deviceKey：优先 {@code device_code}，其次 {@code extra.deviceKey}。
     */
    default IbmsDeviceDO selectByGatewayDeviceKey(String deviceKey) {
        if (StrUtil.isBlank(deviceKey)) {
            return null;
        }
        String k = deviceKey.trim();
        IbmsDeviceDO byCode = selectOne(IbmsDeviceDO::getDeviceCode, k);
        if (byCode != null) {
            return byCode;
        }
        return selectOne(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .apply("JSON_UNQUOTE(JSON_EXTRACT(extra, '$.deviceKey')) = {0}", k));
    }

    /**
     * 网关运行态在线设备（依赖 {@code ibms_device.extra.gatewayRuntimeState}，与 {@link cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum#ONLINE} 对齐）。
     */
    default List<IbmsDeviceDO> selectListByGatewayRuntimeState(int state) {
        return selectList(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .apply("JSON_EXTRACT(extra, '$.gatewayRuntimeState') = {0}", state));
    }

    /**
     * 原 {@code getDeviceListBySubsystemCode("access")} 语义：门禁相关系统码。
     */
    default List<IbmsDeviceDO> selectListAccessLikeDevices() {
        return selectList(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .in(IbmsDeviceDO::getSystemCode, "AC", "IC"));
    }

    /**
     * 按 IBMS 产品主键筛选设备（对齐历史 {@code iot_device.product_id} 场景，如 NVR 产品 id=4）。
     */
    default List<IbmsDeviceDO> selectListByIbmsProductId(Long ibmsProductId) {
        if (ibmsProductId == null) {
            return List.of();
        }
        return selectList(IbmsDeviceDO::getIbmsProductId, ibmsProductId);
    }

    /**
     * 识别 NVR 台账（满足任一）：{@code ibms_product_id=4}、{@code device_type_code=NVR}、
     * {@code extra.deviceType=NVR}、运行态 {@code config.deviceType=NVR}。
     * <p>与历史 iot_device NVR 识别语义对齐。</p>
     */
    default List<IbmsDeviceDO> selectNvrLikeDevices() {
        // 注意：若需按运行态 config.deviceType 识别 NVR，请先确保已建表 ibms_device_runtime，再考虑增加 EXISTS 子查询。
        return selectList(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .and(w -> w
                        .eq(IbmsDeviceDO::getIbmsProductId, 4L)
                        .or()
                        .eq(IbmsDeviceDO::getDeviceTypeCode, "NVR")
                        .or()
                        .apply("JSON_UNQUOTE(JSON_EXTRACT(extra, '$.deviceType')) = 'NVR'")
                ));
    }

    default Long selectCountByCreateTime(LocalDateTime createTime) {
        return selectCount(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .geIfPresent(IbmsDeviceDO::getCreateTime, createTime));
    }

    @Select("SELECT COALESCE(r.state, 0) AS st, COUNT(*) AS cnt FROM ibms_device d "
            + "LEFT JOIN ibms_device_runtime r ON r.device_id = d.id AND r.deleted = 0 "
            + "WHERE d.deleted = 0 "
            + "GROUP BY COALESCE(r.state, 0)")
    List<Map<String, Object>> selectIbmsDeviceStateCountRows();

    default Map<Integer, Long> selectDeviceCountMapByState() {
        List<Map<String, Object>> rows = selectIbmsDeviceStateCountRows();
        Map<Integer, Long> m = new HashMap<>();
        if (rows == null) {
            return m;
        }
        for (Map<String, Object> row : rows) {
            Object st = row.get("st");
            Object cnt = row.get("cnt");
            if (st instanceof Number && cnt instanceof Number) {
                m.put(((Number) st).intValue(), ((Number) cnt).longValue());
            }
        }
        return m;
    }

    /**
     * 分组下 IBMS 台账数量（{@code group_ids} 为逗号分隔 ID，与 {@link cn.iocoder.yudao.framework.mybatis.core.type.LongSetTypeHandler} 一致）。
     */
    default Long selectCountByGroupId(Long groupId) {
        if (groupId == null) {
            return 0L;
        }
        return selectCount(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .apply("FIND_IN_SET({0}, group_ids)", String.valueOf(groupId)));
    }

    /**
     * 指定 IBMS 产品下的台账设备数（与 {@code iot_product.id} 对齐迁移时，可与 {@link cn.iocoder.yudao.module.iot.service.device.IotDeviceService#getDeviceCountByProductId} 双轨相加）。
     */
    default Long selectCountByIbmsProductId(Long ibmsProductId) {
        if (ibmsProductId == null) {
            return 0L;
        }
        return selectCount(IbmsDeviceDO::getIbmsProductId, ibmsProductId);
    }

    /**
     * 精简列表：按数值型 {@code deviceType} 与 {@code ibms_product_id} 过滤（与历史 simple-list 参数对齐）。
     */
    default List<IbmsDeviceDO> selectSimpleList(Integer deviceType, Long ibmsProductId) {
        return selectList(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .eq(deviceType != null, IbmsDeviceDO::getDeviceType, deviceType)
                .eq(ibmsProductId != null, IbmsDeviceDO::getIbmsProductId, ibmsProductId)
                .orderByDesc(IbmsDeviceDO::getId));
    }

    /**
     * 在指定设备编码前缀（含末尾 {@code -}，如 {@code AL-AL-C-SERVER-OTH-}）下，取流水号三段数字最大值。
     */
    default int selectMaxNumericSuffixByDeviceCodePrefix(String prefix) {
        if (StrUtil.isBlank(prefix)) {
            return 0;
        }
        List<IbmsDeviceDO> rows = selectList(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .likeRight(IbmsDeviceDO::getDeviceCode, prefix)
                .select(IbmsDeviceDO::getDeviceCode));
        int max = 0;
        if (rows == null) {
            return 0;
        }
        for (IbmsDeviceDO d : rows) {
            String dc = d.getDeviceCode();
            if (StrUtil.isBlank(dc)) {
                continue;
            }
            String last = StrUtil.subAfter(dc, '-', true);
            if (StrUtil.isNotBlank(last) && StrUtil.isNumeric(last)) {
                max = Math.max(max, Integer.parseInt(last));
            }
        }
        return max;
    }

    /**
     * 与历史 iot_device 按设备名称单行语义对齐（单租户拦截器内）。
     */
    default IbmsDeviceDO selectByDeviceName(String deviceName) {
        if (StrUtil.isBlank(deviceName)) {
            return null;
        }
        return selectOne(IbmsDeviceDO::getName, deviceName.trim());
    }

    /**
     * 与历史 iot_device 按序列号语义对齐：{@code ibms_device.sn}。
     */
    default IbmsDeviceDO selectBySn(String serialNumber) {
        if (StrUtil.isBlank(serialNumber)) {
            return null;
        }
        return selectOne(IbmsDeviceDO::getSn, serialNumber.trim());
    }

    /**
     * 同一楼层内设备名称唯一性（楼层在 {@code ibms_device_runtime.floor_id}）。
     */
    default IbmsDeviceDO selectByFloorIdAndDeviceName(Long floorId, String deviceName) {
        if (floorId == null || StrUtil.isBlank(deviceName)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .eq(IbmsDeviceDO::getName, deviceName.trim())
                .apply("EXISTS (SELECT 1 FROM ibms_device_runtime r WHERE r.device_id = ibms_device.id "
                        + "AND r.deleted = 0 AND r.floor_id = {0})", floorId));
    }

    /**
     * DXF 导入幂等：同楼层 + DXF 实体 ID。
     */
    default IbmsDeviceDO selectByFloorIdAndDxfEntityId(Long floorId, String dxfEntityId) {
        if (floorId == null || StrUtil.isBlank(dxfEntityId)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .eq(IbmsDeviceDO::getDxfEntityId, dxfEntityId.trim())
                .apply("EXISTS (SELECT 1 FROM ibms_device_runtime r WHERE r.device_id = ibms_device.id "
                        + "AND r.deleted = 0 AND r.floor_id = {0})", floorId));
    }

    /**
     * 与历史 {@code iot_device} 上「同产品 Key + 设备名」唯一语义对齐。
     */
    default IbmsDeviceDO selectByProductKeyAndDeviceName(String productKey, String deviceName) {
        if (StrUtil.hasBlank(productKey, deviceName)) {
            return null;
        }
        return selectByProductKeyAndName(productKey.trim(), deviceName.trim());
    }
}

