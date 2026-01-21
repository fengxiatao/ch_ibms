package cn.iocoder.yudao.module.iot.service.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.task.vo.log.TaskLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.task.vo.log.TaskLogRespVO;

/**
 * 任务执行日志 Service 接口
 *
 * @author 芋道源码
 */
public interface TaskLogService {

    /**
     * 获取任务执行日志分页
     *
     * @param pageReqVO 分页查询
     * @return 任务执行日志分页
     */
    PageResult<TaskLogRespVO> getLogPage(TaskLogPageReqVO pageReqVO);

    /**
     * 获取任务执行日志详情
     *
     * @param id 日志ID
     * @return 任务执行日志详情
     */
    TaskLogRespVO getLogDetail(Long id);

}
























































