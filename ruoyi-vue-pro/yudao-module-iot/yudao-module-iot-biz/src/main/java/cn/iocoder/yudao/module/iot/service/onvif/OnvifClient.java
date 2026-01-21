package cn.iocoder.yudao.module.iot.service.onvif;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ONVIF 客户端工具类
 * 
 * <p>基于 Apache CXF 实现的 ONVIF 设备客户端</p>
 * 
 * @author 长辉信息科技有限公司
 */
@Slf4j
public class OnvifClient {

    private final String deviceIp;
    private final String username;
    private final String password;
    private final int timeout;

    /**
     * 构造函数
     * 
     * @param deviceIp 设备IP
     * @param username 用户名
     * @param password 密码
     */
    public OnvifClient(String deviceIp, String username, String password) {
        this(deviceIp, username, password, 5000);
    }

    /**
     * 构造函数（带超时）
     * 
     * @param deviceIp 设备IP
     * @param username 用户名
     * @param password 密码
     * @param timeout 超时时间（毫秒）
     */
    public OnvifClient(String deviceIp, String username, String password, int timeout) {
        this.deviceIp = deviceIp;
        this.username = username;
        this.password = password;
        this.timeout = timeout;
    }

    /**
     * 查询设备的视频流配置文件（Profiles）
     * 
     * <p>每个 Profile 代表一个视频通道配置</p>
     * 
     * @return 通道信息列表
     */
    public List<OnvifChannelInfo> getProfiles() {
        List<OnvifChannelInfo> channels = new ArrayList<>();
        
        try {
            // 1. 创建 Media Service 客户端
            String mediaServiceUrl = String.format("http://%s/onvif/media_service", deviceIp);
            OnvifMediaService mediaService = createMediaServiceClient(mediaServiceUrl);
            
            // 2. 调用 GetProfiles 接口
            log.info("[ONVIF] 查询设备Profiles: ip={}", deviceIp);
            GetProfilesResponse response = mediaService.getProfiles(new GetProfiles());
            
            if (response == null || response.getProfiles() == null) {
                log.warn("[ONVIF] 设备返回空的Profiles: ip={}", deviceIp);
                return channels;
            }
            
            // 3. 解析每个 Profile
            List<Profile> profiles = response.getProfiles();
            log.info("[ONVIF] 设备返回 {} 个Profiles: ip={}", profiles.size(), deviceIp);
            
            for (int i = 0; i < profiles.size(); i++) {
                Profile profile = profiles.get(i);
                OnvifChannelInfo channelInfo = new OnvifChannelInfo();
                channelInfo.setChannelNo(i + 1);
                channelInfo.setProfileToken(profile.getToken());
                channelInfo.setChannelName(profile.getName());
                
                // 解析视频源配置
                if (profile.getVideoSourceConfiguration() != null) {
                    VideoSourceConfiguration videoConfig = profile.getVideoSourceConfiguration();
                    channelInfo.setSourceToken(videoConfig.getSourceToken());
                    
                    // 解析分辨率
                    if (videoConfig.getBounds() != null) {
                        int width = videoConfig.getBounds().getWidth();
                        int height = videoConfig.getBounds().getHeight();
                        channelInfo.setResolution(width + "x" + height);
                    }
                }
                
                // 检查是否支持云台
                if (profile.getPTZConfiguration() != null) {
                    channelInfo.setPtzSupport(true);
                    channelInfo.setPtzToken(profile.getPTZConfiguration().getToken());
                    log.info("[ONVIF] 通道 {} 支持云台: profileToken={}", i + 1, profile.getToken());
                } else {
                    channelInfo.setPtzSupport(false);
                }
                
                // 检查是否支持音频
                if (profile.getAudioSourceConfiguration() != null) {
                    channelInfo.setAudioSupport(true);
                } else {
                    channelInfo.setAudioSupport(false);
                }
                
                channels.add(channelInfo);
                log.info("[ONVIF] 解析通道 {}: name={}, ptz={}, audio={}, resolution={}", 
                        i + 1, channelInfo.getChannelName(), channelInfo.isPtzSupport(), 
                        channelInfo.isAudioSupport(), channelInfo.getResolution());
            }
            
        } catch (Exception e) {
            log.error("[ONVIF] 查询设备Profiles失败: ip={}", deviceIp, e);
        }
        
        return channels;
    }

    /**
     * 创建 Media Service 客户端
     */
    private OnvifMediaService createMediaServiceClient(String serviceUrl) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(OnvifMediaService.class);
        factory.setAddress(serviceUrl);
        
        // 创建代理
        OnvifMediaService service = (OnvifMediaService) factory.create();
        
        // 配置认证
        Client client = ClientProxy.getClient(service);
        configureAuthentication(client);
        configureTimeout(client);
        
        return service;
    }

    /**
     * 配置 WS-Security 认证
     */
    private void configureAuthentication(Client client) {
        Map<String, Object> outProps = new HashMap<>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        outProps.put(WSHandlerConstants.USER, username);
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new OnvifPasswordCallback(username, password));
        
        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        client.getOutInterceptors().add(wssOut);
    }

    /**
     * 配置超时时间
     */
    private void configureTimeout(Client client) {
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout(timeout);
        policy.setReceiveTimeout(timeout);
        conduit.setClient(policy);
    }

    // 注意：OnvifChannelInfo 已提取为独立类 cn.iocoder.yudao.module.iot.service.onvif.OnvifChannelInfo
    // 避免 Hibernate Validator 解析方法签名时的类加载问题
}
