package cn.iocoder.yudao.module.iot.service.opc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.opc.vo.OpcZoneConfigCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.opc.vo.OpcZoneConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.opc.vo.OpcZoneConfigUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opc.OpcZoneConfigDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * OPC 防区配置 Service 接口
 *
 * @author 长辉信息科技有限公司
 */
public interface OpcZoneConfigService {

    /**
     * 创建防区配置
     *
     * @param createReqVO 创建信息
     * @return 配置ID
     */
    Long createZoneConfig(@Valid OpcZoneConfigCreateReqVO createReqVO);

    /**
     * 更新防区配置
     *
     * @param updateReqVO 更新信息
     */
    void updateZoneConfig(@Valid OpcZoneConfigUpdateReqVO updateReqVO);

    /**
     * 删除防区配置
     *
     * @param id 配置ID
     */
    void deleteZoneConfig(Long id);

    /**
     * 获得防区配置
     *
     * @param id 配置ID
     * @return 防区配置
     */
    OpcZoneConfigDO getZoneConfig(Long id);

    /**
     * 获得防区配置分页
     *
     * @param pageReqVO 分页查询
     * @return 防区配置分页
     */
    PageResult<OpcZoneConfigDO> getZoneConfigPage(OpcZoneConfigPageReqVO pageReqVO);

    /**
     * 获得设备的所有防区配置
     *
     * @param deviceId 设备ID
     * @return 防区配置列表
     */
    List<OpcZoneConfigDO> getZoneConfigListByDeviceId(Long deviceId);

    /**
     * 根据设备ID、防区号、点位号获取防区配置
     *
     * @param deviceId 设备ID
     * @param area     防区号
     * @param point    点位号
     * @return 防区配置
     */
    OpcZoneConfigDO getZoneConfigByDeviceAndAreaPoint(Long deviceId, Integer area, Integer point);
}
