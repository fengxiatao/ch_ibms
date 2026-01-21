package cn.iocoder.yudao.module.iot.service.opc;

/**
 * OPC 控制服务接口
 * 
 * @author 长辉信息科技有限公司
 */
public interface OpcControlService {

    /**
     * 布防
     * 
     * @param deviceId 设备ID
     * @return 是否成功
     */
    Boolean arm(Long deviceId);

    /**
     * 撤防
     * 
     * @param deviceId 设备ID
     * @return 是否成功
     */
    Boolean disarm(Long deviceId);

    /**
     * 查询状态
     * 
     * @param deviceId 设备ID
     * @return 是否成功
     */
    Boolean queryStatus(Long deviceId);
}
