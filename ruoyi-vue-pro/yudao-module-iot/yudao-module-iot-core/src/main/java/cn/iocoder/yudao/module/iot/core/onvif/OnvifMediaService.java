package cn.iocoder.yudao.module.iot.core.onvif;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * ONVIF Media Service 接口定义
 * 
 * <p>简化版的 ONVIF Media Service WSDL 接口</p>
 * 
 * @author 长辉信息科技有限公司
 */
@WebService(targetNamespace = "http://www.onvif.org/ver10/media/wsdl", name = "Media")
public interface OnvifMediaService {

    @WebMethod(operationName = "GetProfiles", action = "http://www.onvif.org/ver10/media/wsdl/GetProfiles")
    @WebResult(name = "GetProfilesResponse", targetNamespace = "http://www.onvif.org/ver10/media/wsdl")
    GetProfilesResponse getProfiles(
            @WebParam(name = "GetProfiles", targetNamespace = "http://www.onvif.org/ver10/media/wsdl")
            GetProfiles parameters
    );
}

/**
 * GetProfiles 请求
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetProfiles", namespace = "http://www.onvif.org/ver10/media/wsdl")
@XmlRootElement(name = "GetProfiles", namespace = "http://www.onvif.org/ver10/media/wsdl")
class GetProfiles {
    // 无参数
}

/**
 * GetProfiles 响应
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetProfilesResponse", namespace = "http://www.onvif.org/ver10/media/wsdl")
@XmlRootElement(name = "GetProfilesResponse", namespace = "http://www.onvif.org/ver10/media/wsdl")
class GetProfilesResponse {
    
    @XmlElement(name = "Profiles")
    private List<Profile> profiles;

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }
}

/**
 * Profile（视频流配置文件）
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Profile", namespace = "http://www.onvif.org/ver10/schema")
class Profile {
    
    @XmlAttribute(name = "token")
    private String token;
    
    @XmlElement(name = "Name")
    private String name;
    
    @XmlElement(name = "VideoSourceConfiguration")
    private VideoSourceConfiguration videoSourceConfiguration;
    
    @XmlElement(name = "AudioSourceConfiguration")
    private AudioSourceConfiguration audioSourceConfiguration;
    
    @XmlElement(name = "PTZConfiguration")
    private PTZConfiguration ptzConfiguration;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VideoSourceConfiguration getVideoSourceConfiguration() {
        return videoSourceConfiguration;
    }

    public void setVideoSourceConfiguration(VideoSourceConfiguration videoSourceConfiguration) {
        this.videoSourceConfiguration = videoSourceConfiguration;
    }

    public AudioSourceConfiguration getAudioSourceConfiguration() {
        return audioSourceConfiguration;
    }

    public void setAudioSourceConfiguration(AudioSourceConfiguration audioSourceConfiguration) {
        this.audioSourceConfiguration = audioSourceConfiguration;
    }

    public PTZConfiguration getPTZConfiguration() {
        return ptzConfiguration;
    }

    public void setPTZConfiguration(PTZConfiguration ptzConfiguration) {
        this.ptzConfiguration = ptzConfiguration;
    }
}

/**
 * 视频源配置
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VideoSourceConfiguration", namespace = "http://www.onvif.org/ver10/schema")
class VideoSourceConfiguration {
    
    @XmlElement(name = "SourceToken")
    private String sourceToken;
    
    @XmlElement(name = "Bounds")
    private VideoBounds bounds;

    public String getSourceToken() {
        return sourceToken;
    }

    public void setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
    }

    public VideoBounds getBounds() {
        return bounds;
    }

    public void setBounds(VideoBounds bounds) {
        this.bounds = bounds;
    }
}

/**
 * 视频分辨率
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VideoBounds", namespace = "http://www.onvif.org/ver10/schema")
class VideoBounds {
    
    @XmlAttribute(name = "width")
    private int width;
    
    @XmlAttribute(name = "height")
    private int height;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}

/**
 * 音频源配置
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AudioSourceConfiguration", namespace = "http://www.onvif.org/ver10/schema")
class AudioSourceConfiguration {
    // 简化实现，只需要判断是否存在
}

/**
 * PTZ 配置
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PTZConfiguration", namespace = "http://www.onvif.org/ver10/schema")
class PTZConfiguration {
    
    @XmlAttribute(name = "token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
