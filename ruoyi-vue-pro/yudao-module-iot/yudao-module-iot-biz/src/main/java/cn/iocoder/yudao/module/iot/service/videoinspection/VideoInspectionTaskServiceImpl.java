package cn.iocoder.yudao.module.iot.service.videoinspection;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.videoinspection.vo.InspectionTaskRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.videoinspection.vo.InspectionTaskSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.videoinspection.IotVideoInspectionTaskDO;
import cn.iocoder.yudao.module.iot.dal.mysql.videoinspection.VideoInspectionTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.VIDEO_INSPECTION_TASK_NOT_EXISTS;

/**
 * 视频巡检任务 Service 实现类
 *
 * @author system
 */
@Service
@Validated
@Slf4j
public class VideoInspectionTaskServiceImpl implements VideoInspectionTaskService {

    @Resource
    private VideoInspectionTaskMapper inspectionTaskMapper;

    @Override
    public Long createInspectionTask(InspectionTaskSaveReqVO createReqVO) {
        // 转换VO到DO
        IotVideoInspectionTaskDO task = BeanUtils.toBean(createReqVO, IotVideoInspectionTaskDO.class);
        
        // 转换场景数据
        if (createReqVO.getScenes() != null) {
            task.setScenes(createReqVO.getScenes().stream()
                    .map(scene -> {
                        IotVideoInspectionTaskDO.InspectionSceneDO sceneDO = new IotVideoInspectionTaskDO.InspectionSceneDO();
                        sceneDO.setCellIndex(scene.getCellIndex());
                        if (scene.getChannels() != null) {
                            sceneDO.setChannels(scene.getChannels().stream()
                                    .map(channel -> {
                                        IotVideoInspectionTaskDO.InspectionChannelDO channelDO = new IotVideoInspectionTaskDO.InspectionChannelDO();
                                        BeanUtils.copyProperties(channel, channelDO);
                                        return channelDO;
                                    })
                                    .collect(Collectors.toList()));
                        }
                        return sceneDO;
                    })
                    .collect(Collectors.toList()));
        }
        
        // 插入数据库（自动设置tenant_id）
        inspectionTaskMapper.insert(task);
        log.info("[视频巡检任务] 创建成功, id={}, taskName={}", task.getId(), task.getTaskName());
        return task.getId();
    }

    @Override
    public void updateInspectionTask(InspectionTaskSaveReqVO updateReqVO) {
        // 校验存在
        validateInspectionTaskExists(updateReqVO.getId());
        
        // 转换VO到DO
        IotVideoInspectionTaskDO updateObj = BeanUtils.toBean(updateReqVO, IotVideoInspectionTaskDO.class);
        
        // 转换场景数据
        if (updateReqVO.getScenes() != null) {
            updateObj.setScenes(updateReqVO.getScenes().stream()
                    .map(scene -> {
                        IotVideoInspectionTaskDO.InspectionSceneDO sceneDO = new IotVideoInspectionTaskDO.InspectionSceneDO();
                        sceneDO.setCellIndex(scene.getCellIndex());
                        if (scene.getChannels() != null) {
                            sceneDO.setChannels(scene.getChannels().stream()
                                    .map(channel -> {
                                        IotVideoInspectionTaskDO.InspectionChannelDO channelDO = new IotVideoInspectionTaskDO.InspectionChannelDO();
                                        BeanUtils.copyProperties(channel, channelDO);
                                        return channelDO;
                                    })
                                    .collect(Collectors.toList()));
                        }
                        return sceneDO;
                    })
                    .collect(Collectors.toList()));
        }
        
        // 更新数据库（自动校验tenant_id）
        inspectionTaskMapper.updateById(updateObj);
        log.info("[视频巡检任务] 更新成功, id={}, taskName={}", updateObj.getId(), updateObj.getTaskName());
    }

    @Override
    public void deleteInspectionTask(Long id) {
        // 校验存在
        validateInspectionTaskExists(id);
        
        // 逻辑删除（自动校验tenant_id）
        inspectionTaskMapper.deleteById(id);
        log.info("[视频巡检任务] 删除成功, id={}", id);
    }

    @Override
    public InspectionTaskRespVO getInspectionTask(Long id) {
        // 查询DO（自动过滤tenant_id）
        IotVideoInspectionTaskDO task = inspectionTaskMapper.selectById(id);
        if (task == null) {
            return null;
        }
        
        // 转换DO到VO
        InspectionTaskRespVO respVO = BeanUtils.toBean(task, InspectionTaskRespVO.class);
        
        // 转换场景数据
        if (task.getScenes() != null) {
            respVO.setScenes(task.getScenes().stream()
                    .map(scene -> {
                        InspectionTaskRespVO.InspectionSceneVO sceneVO = new InspectionTaskRespVO.InspectionSceneVO();
                        sceneVO.setCellIndex(scene.getCellIndex());
                        if (scene.getChannels() != null) {
                            sceneVO.setChannels(scene.getChannels().stream()
                                    .map(channel -> {
                                        InspectionTaskRespVO.InspectionChannelVO channelVO = new InspectionTaskRespVO.InspectionChannelVO();
                                        BeanUtils.copyProperties(channel, channelVO);
                                        return channelVO;
                                    })
                                    .collect(Collectors.toList()));
                        }
                        return sceneVO;
                    })
                    .collect(Collectors.toList()));
        }
        
        return respVO;
    }

    @Override
    public List<InspectionTaskRespVO> getInspectionTaskList() {
        // 查询当前租户的所有任务（自动过滤tenant_id）
        List<IotVideoInspectionTaskDO> list = inspectionTaskMapper.selectList();
        
        // 转换DO列表到VO列表
        return list.stream()
                .map(task -> {
                    InspectionTaskRespVO respVO = BeanUtils.toBean(task, InspectionTaskRespVO.class);
                    
                    // 转换场景数据
                    if (task.getScenes() != null) {
                        respVO.setScenes(task.getScenes().stream()
                                .map(scene -> {
                                    InspectionTaskRespVO.InspectionSceneVO sceneVO = new InspectionTaskRespVO.InspectionSceneVO();
                                    sceneVO.setCellIndex(scene.getCellIndex());
                                    if (scene.getChannels() != null) {
                                        sceneVO.setChannels(scene.getChannels().stream()
                                                .map(channel -> {
                                                    InspectionTaskRespVO.InspectionChannelVO channelVO = new InspectionTaskRespVO.InspectionChannelVO();
                                                    BeanUtils.copyProperties(channel, channelVO);
                                                    return channelVO;
                                                })
                                                .collect(Collectors.toList()));
                                    }
                                    return sceneVO;
                                })
                                .collect(Collectors.toList()));
                    }
                    
                    return respVO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 校验视频巡检任务是否存在
     */
    private void validateInspectionTaskExists(Long id) {
        if (inspectionTaskMapper.selectById(id) == null) {
            throw exception(VIDEO_INSPECTION_TASK_NOT_EXISTS);
        }
    }
}
