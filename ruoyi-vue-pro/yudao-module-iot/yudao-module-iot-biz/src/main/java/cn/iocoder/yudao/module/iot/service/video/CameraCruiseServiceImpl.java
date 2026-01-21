package cn.iocoder.yudao.module.iot.service.video;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraCruiseSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraCruiseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraCruisePointDO;
import cn.iocoder.yudao.module.iot.dal.mysql.video.CameraCruiseMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.video.CameraCruisePointMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 摄像头巡航路线 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class CameraCruiseServiceImpl implements CameraCruiseService {

    @Resource
    private CameraCruiseMapper cruiseMapper;

    @Resource
    private CameraCruisePointMapper cruisePointMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCruise(CameraCruiseSaveReqVO createReqVO) {
        // 校验巡航路线名称是否已存在
        CameraCruiseDO existCruise = cruiseMapper.selectByChannelIdAndName(
                createReqVO.getChannelId(), createReqVO.getCruiseName());
        if (existCruise != null) {
            throw exception(CAMERA_CRUISE_NAME_EXISTS);
        }

        // 插入巡航路线
        CameraCruiseDO cruise = BeanUtils.toBean(createReqVO, CameraCruiseDO.class);
        cruise.setStatus(0); // 默认停止状态
        cruiseMapper.insert(cruise);

        // 插入巡航点
        saveCruisePoints(cruise.getId(), createReqVO.getPoints());

        log.info("[createCruise] 创建巡航路线成功: channelId={}, cruiseName={}, pointCount={}",
                cruise.getChannelId(), cruise.getCruiseName(), createReqVO.getPoints().size());

        return cruise.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCruise(CameraCruiseSaveReqVO updateReqVO) {
        // 校验存在
        validateCruiseExists(updateReqVO.getId());

        // 校验名称是否重复
        CameraCruiseDO existCruise = cruiseMapper.selectByChannelIdAndName(
                updateReqVO.getChannelId(), updateReqVO.getCruiseName());
        if (existCruise != null && !existCruise.getId().equals(updateReqVO.getId())) {
            throw exception(CAMERA_CRUISE_NAME_EXISTS);
        }

        // 更新巡航路线
        CameraCruiseDO updateObj = BeanUtils.toBean(updateReqVO, CameraCruiseDO.class);
        cruiseMapper.updateById(updateObj);

        // 删除旧的巡航点
        cruisePointMapper.deleteByCruiseId(updateReqVO.getId());

        // 插入新的巡航点
        saveCruisePoints(updateReqVO.getId(), updateReqVO.getPoints());

        log.info("[updateCruise] 更新巡航路线成功: id={}, cruiseName={}, pointCount={}",
                updateObj.getId(), updateObj.getCruiseName(), updateReqVO.getPoints().size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCruise(Long id) {
        // 校验存在
        validateCruiseExists(id);

        // 删除巡航路线
        cruiseMapper.deleteById(id);

        // 删除巡航点
        cruisePointMapper.deleteByCruiseId(id);

        log.info("[deleteCruise] 删除巡航路线成功: id={}", id);
    }

    @Override
    public CameraCruiseDO getCruise(Long id) {
        return cruiseMapper.selectById(id);
    }

    @Override
    public List<CameraCruiseDO> getCruiseListByChannelId(Long channelId) {
        return cruiseMapper.selectListByChannelId(channelId);
    }

    @Override
    public void startCruise(Long id) {
        // 校验存在
        CameraCruiseDO cruise = validateCruiseExists(id);

        // 更新状态为运行中
        CameraCruiseDO updateObj = new CameraCruiseDO();
        updateObj.setId(id);
        updateObj.setStatus(1);
        cruiseMapper.updateById(updateObj);

        log.info("[startCruise] 启动巡航: id={}, cruiseName={}", id, cruise.getCruiseName());
    }

    @Override
    public void stopCruise(Long id) {
        // 校验存在
        CameraCruiseDO cruise = validateCruiseExists(id);

        // 更新状态为停止
        CameraCruiseDO updateObj = new CameraCruiseDO();
        updateObj.setId(id);
        updateObj.setStatus(0);
        cruiseMapper.updateById(updateObj);

        log.info("[stopCruise] 停止巡航: id={}, cruiseName={}", id, cruise.getCruiseName());
    }

    /**
     * 校验巡航路线是否存在
     */
    private CameraCruiseDO validateCruiseExists(Long id) {
        CameraCruiseDO cruise = cruiseMapper.selectById(id);
        if (cruise == null) {
            throw exception(CAMERA_CRUISE_NOT_EXISTS);
        }
        return cruise;
    }

    /**
     * 保存巡航点列表
     */
    private void saveCruisePoints(Long cruiseId, List<?> points) {
        for (Object point : points) {
            CameraCruisePointDO cruisePoint = BeanUtils.toBean(point, CameraCruisePointDO.class);
            cruisePoint.setCruiseId(cruiseId);
            cruisePointMapper.insert(cruisePoint);
        }
    }

}
