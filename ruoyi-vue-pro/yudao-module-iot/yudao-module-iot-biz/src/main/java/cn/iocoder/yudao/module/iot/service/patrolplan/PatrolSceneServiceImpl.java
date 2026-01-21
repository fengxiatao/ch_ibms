package cn.iocoder.yudao.module.iot.service.patrolplan;

import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.scene.PatrolSceneChannelSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.scene.PatrolSceneSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolSceneChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolSceneDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolTaskDO;
import cn.iocoder.yudao.module.iot.dal.mysql.patrolplan.IotVideoPatrolSceneChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.patrolplan.IotVideoPatrolSceneMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.patrolplan.IotVideoPatrolTaskMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 轮巡场景 Service 实现类
 *
 * @author 长辉信息
 */
@Service
@Validated
public class PatrolSceneServiceImpl implements PatrolSceneService {

    @Resource
    private IotVideoPatrolSceneMapper patrolSceneMapper;

    @Resource
    private IotVideoPatrolSceneChannelMapper sceneChannelMapper;

    @Resource
    private IotVideoPatrolTaskMapper patrolTaskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPatrolScene(PatrolSceneSaveReqVO createReqVO) {
        // 校验任务是否存在（自动过滤租户）
        validateTaskExists(createReqVO.getTaskId());
        
        // 插入场景
        IotVideoPatrolSceneDO scene = IotVideoPatrolSceneDO.builder()
                .taskId(createReqVO.getTaskId())
                .sceneName(createReqVO.getSceneName())
                .sceneOrder(createReqVO.getSceneOrder() != null ? createReqVO.getSceneOrder() : 0)
                .duration(createReqVO.getDuration() != null ? createReqVO.getDuration() : 10)
                .gridLayout(createReqVO.getGridLayout())
                .gridCount(createReqVO.getGridCount())
                .description(createReqVO.getDescription())
                .status(createReqVO.getStatus() != null ? createReqVO.getStatus() : 1)
                .build();
        
        patrolSceneMapper.insert(scene);
        
        // 插入通道配置
        if (createReqVO.getChannels() != null && !createReqVO.getChannels().isEmpty()) {
            saveSceneChannels(scene.getId(), createReqVO.getChannels());
        }
        
        // 返回
        return scene.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePatrolScene(PatrolSceneSaveReqVO updateReqVO) {
        // 校验存在
        validatePatrolSceneExists(updateReqVO.getId());
        
        // 校验任务是否存在（自动过滤租户）
        validateTaskExists(updateReqVO.getTaskId());
        
        // 更新场景
        IotVideoPatrolSceneDO updateObj = IotVideoPatrolSceneDO.builder()
                .id(updateReqVO.getId())
                .taskId(updateReqVO.getTaskId())
                .sceneName(updateReqVO.getSceneName())
                .sceneOrder(updateReqVO.getSceneOrder())
                .duration(updateReqVO.getDuration())
                .gridLayout(updateReqVO.getGridLayout())
                .gridCount(updateReqVO.getGridCount())
                .description(updateReqVO.getDescription())
                .status(updateReqVO.getStatus())
                .build();
        
        patrolSceneMapper.updateById(updateObj);
        
        // 更新通道配置：先删除旧的，再插入新的
        List<IotVideoPatrolSceneChannelDO> oldChannels = sceneChannelMapper.selectList("scene_id", updateReqVO.getId());
        if (!oldChannels.isEmpty()) {
            sceneChannelMapper.deleteBatchIds(oldChannels.stream().map(IotVideoPatrolSceneChannelDO::getId).collect(java.util.stream.Collectors.toList()));
        }
        if (updateReqVO.getChannels() != null && !updateReqVO.getChannels().isEmpty()) {
            saveSceneChannels(updateReqVO.getId(), updateReqVO.getChannels());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePatrolScene(Long id) {
        // 校验存在
        validatePatrolSceneExists(id);
        
        // 删除场景（自动过滤租户）
        patrolSceneMapper.deleteById(id);
        
        // 删除关联的通道配置（自动过滤租户）
        List<IotVideoPatrolSceneChannelDO> channels = sceneChannelMapper.selectList("scene_id", id);
        if (!channels.isEmpty()) {
            sceneChannelMapper.deleteBatchIds(channels.stream().map(IotVideoPatrolSceneChannelDO::getId).collect(java.util.stream.Collectors.toList()));
        }
    }

    @Override
    public IotVideoPatrolSceneDO getPatrolScene(Long id) {
        // 自动过滤租户
        return patrolSceneMapper.selectById(id);
    }

    @Override
    public List<IotVideoPatrolSceneDO> getPatrolSceneListByTaskId(Long taskId) {
        // 自动过滤租户
        return patrolSceneMapper.selectListByTaskId(taskId);
    }

    @Override
    public List<IotVideoPatrolSceneChannelDO> getSceneChannels(Long sceneId) {
        // 自动过滤租户
        return sceneChannelMapper.selectListBySceneId(sceneId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSceneOrder(List<Long> sceneIds) {
        // 批量更新场景顺序
        for (int i = 0; i < sceneIds.size(); i++) {
            Long sceneId = sceneIds.get(i);
            
            // 校验场景存在（自动过滤租户）
            IotVideoPatrolSceneDO scene = validatePatrolSceneExists(sceneId);
            
            // 更新顺序
            IotVideoPatrolSceneDO updateObj = IotVideoPatrolSceneDO.builder()
                    .id(sceneId)
                    .sceneOrder(i)
                    .build();
            
            patrolSceneMapper.updateById(updateObj);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 校验轮巡场景是否存在
     */
    private IotVideoPatrolSceneDO validatePatrolSceneExists(Long id) {
        IotVideoPatrolSceneDO scene = patrolSceneMapper.selectById(id);
        if (scene == null) {
            throw exception(VIDEO_PATROL_SCENE_NOT_EXISTS);
        }
        return scene;
    }

    /**
     * 校验任务是否存在
     */
    private void validateTaskExists(Long taskId) {
        IotVideoPatrolTaskDO task = patrolTaskMapper.selectById(taskId);
        if (task == null) {
            throw exception(VIDEO_PATROL_TASK_NOT_EXISTS);
        }
    }

    /**
     * 保存场景通道配置
     * 
     * @param sceneId 场景ID
     * @param channels 通道配置列表
     */
    private void saveSceneChannels(Long sceneId, List<PatrolSceneChannelSaveReqVO> channels) {
        for (PatrolSceneChannelSaveReqVO channelVO : channels) {
            IotVideoPatrolSceneChannelDO channel = IotVideoPatrolSceneChannelDO.builder()
                    .sceneId(sceneId)
                    .gridPosition(channelVO.getGridPosition())
                    .duration(channelVO.getDuration())
                    .channelId(channelVO.getChannelId())
                    .deviceId(channelVO.getDeviceId() != null ? Long.parseLong(channelVO.getDeviceId()) : null)
                    .channelNo(channelVO.getChannelNo())
                    .channelName(channelVO.getChannelName())
                    .targetIp(channelVO.getTargetIp())
                    .targetChannelNo(channelVO.getTargetChannelNo())
                    .streamUrlMain(channelVO.getStreamUrlMain())
                    .streamUrlSub(channelVO.getStreamUrlSub())
                    .config(channelVO.getConfig())
                    .build();
            
            sceneChannelMapper.insert(channel);
        }
    }

    @Override
    public PatrolSceneSaveReqVO getPatrolSceneWithChannelsByTaskId(Long taskId) {
        // 查询任务下的场景（通常一个任务只有一个场景）
        List<IotVideoPatrolSceneDO> scenes = patrolSceneMapper.selectListByTaskId(taskId);
        if (scenes == null || scenes.isEmpty()) {
            return null;
        }
        
        // 取第一个场景
        IotVideoPatrolSceneDO scene = scenes.get(0);
        
        // 查询场景的通道列表
        List<IotVideoPatrolSceneChannelDO> channelDOs = sceneChannelMapper.selectListBySceneId(scene.getId());
        
        // 转换为 VO
        PatrolSceneSaveReqVO vo = new PatrolSceneSaveReqVO();
        vo.setId(scene.getId());
        vo.setTaskId(scene.getTaskId());
        vo.setSceneName(scene.getSceneName());
        vo.setSceneOrder(scene.getSceneOrder());
        vo.setDuration(scene.getDuration());
        vo.setGridLayout(scene.getGridLayout());
        vo.setGridCount(scene.getGridCount());
        vo.setDescription(scene.getDescription());
        vo.setStatus(scene.getStatus());
        
        // 转换通道列表
        if (channelDOs != null && !channelDOs.isEmpty()) {
            List<PatrolSceneChannelSaveReqVO> channelVOs = new java.util.ArrayList<>();
            for (IotVideoPatrolSceneChannelDO channelDO : channelDOs) {
                PatrolSceneChannelSaveReqVO channelVO = new PatrolSceneChannelSaveReqVO();
                channelVO.setGridPosition(channelDO.getGridPosition());
                channelVO.setDuration(channelDO.getDuration());
                channelVO.setChannelId(channelDO.getChannelId());
                channelVO.setDeviceId(channelDO.getDeviceId() != null ? String.valueOf(channelDO.getDeviceId()) : null);
                channelVO.setChannelNo(channelDO.getChannelNo());
                channelVO.setChannelName(channelDO.getChannelName());
                channelVO.setTargetIp(channelDO.getTargetIp());
                channelVO.setTargetChannelNo(channelDO.getTargetChannelNo());
                channelVO.setStreamUrlMain(channelDO.getStreamUrlMain());
                channelVO.setStreamUrlSub(channelDO.getStreamUrlSub());
                channelVO.setConfig(channelDO.getConfig());
                channelVOs.add(channelVO);
            }
            vo.setChannels(channelVOs);
        }
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long savePatrolSceneWithChannels(PatrolSceneSaveReqVO saveReqVO) {
        if (saveReqVO.getId() != null) {
            // 更新场景
            updatePatrolScene(saveReqVO);
            return saveReqVO.getId();
        } else {
            // 创建场景
            return createPatrolScene(saveReqVO);
        }
    }

}
