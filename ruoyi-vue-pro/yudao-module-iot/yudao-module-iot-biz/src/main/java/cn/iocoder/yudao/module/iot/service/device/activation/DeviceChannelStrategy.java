package cn.iocoder.yudao.module.iot.service.device.activation;

/**
 * 设备通道策略枚举
 * 
 * @author 长辉信息科技有限公司
 */
public enum DeviceChannelStrategy {
    
    /**
     * NVR/DVR设备：需要同步多个通道（通道 = 接入的摄像头）
     */
    NVR_DEVICE,
    
    /**
     * 普通IPC：直接创建1个通道（IPC自身）
     */
    IPC_SINGLE_CHANNEL,
    
    /**
     * 球机：需要查询通道数（可能是1个或2个，如双光谱）
     */
    PTZ_MULTI_CHANNEL,
    
    /**
     * 无通道设备（如单点传感器）
     */
    NO_CHANNEL
}
