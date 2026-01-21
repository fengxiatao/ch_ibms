package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.area.AreaPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO;

import java.util.List;

/**
 * 区域 Service 接口
 *
 * @author IBMS Team
 */
public interface IotGisAreaService {

    /**
     * 创建区域
     *
     * @param area 区域信息
     * @return 区域ID
     */
    Long createArea(AreaDO area);

    /**
     * 更新区域
     *
     * @param area 区域信息
     */
    void updateArea(AreaDO area);

    /**
     * 删除区域
     *
     * @param id 区域ID
     */
    void deleteArea(Long id);

    /**
     * 获取区域
     *
     * @param id 区域ID
     * @return 区域信息
     */
    AreaDO getArea(Long id);

    /**
     * 获取区域列表
     *
     * @return 区域列表
     */
    List<AreaDO> getAreaList();

    /**
     * 获取区域分页
     *
     * @param pageReqVO 分页请求
     * @return 区域分页
     */
    PageResult<AreaDO> getAreaPage(AreaPageReqVO pageReqVO);

    /**
     * 根据楼层ID获取区域列表
     *
     * @param floorId 楼层ID
     * @return 区域列表
     */
    List<AreaDO> getAreaListByFloorId(Long floorId);
}


