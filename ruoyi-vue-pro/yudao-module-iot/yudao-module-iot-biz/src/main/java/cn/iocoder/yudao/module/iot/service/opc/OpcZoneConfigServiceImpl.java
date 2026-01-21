package cn.iocoder.yudao.module.iot.service.opc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.opc.vo.OpcZoneConfigCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.opc.vo.OpcZoneConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.opc.vo.OpcZoneConfigUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opc.OpcZoneConfigDO;
import cn.iocoder.yudao.module.iot.dal.mysql.opc.OpcZoneConfigMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * OPC 防区配置 Service 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class OpcZoneConfigServiceImpl implements OpcZoneConfigService {

    @Resource
    private OpcZoneConfigMapper zoneConfigMapper;

    @Resource
    private IotDeviceService deviceService;

    @Override
    public Long createZoneConfig(OpcZoneConfigCreateReqVO createReqVO) {
        // 1. 校验设备是否存在
        validateDeviceExists(createReqVO.getDeviceId());

        // 2. 校验防区号和点位号是否已存在
        validateZoneConfigNotExists(createReqVO.getDeviceId(), createReqVO.getArea(), createReqVO.getPoint());

        // 3. 转换并插入
        OpcZoneConfigDO config = BeanUtils.toBean(createReqVO, OpcZoneConfigDO.class);
        if (config.getEnabled() == null) {
            config.setEnabled(true);
        }
        zoneConfigMapper.insert(config);

        log.info("[createZoneConfig][创建防区配置成功] id={}, deviceId={}, area={}, point={}",
                config.getId(), config.getDeviceId(), config.getArea(), config.getPoint());
        return config.getId();
    }

    @Override
    public void updateZoneConfig(OpcZoneConfigUpdateReqVO updateReqVO) {
        // 1. 校验配置是否存在
        validateZoneConfigExists(updateReqVO.getId());

        // 2. 如果修改了设备ID，校验新设备是否存在
        if (updateReqVO.getDeviceId() != null) {
            validateDeviceExists(updateReqVO.getDeviceId());
        }

        // 3. 更新
        OpcZoneConfigDO config = BeanUtils.toBean(updateReqVO, OpcZoneConfigDO.class);
        zoneConfigMapper.updateById(config);

        log.info("[updateZoneConfig][更新防区配置成功] id={}", updateReqVO.getId());
    }

    @Override
    public void deleteZoneConfig(Long id) {
        // 校验存在
        validateZoneConfigExists(id);

        // 删除
        zoneConfigMapper.deleteById(id);

        log.info("[deleteZoneConfig][删除防区配置成功] id={}", id);
    }

    @Override
    public OpcZoneConfigDO getZoneConfig(Long id) {
        return zoneConfigMapper.selectById(id);
    }

    @Override
    public PageResult<OpcZoneConfigDO> getZoneConfigPage(OpcZoneConfigPageReqVO pageReqVO) {
        return zoneConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OpcZoneConfigDO> getZoneConfigListByDeviceId(Long deviceId) {
        return zoneConfigMapper.selectList(OpcZoneConfigDO::getDeviceId, deviceId);
    }

    @Override
    public OpcZoneConfigDO getZoneConfigByDeviceAndAreaPoint(Long deviceId, Integer area, Integer point) {
        return zoneConfigMapper.selectByDeviceAndAreaPoint(deviceId, area, point);
    }

    /**
     * 校验设备是否存在
     */
    private void validateDeviceExists(Long deviceId) {
        IotDeviceDO device = deviceService.getDevice(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
    }

    /**
     * 校验防区配置是否存在
     */
    private void validateZoneConfigExists(Long id) {
        if (zoneConfigMapper.selectById(id) == null) {
            throw exception(OPC_ZONE_CONFIG_NOT_EXISTS);
        }
    }

    /**
     * 校验防区配置是否已存在
     */
    private void validateZoneConfigNotExists(Long deviceId, Integer area, Integer point) {
        OpcZoneConfigDO existing = zoneConfigMapper.selectByDeviceAndAreaPoint(deviceId, area, point);
        if (existing != null) {
            throw exception(OPC_ZONE_CONFIG_EXISTS);
        }
    }
}
