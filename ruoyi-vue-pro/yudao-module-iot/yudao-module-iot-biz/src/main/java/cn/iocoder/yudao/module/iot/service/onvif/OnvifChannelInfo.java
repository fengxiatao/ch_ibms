package cn.iocoder.yudao.module.iot.service.onvif;

import lombok.Data;

/**
 * ONVIF 通道信息
 * 
 * <p>从 OnvifClient 内部类提取为独立类，避免类加载问题</p>
 * 
 * @author 长辉信息科技有限公司
 */
@Data
public class OnvifChannelInfo {
    private int channelNo;
    private String profileToken;
    private String channelName;
    private String sourceToken;
    private String resolution;
    private boolean ptzSupport;
    private String ptzToken;
    private boolean audioSupport;
}
