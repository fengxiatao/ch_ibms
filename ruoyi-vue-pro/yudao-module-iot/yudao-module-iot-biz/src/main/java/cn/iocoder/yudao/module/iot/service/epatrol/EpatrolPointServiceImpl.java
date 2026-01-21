package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPointPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPointSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPointDO;
import cn.iocoder.yudao.module.iot.dal.mysql.epatrol.EpatrolPointMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 电子巡更 - 巡更点 Service 实现类
 *
 * @author 长辉信息
 */
@Service
@Validated
public class EpatrolPointServiceImpl implements EpatrolPointService {

    @Resource
    private EpatrolPointMapper pointMapper;

    @Override
    public Long createPoint(EpatrolPointSaveReqVO createReqVO) {
        // 校验点位编号是否重复
        validatePointNoUnique(null, createReqVO.getPointNo());
        // 插入
        EpatrolPointDO point = BeanUtils.toBean(createReqVO, EpatrolPointDO.class);
        point.setStatus(1); // 默认启用
        pointMapper.insert(point);
        return point.getId();
    }

    @Override
    public void updatePoint(EpatrolPointSaveReqVO updateReqVO) {
        // 校验存在
        validatePointExists(updateReqVO.getId());
        // 校验点位编号是否重复
        validatePointNoUnique(updateReqVO.getId(), updateReqVO.getPointNo());
        // 更新
        EpatrolPointDO updateObj = BeanUtils.toBean(updateReqVO, EpatrolPointDO.class);
        pointMapper.updateById(updateObj);
    }

    @Override
    public void deletePoint(Long id) {
        // 校验存在
        validatePointExists(id);
        // 删除
        pointMapper.deleteById(id);
    }

    private void validatePointExists(Long id) {
        if (pointMapper.selectById(id) == null) {
            throw exception(EPATROL_POINT_NOT_EXISTS);
        }
    }

    private void validatePointNoUnique(Long id, String pointNo) {
        EpatrolPointDO existing = pointMapper.selectByPointNo(pointNo);
        if (existing != null && !existing.getId().equals(id)) {
            throw exception(EPATROL_POINT_NO_EXISTS);
        }
    }

    @Override
    public EpatrolPointDO getPoint(Long id) {
        return pointMapper.selectById(id);
    }

    @Override
    public List<EpatrolPointDO> getPointList(Collection<Long> ids) {
        return pointMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<EpatrolPointDO> getPointPage(EpatrolPointPageReqVO pageReqVO) {
        return pointMapper.selectPage(pageReqVO);
    }

    @Override
    public List<EpatrolPointDO> getEnabledPointList() {
        return pointMapper.selectListByStatus(1);
    }

    @Override
    public void updatePointStatus(Long id, Integer status) {
        // 校验存在
        validatePointExists(id);
        // 更新状态
        EpatrolPointDO updateObj = new EpatrolPointDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        pointMapper.updateById(updateObj);
    }

    @Override
    public EpatrolPointDO getPointByNo(String pointNo) {
        return pointMapper.selectByPointNo(pointNo);
    }

}
