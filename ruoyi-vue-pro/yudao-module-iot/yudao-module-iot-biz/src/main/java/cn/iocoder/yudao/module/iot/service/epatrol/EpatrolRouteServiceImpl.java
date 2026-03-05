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
        // 使用前端传递的总时长，如果没有则计算
        int totalDuration = createReqVO.getTotalDuration() != null ? createReqVO.getTotalDuration() : 0;
        if (totalDuration == 0 && CollUtil.isNotEmpty(createReqVO.getPoints())) {
            for (EpatrolRouteSaveReqVO.RoutePointItem item : createReqVO.getPoints()) {
                totalDuration += item.getIntervalMinutes() != null ? item.getIntervalMinutes() : 0;
            }
        }

        // 插入路线
        EpatrolRouteDO route = new EpatrolRouteDO();
        route.setRouteName(createReqVO.getRouteName());
        route.setPointCount(CollUtil.isNotEmpty(createReqVO.getPoints()) ? createReqVO.getPoints().size() : 0);
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

        // 使用前端传递的总时长，如果没有则计算
        int totalDuration = updateReqVO.getTotalDuration() != null ? updateReqVO.getTotalDuration() : 0;
        if (totalDuration == 0 && CollUtil.isNotEmpty(updateReqVO.getPoints())) {
            for (EpatrolRouteSaveReqVO.RoutePointItem item : updateReqVO.getPoints()) {
                totalDuration += item.getIntervalMinutes() != null ? item.getIntervalMinutes() : 0;
            }
        }

        // 更新路线
        EpatrolRouteDO updateObj = new EpatrolRouteDO();
        updateObj.setId(updateReqVO.getId());
        updateObj.setRouteName(updateReqVO.getRouteName());
        updateObj.setPointCount(CollUtil.isNotEmpty(updateReqVO.getPoints()) ? updateReqVO.getPoints().size() : 0);
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
        // 如果有点位名称搜索条件，先查询包含该点位的路线ID
        if (pageReqVO.getPointName() != null && !pageReqVO.getPointName().isEmpty()) {
            // 查询符合条件的点位
            List<EpatrolPointDO> points = pointMapper.selectList(
                    new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<EpatrolPointDO>()
                            .like(EpatrolPointDO::getPointName, pageReqVO.getPointName()));
            if (CollUtil.isEmpty(points)) {
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            // 查询包含这些点位的路线ID
            List<Long> pointIds = points.stream().map(EpatrolPointDO::getId).collect(Collectors.toList());
            List<EpatrolRoutePointDO> routePoints = routePointMapper.selectList(
                    new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<EpatrolRoutePointDO>()
                            .in(EpatrolRoutePointDO::getPointId, pointIds));
            if (CollUtil.isEmpty(routePoints)) {
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            List<Long> routeIds = routePoints.stream().map(EpatrolRoutePointDO::getRouteId).distinct().collect(Collectors.toList());
            // 在这些路线中分页查询
            return routeMapper.selectPage(pageReqVO, new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<EpatrolRouteDO>()
                    .in(EpatrolRouteDO::getId, routeIds)
                    .likeIfPresent(EpatrolRouteDO::getRouteName, pageReqVO.getRouteName())
                    .eqIfPresent(EpatrolRouteDO::getStatus, pageReqVO.getStatus())
                    .orderByDesc(EpatrolRouteDO::getId));
        }
        return routeMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<EpatrolRouteRespVO> getRoutePageWithPoints(EpatrolRoutePageReqVO pageReqVO) {
        // 先获取分页数据
        PageResult<EpatrolRouteDO> pageResult = getRoutePage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(new ArrayList<>(), pageResult.getTotal());
        }

        // 获取所有路线ID
        List<Long> routeIds = pageResult.getList().stream()
                .map(EpatrolRouteDO::getId)
                .collect(Collectors.toList());

        // 批量查询所有路线的点位关联
        List<EpatrolRoutePointDO> allRoutePoints = routePointMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<EpatrolRoutePointDO>()
                        .in(EpatrolRoutePointDO::getRouteId, routeIds)
                        .orderByAsc(EpatrolRoutePointDO::getSort));

        // 获取所有点位ID
        List<Long> pointIds = allRoutePoints.stream()
                .map(EpatrolRoutePointDO::getPointId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询点位详情
        Map<Long, EpatrolPointDO> pointMap = new java.util.HashMap<>();
        if (CollUtil.isNotEmpty(pointIds)) {
            List<EpatrolPointDO> points = pointMapper.selectBatchIds(pointIds);
            pointMap = points.stream().collect(Collectors.toMap(EpatrolPointDO::getId, p -> p));
        }

        // 按路线ID分组点位
        Map<Long, List<EpatrolRoutePointDO>> routePointsMap = allRoutePoints.stream()
                .collect(Collectors.groupingBy(EpatrolRoutePointDO::getRouteId));

        // 转换为RespVO
        List<EpatrolRouteRespVO> respList = new ArrayList<>();
        for (EpatrolRouteDO route : pageResult.getList()) {
            EpatrolRouteRespVO respVO = BeanUtils.toBean(route, EpatrolRouteRespVO.class);

            // 填充点位信息
            List<EpatrolRoutePointDO> routePoints = routePointsMap.get(route.getId());
            if (CollUtil.isNotEmpty(routePoints)) {
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
            respList.add(respVO);
        }

        return new PageResult<>(respList, pageResult.getTotal());
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
