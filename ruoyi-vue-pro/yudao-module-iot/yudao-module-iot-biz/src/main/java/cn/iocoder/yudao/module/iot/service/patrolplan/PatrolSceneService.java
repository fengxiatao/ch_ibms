package cn.iocoder.yudao.module.iot.service.patrolplan;

import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.scene.PatrolSceneSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolSceneDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolSceneChannelDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 轮巡场景 Service 接口
 *
 * @author 长辉信息
 */
public interface PatrolSceneService {

    /**
     * 创建轮巡场景（包含通道配置）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPatrolScene(@Valid PatrolSceneSaveReqVO createReqVO);

    /**
     * 更新轮巡场景（包含通道配置）
     *
     * @param updateReqVO 更新信息
     */
    void updatePatrolScene(@Valid PatrolSceneSaveReqVO updateReqVO);

    /**
     * 删除轮巡场景
     *
     * @param id 编号
     */
    void deletePatrolScene(Long id);

    /**
     * 获得轮巡场景
     *
     * @param id 编号
     * @return 轮巡场景
     */
    IotVideoPatrolSceneDO getPatrolScene(Long id);

    /**
     * 获得指定任务下的场景列表
     *
     * @param taskId 任务ID
     * @return 场景列表
     */
    List<IotVideoPatrolSceneDO> getPatrolSceneListByTaskId(Long taskId);

    /**
     * 获得场景的通道列表
     *
     * @param sceneId 场景ID
     * @return 通道列表
     */
    List<IotVideoPatrolSceneChannelDO> getSceneChannels(Long sceneId);

    /**
     * 更新场景顺序
     *
     * @param sceneIds 场景ID列表（按新顺序排列）
     */
    void updateSceneOrder(List<Long> sceneIds);

    /**
     * 根据任务ID获取场景（包含通道）
     *
     * @param taskId 任务ID
     * @return 场景信息（包含通道列表）
     */
    PatrolSceneSaveReqVO getPatrolSceneWithChannelsByTaskId(Long taskId);

    /**
     * 保存场景（包含通道）
     * 如果场景ID存在则更新，否则创建
     *
     * @param saveReqVO 场景信息（包含通道列表）
     * @return 场景ID
     */
    Long savePatrolSceneWithChannels(@Valid PatrolSceneSaveReqVO saveReqVO);

}
