package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolRoutePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolRouteRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolRouteSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolRouteDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolRoutePointDO;
import cn.iocoder.yudao.module.iot.dal.mysql.epatrol.EpatrolPointMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.epatrol.EpatrolRouteMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.epatrol.EpatrolRoutePointMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 电子巡更 - 巡更路线 Service 实现类
 *
 * @author 长辉信息
 */
@Service
@Validated
public class EpatrolRouteServiceImpl implements EpatrolRouteService {

    @Resource
    private EpatrolRouteMapper routeMapper;

    @Resource
    private EpatrolRoutePointMapper routePointMapper;

    @Resource
    private EpatrolPointMapper pointMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRoute(EpatrolRouteSaveReqVO createReqVO) {
        // 计算路线总耗时
        int totalDuration = 0;
        if (CollUtil.isNotEmpty(createReqVO.getPoints())) {
            for (EpatrolRouteSaveReqVO.RoutePointItem item : createReqVO.getPoints()) {
                totalDuration += item.getIntervalMinutes() != null ? item.getIntervalMinutes() : 0;
            }
        }

        // 插入路线
        EpatrolRouteDO route = new EpatrolRouteDO();
        route.setRouteName(createReqVO.getRouteName());
        route.setPointCount(createReqVO.getPoints().size());
        route.setTotalDuration(totalDuration);
        route.setStatus(1); // 默认启用
        route.setRemark(createReqVO.getRemark());
        routeMapper.insert(route);

        // 插入路线点位关联
        if (CollUtil.isNotEmpty(createReqVO.getPoints())) {
            for (EpatrolRouteSaveReqVO.RoutePointItem item : createReqVO.getPoints()) {
                EpatrolRoutePointDO routePoint = new EpatrolRoutePointDO();
                routePoint.setRouteId(route.getId());
                routePoint.setPointId(item.getPointId());
                routePoint.setSort(item.getSort());
                routePoint.setIntervalMinutes(item.getIntervalMinutes() != null ? item.getIntervalMinutes() : 0);
                routePointMapper.insert(routePoint);
            }
        }

        return route.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoute(EpatrolRouteSaveReqVO updateReqVO) {
        // 校验存在
        validateRouteExists(updateReqVO.getId());

        // 计算路线总耗时
        int totalDuration = 0;
        if (CollUtil.isNotEmpty(updateReqVO.getPoints())) {
            for (EpatrolRouteSaveReqVO.RoutePointItem item : updateReqVO.getPoints()) {
                totalDuration += item.getIntervalMinutes() != null ? item.getIntervalMinutes() : 0;
            }
        }

        // 更新路线
        EpatrolRouteDO updateObj = new EpatrolRouteDO();
        updateObj.setId(updateReqVO.getId());
        updateObj.setRouteName(updateReqVO.getRouteName());
        updateObj.setPointCount(updateReqVO.getPoints().size());
        updateObj.setTotalDuration(totalDuration);
        updateObj.setRemark(updateReqVO.getRemark());
        routeMapper.updateById(updateObj);

        // 删除旧的路线点位关联
        routePointMapper.deleteByRouteId(updateReqVO.getId());

        // 插入新的路线点位关联
        if (CollUtil.isNotEmpty(updateReqVO.getPoints())) {
            for (EpatrolRouteSaveReqVO.RoutePointItem item : updateReqVO.getPoints()) {
                EpatrolRoutePointDO routePoint = new EpatrolRoutePointDO();
                routePoint.setRouteId(updateReqVO.getId());
                routePoint.setPointId(item.getPointId());
                routePoint.setSort(item.getSort());
                routePoint.setIntervalMinutes(item.getIntervalMinutes() != null ? item.getIntervalMinutes() : 0);
                routePointMapper.insert(routePoint);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoute(Long id) {
        // 校验存在
        validateRouteExists(id);
        // 删除路线点位关联
        routePointMapper.deleteByRouteId(id);
        // 删除路线
        routeMapper.deleteById(id);
    }

    private void validateRouteExists(Long id) {
        if (routeMapper.selectById(id) == null) {
            throw exception(EPATROL_ROUTE_NOT_EXISTS);
        }
    }

    @Override
    public EpatrolRouteDO getRoute(Long id) {
        return routeMapper.selectById(id);
    }

    @Override
    public EpatrolRouteRespVO getRouteDetail(Long id) {
        EpatrolRouteDO route = routeMapper.selectById(id);
        if (route == null) {
            return null;
        }

        EpatrolRouteRespVO respVO = BeanUtils.toBean(route, EpatrolRouteRespVO.class);

        // 获取路线点位
        List<EpatrolRoutePointDO> routePoints = routePointMapper.selectByRouteId(id);
        if (CollUtil.isNotEmpty(routePoints)) {
            // 获取点位详情
            List<Long> pointIds = routePoints.stream().map(EpatrolRoutePointDO::getPointId).collect(Collectors.toList());
            List<EpatrolPointDO> points = pointMapper.selectBatchIds(pointIds);
            Map<Long, EpatrolPointDO> pointMap = points.stream().collect(Collectors.toMap(EpatrolPointDO::getId, p -> p));

            List<EpatrolRouteRespVO.RoutePointRespVO> pointRespVOs = new ArrayList<>();
            for (EpatrolRoutePointDO rp : routePoints) {
                EpatrolRouteRespVO.RoutePointRespVO pointResp = new EpatrolRouteRespVO.RoutePointRespVO();
                pointResp.setId(rp.getId());
                pointResp.setPointId(rp.getPointId());
                pointResp.setSort(rp.getSort());
                pointResp.setIntervalMinutes(rp.getIntervalMinutes());

                EpatrolPointDO point = pointMap.get(rp.getPointId());
                if (point != null) {
                    pointResp.setPointNo(point.getPointNo());
                    pointResp.setPointName(point.getPointName());
                    pointResp.setPointLocation(point.getPointLocation());
                }
                pointRespVOs.add(pointResp);
            }
            respVO.setPoints(pointRespVOs);
        }

        return respVO;
    }

    @Override
    public PageResult<EpatrolRouteDO> getRoutePage(EpatrolRoutePageReqVO pageReqVO) {
        return routeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<EpatrolRouteDO> getEnabledRouteList() {
        return routeMapper.selectListByStatus(1);
    }

    @Override
    public void updateRouteStatus(Long id, Integer status) {
        // 校验存在
        validateRouteExists(id);
        // 更新状态
        EpatrolRouteDO updateObj = new EpatrolRouteDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        routeMapper.updateById(updateObj);
    }

    @Override
    public List<EpatrolRoutePointDO> getRoutePoints(Long routeId) {
        return routePointMapper.selectByRouteId(routeId);
    }

}
