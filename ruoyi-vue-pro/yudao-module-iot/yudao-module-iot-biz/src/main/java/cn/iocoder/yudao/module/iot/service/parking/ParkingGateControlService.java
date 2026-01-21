package cn.iocoder.yudao.module.iot.service.parking;

/**
 * 停车场道闸控制 Service 接口
 */
public interface ParkingGateControlService {

    /**
     * 开启道闸放行
     *
     * @param lotId 停车场ID
     * @param plateNumber 车牌号
     */
    void openGate(Long lotId, String plateNumber);

    /**
     * 关闭道闸
     *
     * @param gateId 道闸ID
     */
    void closeGate(Long gateId);

    /**
     * 发送白名单车辆放行指令
     *
     * @param gateId 道闸ID
     * @param plateNumber 车牌号
     */
    void sendWhitelistPass(Long gateId, String plateNumber);
}
