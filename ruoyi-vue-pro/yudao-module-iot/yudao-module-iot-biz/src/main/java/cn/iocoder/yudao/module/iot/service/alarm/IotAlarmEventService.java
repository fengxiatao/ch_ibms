package cn.iocoder.yudao.module.iot.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventHandleReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event.IotAlarmEventStatsVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmEventDO;

import java.util.List;

/**
 * 报警事件 Service 接口
 *
 * @author 长辉信息科技有限公司
 */
public interface IotAlarmEventService {

    /**
     * 分页查询报警事件
     *
     * @param pageReqVO 分页查询条件
     * @return 报警事件分页结果
     */
    PageResult<IotAlarmEventDO> getEventPage(IotAlarmEventPageReqVO pageReqVO);

    /**
     * 分页查询报警事件（补齐主机名称、防区名称等展示字段）
     */
    PageResult<IotAlarmEventRespVO> getEventPageVO(IotAlarmEventPageReqVO pageReqVO);

    /**
     * 获取报警事件详情（补齐展示字段）
     */
    IotAlarmEventRespVO getEvent(Long id);

    /**
     * 导出报警事件列表（按筛选条件）
     */
    List<IotAlarmEventRespVO> getEventListForExport(IotAlarmEventPageReqVO reqVO);

    /**
     * 获取报警事件统计数据
     *
     * @return 统计数据
     */
    IotAlarmEventStatsVO getEventStats();

    /**
     * 处理报警事件
     *
     * @param reqVO 处理请求
     */
    void handleEvent(IotAlarmEventHandleReqVO reqVO);

    /**
     * 忽略报警事件（本质为“标记已处理”，并写入忽略备注）
     *
     * @param id 事件ID
     * @param remark 备注（可选）
     */
    void ignoreEvent(Long id, String remark);

    /**
     * 保存报警事件（由 MQ 消费者调用）
     *
     * @param eventDO 报警事件
     */
    void saveEvent(IotAlarmEventDO eventDO);
}
