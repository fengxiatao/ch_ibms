package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolRoutePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolRouteRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolRouteSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolRouteDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolRoutePointDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 电子巡更 - 巡更路线 Service 接口
 *
 * @author 长辉信息
 */
public interface EpatrolRouteService {

    /**
     * 创建巡更路线
     */
    Long createRoute(@Valid EpatrolRouteSaveReqVO createReqVO);

    /**
     * 更新巡更路线
     */
    void updateRoute(@Valid EpatrolRouteSaveReqVO updateReqVO);

    /**
     * 删除巡更路线
     */
    void deleteRoute(Long id);

    /**
     * 获得巡更路线
     */
    EpatrolRouteDO getRoute(Long id);

    /**
     * 获得巡更路线详情（包含点位列表）
     */
    EpatrolRouteRespVO getRouteDetail(Long id);

    /**
     * 获得巡更路线分页
     */
    PageResult<EpatrolRouteDO> getRoutePage(EpatrolRoutePageReqVO pageReqVO);

    /**
     * 获得所有启用的巡更路线
     */
    List<EpatrolRouteDO> getEnabledRouteList();

    /**
     * 更新巡更路线状态
     */
    void updateRouteStatus(Long id, Integer status);

    /**
     * 获取路线的点位列表
     */
    List<EpatrolRoutePointDO> getRoutePoints(Long routeId);

}
