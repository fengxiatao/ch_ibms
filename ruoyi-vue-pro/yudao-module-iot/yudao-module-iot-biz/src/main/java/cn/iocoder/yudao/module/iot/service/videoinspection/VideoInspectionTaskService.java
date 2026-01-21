package cn.iocoder.yudao.module.iot.service.videoinspection;

import cn.iocoder.yudao.module.iot.controller.admin.videoinspection.vo.InspectionTaskRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.videoinspection.vo.InspectionTaskSaveReqVO;

import java.util.List;

/**
 * 视频巡检任务 Service 接口
 *
 * @author system
 */
public interface VideoInspectionTaskService {

    /**
     * 创建视频巡检任务
     *
     * @param createReqVO 创建信息
     * @return 任务ID
     */
    Long createInspectionTask(InspectionTaskSaveReqVO createReqVO);

    /**
     * 更新视频巡检任务
     *
     * @param updateReqVO 更新信息
     */
    void updateInspectionTask(InspectionTaskSaveReqVO updateReqVO);

    /**
     * 删除视频巡检任务
     *
     * @param id 任务ID
     */
    void deleteInspectionTask(Long id);

    /**
     * 获得视频巡检任务
     *
     * @param id 任务ID
     * @return 任务信息
     */
    InspectionTaskRespVO getInspectionTask(Long id);

    /**
     * 获得视频巡检任务列表
     *
     * @return 任务列表
     */
    List<InspectionTaskRespVO> getInspectionTaskList();
}
