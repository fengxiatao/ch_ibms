package cn.iocoder.yudao.module.iot.service.opc;

import cn.iocoder.yudao.module.iot.core.mq.message.opc.OpcAlarmEvent;

/**
 * OPC报警记录 Service 接口
 *
 * @author 长辉信息科技有限公司
 */
public interface OpcAlarmRecordService {

    /**
     * 初始化OPC报警记录超级表
     * <p>
     * 在系统启动时自动调用，创建TDengine超级表结构
     */
    void initOpcAlarmStable();

    /**
     * 保存OPC报警记录到TDengine
     *
     * @param event OPC报警事件
     */
    void saveAlarmRecord(OpcAlarmEvent event);
}
