package cn.iocoder.yudao.module.iot.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.operationlog.IotAlarmOperationLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.operationlog.IotAlarmOperationLogRespVO;

/**
 * 报警主机操作记录 Service 接口
 *
 * @author 长辉信息科技有限公司
 */
public interface IotAlarmOperationLogService {

    /**
     * 获得报警主机操作记录分页
     *
     * @param pageReqVO 分页查询
     * @return 报警主机操作记录分页
     */
    PageResult<IotAlarmOperationLogRespVO> getOperationLogPage(IotAlarmOperationLogPageReqVO pageReqVO);

    /**
     * 记录操作日志
     *
     * @param hostId 主机ID
     * @param partitionId 分区ID
     * @param zoneId 防区ID
     * @param operationType 操作类型
     * @param operatorName 操作人姓名
     * @param result 操作结果
     * @param errorMessage 错误信息
     * @param requestId 请求ID
     */
    void logOperation(Long hostId, Long partitionId, Long zoneId, String operationType, 
                     String operatorName, String result, String errorMessage, String requestId);

}
