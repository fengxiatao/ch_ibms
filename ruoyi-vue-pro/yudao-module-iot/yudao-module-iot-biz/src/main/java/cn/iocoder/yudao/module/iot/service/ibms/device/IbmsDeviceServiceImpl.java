package cn.iocoder.yudao.module.iot.service.ibms.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceSimpleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceRuntimeMapper;
import cn.iocoder.yudao.module.iot.core.mq.message.DeviceProfileChangedMessage;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceProfileChangedPublisher;
import cn.iocoder.yudao.module.iot.mq.support.DeviceProfileMessageBuilder;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceGroupService;
import cn.iocoder.yudao.module.iot.service.ibms.channel.IbmsChannelService;
import cn.iocoder.yudao.module.iot.service.ibms.facade.IbmsBusinessMappingHelper;
import cn.iocoder.yudao.module.iot.service.ibms.product.IbmsProductService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * IBMS 设备管理 Service 实现
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class IbmsDeviceServiceImpl implements IbmsDeviceService {

    private static final int GATEWAY_PROFILE_REPUSH_BATCH = 500;

    private final IbmsDeviceMapper deviceMapper;
    private final IbmsProductService productService;
    private final IbmsChannelService channelService;
    private final DeviceProfileChangedPublisher deviceProfileChangedPublisher;
    private final IbmsDeviceRuntimeService deviceRuntimeService;
    private final IbmsDeviceRuntimeMapper deviceRuntimeMapper;
    private final IbmsBusinessMappingHelper businessMappingHelper;

    @Resource
    @Lazy
    private IotDeviceGroupService iotDeviceGroupService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDevice(IbmsDeviceSaveReqVO reqVO) {
        IbmsProductRespVO productTemplate = productService.getProductTemplateForDevice(
                reqVO.getGroupCode(), reqVO.getSystemCode(), reqVO.getDeviceTypeCode(), reqVO.getProductModel());
        String modelCode = productTemplate != null ? StrUtil.trimToNull(productTemplate.getModelCode()) : null;
        if (StrUtil.isBlank(modelCode)) {
            throw ServiceExceptionUtil.exception0(400,
                    "无法解析型号码：请确认「产品型号」与已维护的产品模板一致（分组/系统/设备类型/型号需匹配）");
        }

        IbmsDeviceDO device = BeanUtils.toBean(reqVO, IbmsDeviceDO.class);
        device.setId(null);

        String deviceCode = generateDeviceCode(reqVO.getSystemCode(), modelCode,
                reqVO.getDeviceTypeCode(), reqVO.getBrand(), reqVO.getSeq());
        device.setDeviceCode(deviceCode);
        if (deviceMapper.selectByDeviceCode(deviceCode) != null) {
            throw ServiceExceptionUtil.exception0(409, "设备编码已存在，请调整系统/型号码/类型/品牌或序号后重试");
        }

        device.setSn(generateSn());
        device.setProductKey(generateProductKey());
        if (productTemplate != null && productTemplate.getId() != null) {
            device.setIbmsProductId(productTemplate.getId());
        }
        int totalPointCount = 0;
        if (productTemplate != null && productTemplate.getPointTypes() != null) {
            totalPointCount = productTemplate.getPointTypes().stream()
                    .filter(p -> p.getCount() != null && p.getCount() > 0)
                    .mapToInt(p -> p.getCount() != null ? p.getCount() : 0)
                    .sum();
        }
        device.setPointCount(totalPointCount);
        device.setPointsOnline(totalPointCount);
        device.setPointsAlarm(0);

        try {
            deviceMapper.insert(device);
        } catch (DuplicateKeyException ex) {
            // 并发创建时可能越过前置查重，兜底转换为业务错误提示
            throw ServiceExceptionUtil.exception0(409, "设备编码已存在，请勿重复提交");
        }
        deviceRuntimeService.ensureRowForDevice(device);
        // 自动生成通道（设备创建成功后再落库，确保 device_id、device_sn 等冗余字段可用）
        if (productTemplate != null && totalPointCount > 0) {
            generateChannelsFromProductTemplate(device, productTemplate);
        }
        channelService.syncRuntimeByDevice(device.getId(), true, 0);
        IbmsDeviceDO persisted = deviceMapper.selectById(device.getId());
        publishProfileIfPresent(persisted, DeviceProfileChangedMessage.OP_UPSERT);
        return device.getId();
    }

    private void generateChannelsFromProductTemplate(IbmsDeviceDO device, IbmsProductRespVO productTemplate) {
        // 通道编码：{设备编码}-{点位类型}{两位序号}，例如 VI-NV-NVR-DAH-001-VT01
        String deviceCode = device.getDeviceCode();
        String systemType = device.getSystemCode();

        // 设备状态/业务分类是通道必填字段，这里先按系统类型做可复现映射（后续可以再细化到类型级）
        String business = resolveBusinessBySystemType(systemType);
        String status = resolveStatusBySystemType(systemType);

        List<? extends cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductPointTypeVO> pointTypes =
                productTemplate.getPointTypes();
        if (pointTypes == null || pointTypes.isEmpty()) {
            return;
        }

        for (var pointType : pointTypes) {
            Integer count = pointType.getCount();
            if (count == null || count <= 0) {
                continue;
            }
            String typeCode = pointType.getPointTypeCode();
            String baseName = pointType.getName();
            for (int i = 1; i <= count; i++) {
                String channelCode = deviceCode + "-" + typeCode + String.format("%02d", i);
                String channelName = (baseName != null && !baseName.isBlank()) ? baseName + "-" + String.format("%03d", i)
                        : typeCode + "-" + String.format("%03d", i);

                cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelSaveReqVO channelReq =
                        new cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelSaveReqVO();
                channelReq.setSpaceId(null); // 当前原型以 space 文案展示为主，spaceId 可后续再做更严格绑定
                channelReq.setDeviceId(device.getId());
                channelReq.setCode(channelCode);
                channelReq.setChannelNo(i);
                channelReq.setName(channelName);
                channelReq.setBusiness(business);
                channelReq.setTypeCode(typeCode);
                channelReq.setCategory(resolveCategoryByTypeCode(typeCode, pointType.getName()));
                channelReq.setSystemType(systemType);
                channelReq.setStatus(status);
                channelReq.setDataSource(resolveDataSourceByTypeCode(typeCode));
                channelReq.setIp(device.getIp());
                channelReq.setDeviceSn(device.getSn());
                channelReq.setDeviceName(device.getName());
                channelReq.setSpace(device.getSpace());
                channelReq.setCurrentValue(resolveDefaultCurrentValue(typeCode));
                channelReq.setExtra(buildChannelExtraTemplate(pointType, i));

                try {
                    channelService.createChannel(channelReq);
                } catch (DuplicateKeyException ignore) {
                    // 幂等兜底：如果通道编码已存在，直接跳过
                }
            }
        }
    }

    private String resolveBusinessBySystemType(String systemType) {
        // 单一事实源：复用 IbmsBusinessMappingHelper，返回小写大类码（sa/st/sb/se/sf）
        // 与 IbmsChannelServiceImpl.deriveBusinessFromSystemType 行为对齐
        String group = businessMappingHelper.resolveGroupBySystem(systemType);
        return StrUtil.isNotBlank(group) ? group.toLowerCase() : "sa";
    }

    private String resolveStatusBySystemType(String systemType) {
        // 复现你们 seed 示例：VI -> online，AC/门禁 -> armed，AL/告警 -> warning
        return switch (systemType) {
            case "AC", "IC" -> "armed";
            case "AL", "FD", "PA" -> "warning";
            default -> "online";
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDevice(IbmsDeviceSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception0(400, "设备 ID 不能为空");
        }
        IbmsDeviceDO exist = deviceMapper.selectById(reqVO.getId());
        if (exist == null) {
            throw ServiceExceptionUtil.exception0(404, "设备不存在");
        }
        IbmsDeviceDO update = BeanUtils.toBean(reqVO, IbmsDeviceDO.class);
        // 编码 / SN / ProductKey 不允许通过更新接口修改
        update.setDeviceCode(exist.getDeviceCode());
        update.setSn(exist.getSn());
        update.setProductKey(exist.getProductKey());
        // extra 不传则保留原值，避免 Bean 拷贝把 null 写入库
        if (reqVO.getExtra() == null) {
            update.setExtra(exist.getExtra());
        }
        deviceMapper.updateById(update);
        IbmsDeviceDO persisted = deviceMapper.selectById(reqVO.getId());
        publishProfileIfPresent(persisted, DeviceProfileChangedMessage.OP_UPSERT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long id) {
        IbmsDeviceDO exist = deviceMapper.selectById(id);
        if (exist == null) {
            return;
        }
        deviceProfileChangedPublisher.publish(DeviceProfileMessageBuilder.deleteIbms(id, exist.getTenantId()));
        // 预留：如后续有通道/空间引用设备，在这里增加引用校验
        deviceRuntimeService.deleteByDeviceId(id);
        deviceMapper.deleteById(id);
    }

    @Override
    public IbmsDeviceRespVO getDevice(Long id) {
        IbmsDeviceDO device = deviceMapper.selectById(id);
        if (device == null) {
            return null;
        }
        IbmsDeviceRespVO vo = BeanUtils.toBean(device, IbmsDeviceRespVO.class);
        fillRuntimeState(List.of(vo));
        return vo;
    }

    @Override
    public PageResult<IbmsDeviceRespVO> getDevicePage(IbmsDevicePageReqVO reqVO) {
        PageResult<IbmsDeviceDO> page = deviceMapper.selectPage(reqVO);
        PageResult<IbmsDeviceRespVO> result = BeanUtils.toBean(page, IbmsDeviceRespVO.class);
        fillRuntimeState(result.getList());
        return result;
    }

    private void fillRuntimeState(List<IbmsDeviceRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<Long> ids = convertList(list, IbmsDeviceRespVO::getId);
        Map<Long, Integer> stateMap = deviceRuntimeMapper.selectStateMapByDeviceIds(ids);
        for (IbmsDeviceRespVO vo : list) {
            if (vo.getId() != null && stateMap.containsKey(vo.getId())) {
                vo.setState(stateMap.get(vo.getId()));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncRuntime(Long id, boolean online, Integer pointsAlarm) {
        if (id == null) {
            throw ServiceExceptionUtil.exception0(400, "设备 ID 不能为空");
        }
        IbmsDeviceDO exist = deviceMapper.selectById(id);
        if (exist == null) {
            throw ServiceExceptionUtil.exception0(404, "设备不存在");
        }
        int total = exist.getPointCount() != null ? exist.getPointCount() : 0;
        int alarm = pointsAlarm != null ? Math.max(pointsAlarm, 0) : 0;
        int onlineCount = online ? Math.max(total - alarm, 0) : 0;
        deviceMapper.updateById(IbmsDeviceDO.builder()
                .id(id)
                .pointsAlarm(alarm)
                .pointsOnline(onlineCount)
                .build());
        channelService.syncRuntimeByDevice(id, online, alarm);
    }

    private String resolveCategoryByTypeCode(String typeCode, String pointTypeName) {
        if (pointTypeName != null && !pointTypeName.isBlank()) {
            return pointTypeName;
        }
        return switch (safeTypeCode(typeCode)) {
            case "VT", "VT-SUB", "VT-IN", "VT-OUT" -> "视频监控";
            case "DR", "DR-READER" -> "门禁通行";
            case "DI", "DO", "AI", "AO", "AI_AN", "AO_AN" -> "楼宇点位";
            case "PM" -> "能源计量";
            case "LT" -> "智能照明";
            case "FP" -> "消防报警";
            case "BC" -> "广播通道";
            default -> "通道";
        };
    }

    private String resolveDataSourceByTypeCode(String typeCode) {
        String t = safeTypeCode(typeCode);
        if (t.startsWith("VT")) {
            return "NVR";
        }
        if ("DR".equals(t) || "DR-READER".equals(t)) {
            return "CTR";
        }
        if ("PM".equals(t)) {
            return "Meter";
        }
        if ("DI".equals(t) || "DO".equals(t) || "AI".equals(t) || "AO".equals(t)
                || "AI_AN".equals(t) || "AO_AN".equals(t)) {
            return "DDC";
        }
        return "GW";
    }

    private String resolveDefaultCurrentValue(String typeCode) {
        return switch (safeTypeCode(typeCode)) {
            case "VT", "VT-SUB", "VT-IN", "VT-OUT" -> "在线";
            case "DI", "DR", "DR-READER", "FP", "AI" -> "0";
            case "DO", "AO", "AO_AN", "LT", "BC" -> "OFF";
            case "PM", "AI_AN" -> "正常";
            default -> "--";
        };
    }

    private String buildChannelExtraTemplate(
            cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductPointTypeVO pointType,
            int channelNo) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("pointTypeCode", pointType.getPointTypeCode());
        extra.put("dataType", pointType.getDataType());
        extra.put("templateName", pointType.getName());
        extra.put("channelNo", channelNo);
        extra.put("runtimeKey", resolveRuntimeKeyByTypeCode(pointType.getPointTypeCode()));
        return JSONUtil.toJsonStr(extra);
    }

    private String resolveRuntimeKeyByTypeCode(String typeCode) {
        return switch (safeTypeCode(typeCode)) {
            case "VT", "VT-SUB", "VT-IN", "VT-OUT" -> "stream_status";
            case "DI", "DO", "AI", "AO", "AI_AN", "AO_AN" -> "io_value";
            case "DR", "DR-READER" -> "door_state";
            case "PM" -> "meter_value";
            case "LT" -> "light_state";
            case "FP" -> "fire_alarm_state";
            case "BC" -> "broadcast_state";
            default -> "channel_value";
        };
    }

    private String safeTypeCode(String typeCode) {
        return typeCode == null ? "" : typeCode.trim().toUpperCase();
    }

    /**
     * 生成设备编码：{系统}-{型号码}-{设备类型}-{品牌}-{流水}
     * 示例：VI-NV-NVR-DAH-001（空间不参与编码，安装位置用 space/space_id 单独维护）
     */
    private String generateDeviceCode(String system, String modelCode, String deviceType,
                                      String brand, Integer seq) {
        String safeSystem = StrUtil.blankToDefault(system, "VI").trim();
        String safeModel = StrUtil.blankToDefault(modelCode, "UNK").trim();
        String safeDeviceType = StrUtil.blankToDefault(deviceType, "CAM").trim();
        String safeBrand = StrUtil.blankToDefault(brand, "UNK").trim();
        String seqStr = String.format("%03d", seq != null ? seq : 1);
        return safeSystem + "-" + safeModel + "-" + safeDeviceType + "-" + safeBrand + "-" + seqStr;
    }

    /** 简单生成设备序列号 */
    private String generateSn() {
        return "SN-" + RandomUtil.randomString(10).toUpperCase();
    }

    /** 简单生成 ProductKey */
    private String generateProductKey() {
        return "PK-" + UUID.randomUUID();
    }

    @Override
    public int repushAllGatewayProfiles() {
        int total = 0;
        Long lastId = 0L;
        while (true) {
            List<IbmsDeviceDO> batch = deviceMapper.selectList(new LambdaQueryWrapperX<IbmsDeviceDO>()
                    .gt(IbmsDeviceDO::getId, lastId)
                    .orderByAsc(IbmsDeviceDO::getId)
                    .last("LIMIT " + GATEWAY_PROFILE_REPUSH_BATCH));
            if (batch == null || batch.isEmpty()) {
                break;
            }
            for (IbmsDeviceDO d : batch) {
                publishProfileIfPresent(d, DeviceProfileChangedMessage.OP_UPSERT);
                total++;
            }
            lastId = batch.get(batch.size() - 1).getId();
            if (batch.size() < GATEWAY_PROFILE_REPUSH_BATCH) {
                break;
            }
        }
        log.info("[repushAllGatewayProfiles] 已向网关重推 IBMS profile 条数={}", total);
        return total;
    }

    @Override
    @TenantIgnore
    public boolean isDeviceExistsByIp(String ip) {
        if (StrUtil.isBlank(ip)) {
            return false;
        }
        return deviceMapper.selectCount(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .eq(IbmsDeviceDO::getIp, ip.trim())) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceGroup(Set<Long> deviceIds, Set<Long> groupIds) {
        if (CollUtil.isEmpty(deviceIds) || CollUtil.isEmpty(groupIds)) {
            return;
        }
        iotDeviceGroupService.validateDeviceGroupExists(groupIds);
        List<IbmsDeviceDO> devices = deviceMapper.selectByIds(deviceIds);
        if (CollUtil.isEmpty(devices)) {
            return;
        }
        for (IbmsDeviceDO d : devices) {
            deviceMapper.updateById(IbmsDeviceDO.builder().id(d.getId()).groupIds(groupIds).build());
            IbmsDeviceDO persisted = deviceMapper.selectById(d.getId());
            publishProfileIfPresent(persisted, DeviceProfileChangedMessage.OP_UPSERT);
        }
    }

    @Override
    public long countDevicesByProduct(Long ibmsProductId) {
        if (ibmsProductId == null) {
            return 0L;
        }
        return deviceMapper.selectCountByIbmsProductId(ibmsProductId);
    }

    @Override
    public List<IbmsDeviceSimpleRespVO> listSimpleDevices(Integer deviceType, Long ibmsProductId) {
        List<IbmsDeviceDO> list = deviceMapper.selectSimpleList(deviceType, ibmsProductId);
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        List<Long> ids = convertList(list, IbmsDeviceDO::getId);
        Map<Long, Integer> stateMap = deviceRuntimeMapper.selectStateMapByDeviceIds(ids);
        List<IbmsDeviceSimpleRespVO> out = new ArrayList<>(list.size());
        for (IbmsDeviceDO d : list) {
            IbmsDeviceSimpleRespVO vo = new IbmsDeviceSimpleRespVO();
            vo.setId(d.getId());
            vo.setName(d.getName());
            vo.setIbmsProductId(d.getIbmsProductId());
            vo.setProductKey(d.getProductKey());
            vo.setDeviceType(d.getDeviceType());
            vo.setState(stateMap.get(d.getId()));
            out.add(vo);
        }
        return out;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeviceList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            deleteDevice(id);
        }
    }

    private void publishProfileIfPresent(IbmsDeviceDO d, String op) {
        if (d == null) {
            return;
        }
        var msg = DeviceProfileMessageBuilder.fromIbms(d, op);
        if (msg != null) {
            deviceProfileChangedPublisher.publish(msg);
        }
    }
}

