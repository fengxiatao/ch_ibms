package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.alarm.*;

import jakarta.validation.Valid;

/**
 * 门禁告警 Service 接口
 *
 * @author 智能化系统
 */
public interface AccessAlarmService {

    /**
     * 获得门禁告警分页
     *
     * @param pageReqVO 分页查询
     * @return 门禁告警分页
     */
    PageResult<AccessAlarmRespVO> getAccessAlarmPage(AccessAlarmPageReqVO pageReqVO);

    /**
     * 获得门禁告警
     *
     * @param id 告警ID
     * @return 门禁告警
     */
    AccessAlarmRespVO getAccessAlarm(Long id);

    /**
     * 处理门禁告警
     *
     * @param handleReqVO 处理信息
     */
    void handleAccessAlarm(@Valid AccessAlarmHandleReqVO handleReqVO);

    /**
     * 获取告警类型统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    java.util.List<java.util.Map<String, Object>> getAlarmTypeStatistics(
        java.time.LocalDateTime startTime, 
        java.time.LocalDateTime endTime
    );

}

