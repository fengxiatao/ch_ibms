package cn.iocoder.yudao.module.iot.service.ibms.device.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceRuntimeMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsProductMapper;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单台账适配：使用 {@code ibms_device} / {@code ibms_device_runtime} 拼 {@link OtaDeviceView}（供仍消费 legacy DTO 字段子集的路径使用）。
 * <p>用于场景规则、设备消息下发等仍消费 legacy DTO 的路径；查询侧使用 {@link TenantIgnore}。</p>
 */
@Component
public class IbmsIotDualTrackDeviceResolver {

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;
    @Resource
    private IbmsDeviceRuntimeMapper ibmsDeviceRuntimeMapper;
    @Resource
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;
    @Resource
    private IbmsProductMapper ibmsProductMapper;
    @Resource
    private IotProductService iotProductService;

    /**
     * 按设备主键解析兼容壳：仅 IBMS 台账 + 运行态，不允许回退到 {@code iot_device}。
     */
    @TenantIgnore
    public OtaDeviceView getDeviceShellPreferIbmsThenIot(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        IbmsDeviceDO ibms = ibmsDeviceMapper.selectById(deviceId);
        if (ibms != null) {
            IbmsDeviceRuntimeDO rt = ibmsDeviceRuntimeService.getByDeviceId(deviceId);
            return OtaDeviceView.of(ibms, rt);
        }
        return null;
    }

    /**
     * 配置了 {@code jobConfig} 的设备壳：优先 {@code ibms_device_runtime.job_config}（与台账拼壳并写入壳的 jobConfig），
     * 不再合并 {@code iot_device.job_config}（单台账收口）。
     */
    @TenantIgnore
    public List<OtaDeviceView> listDeviceShellsWithJobConfigPreferIbmsMergedWithIot() {
        Map<Long, OtaDeviceView> byId = new LinkedHashMap<>();
        List<IbmsDeviceRuntimeDO> runtimes = ibmsDeviceRuntimeMapper.selectList(
                new LambdaQueryWrapperX<IbmsDeviceRuntimeDO>()
                        .isNotNull(IbmsDeviceRuntimeDO::getJobConfig)
                        .ne(IbmsDeviceRuntimeDO::getJobConfig, ""));
        if (CollUtil.isNotEmpty(runtimes)) {
            for (IbmsDeviceRuntimeDO rt : runtimes) {
                Long did = rt.getDeviceId();
                if (did == null || StrUtil.isBlank(rt.getJobConfig())) {
                    continue;
                }
                IbmsDeviceDO ibms = ibmsDeviceMapper.selectById(did);
                if (ibms == null) {
                    continue;
                }
                OtaDeviceView view = OtaDeviceView.of(ibms, rt);
                if (view != null) {
                    byId.put(did, view);
                }
            }
        }
        return new ArrayList<>(byId.values());
    }

    /**
     * 按产品主键（与 {@code ibms_device.ibms_product_id} 对齐）列设备壳：仅 IBMS 台账，不允许回退到 {@code iot_device}。
     */
    @TenantIgnore
    public List<OtaDeviceView> listDeviceShellsByProductIdPreferIbmsThenIot(Long productId) {
        if (productId == null) {
            return Collections.emptyList();
        }
        List<IbmsDeviceDO> ibmsList = ibmsDeviceMapper.selectListByIbmsProductId(productId);
        if (CollUtil.isEmpty(ibmsList)) {
            return Collections.emptyList();
        }
        List<OtaDeviceView> shells = new ArrayList<>(ibmsList.size());
        for (IbmsDeviceDO ibms : ibmsList) {
            IbmsDeviceRuntimeDO rt = ibmsDeviceRuntimeService.getByDeviceId(ibms.getId());
            OtaDeviceView view = OtaDeviceView.of(ibms, rt);
            if (view != null) {
                shells.add(view);
            }
        }
        return shells;
    }

    /**
     * 规则匹配仅需产品主键：先 {@code iot_product}，再 {@code ibms_product} 兜底为最小壳。
     */
    @TenantIgnore
    public IotProductDO getProductShellPreferIotThenIbms(Long productId) {
        if (productId == null) {
            return null;
        }
        IotProductDO p = iotProductService.getProductFromCache(productId);
        if (p != null) {
            return p;
        }
        IbmsProductDO ibms = ibmsProductMapper.selectById(productId);
        if (ibms == null) {
            return null;
        }
        IotProductDO shell = new IotProductDO();
        shell.setId(ibms.getId());
        return shell;
    }

    /**
     * 离线检测 Job：在线设备 = IBMS 运行态在线（单台账收口）。
     */
    @TenantIgnore
    public List<OtaDeviceView> listOnlineDeviceShellsMergedForOfflineCheck() {
        Map<Long, OtaDeviceView> byId = new LinkedHashMap<>();
        List<Long> ibmsOnlineIds = ibmsDeviceRuntimeMapper.selectOnlineDeviceIds();
        if (CollUtil.isNotEmpty(ibmsOnlineIds)) {
            for (Long bid : ibmsOnlineIds) {
                OtaDeviceView view = getDeviceShellPreferIbmsThenIot(bid);
                if (view != null && view.getId() != null) {
                    byId.put(view.getId(), view);
                }
            }
        }
        return new ArrayList<>(byId.values());
    }
}
