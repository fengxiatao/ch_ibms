package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPointPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPointSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPointDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 电子巡更 - 巡更点 Service 接口
 *
 * @author 长辉信息
 */
public interface EpatrolPointService {

    /**
     * 创建巡更点
     */
    Long createPoint(@Valid EpatrolPointSaveReqVO createReqVO);

    /**
     * 更新巡更点
     */
    void updatePoint(@Valid EpatrolPointSaveReqVO updateReqVO);

    /**
     * 删除巡更点
     */
    void deletePoint(Long id);

    /**
     * 获得巡更点
     */
    EpatrolPointDO getPoint(Long id);

    /**
     * 获得巡更点列表
     */
    List<EpatrolPointDO> getPointList(Collection<Long> ids);

    /**
     * 获得巡更点分页
     */
    PageResult<EpatrolPointDO> getPointPage(EpatrolPointPageReqVO pageReqVO);

    /**
     * 获得所有启用的巡更点
     */
    List<EpatrolPointDO> getEnabledPointList();

    /**
     * 更新巡更点状态
     */
    void updatePointStatus(Long id, Integer status);

    /**
     * 根据点位编号获取点位
     */
    EpatrolPointDO getPointByNo(String pointNo);

}
