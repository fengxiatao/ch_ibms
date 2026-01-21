package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.record.*;

/**
 * 门禁记录 Service 接口
 *
 * @author 智能化系统
 */
public interface AccessRecordService {

    /**
     * 获得门禁记录分页
     *
     * @param pageReqVO 分页查询
     * @return 门禁记录分页
     */
    PageResult<AccessRecordRespVO> getAccessRecordPage(AccessRecordPageReqVO pageReqVO);

    /**
     * 获得门禁记录
     *
     * @param id 记录ID
     * @return 门禁记录
     */
    AccessRecordRespVO getAccessRecord(Long id);

    /**
     * 获取通行方式统计（按开门类型分组）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    java.util.List<java.util.Map<String, Object>> getAccessMethodStatistics(
        java.time.LocalDateTime startTime, 
        java.time.LocalDateTime endTime
    );

    /**
     * 获取24小时人员流量统计（按小时分组）
     *
     * @param date 指定日期（默认今天）
     * @return 统计数据
     */
    java.util.List<java.util.Map<String, Object>> getHourlyTrafficStatistics(
        java.time.LocalDate date
    );

}

