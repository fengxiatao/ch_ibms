package cn.iocoder.yudao.module.iot.core.onvif;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * ONVIF 动态客户端（使用 CXF 动态调用，无需 JAXB 类）
 * 
 * @author 长辉信息科技有限公司
 */
@Slf4j
public class OnvifDynamicClient {

    private final String deviceIp;
    private final String username;
    private final String password;
    private final int timeout;

    public OnvifDynamicClient(String deviceIp, String username, String password) {
        this(deviceIp, username, password, 10000);
    }

    public OnvifDynamicClient(String deviceIp, String username, String password, int timeout) {
        this.deviceIp = deviceIp;
        this.username = username;
        this.password = password;
        this.timeout = timeout;
    }

    /**
     * 查询设备的视频流配置文件（Profiles）
     */
    public List<OnvifChannelInfo> getProfiles() {
        List<OnvifChannelInfo> channels = new ArrayList<>();
        
        try {
            // 1. 构建 SOAP 请求
            String soapRequest = buildGetProfilesRequest();
            
            // 2. 发送 HTTP 请求
            String mediaServiceUrl = String.format("http://%s/onvif/media_service", deviceIp);
            log.info("[ONVIF] 查询设备Profiles: ip={}, url={}", deviceIp, mediaServiceUrl);
            
            String soapResponse = sendSoapRequest(mediaServiceUrl, soapRequest);
            
            // 3. 解析响应
            channels = parseGetProfilesResponse(soapResponse);
            
            log.info("[ONVIF] 查询设备Profiles成功: profileCount={}", channels.size());
            
        } catch (Exception e) {
            log.error("[ONVIF] 查询设备Profiles失败: ip={}", deviceIp, e);
            throw new RuntimeException("ONVIF 查询失败: " + e.getMessage(), e);
        }
        
        return channels;
    }

    /**
     * 构建 GetProfiles SOAP 请求
     */
    private String buildGetProfilesRequest() {
        try {
            // 生成 WS-Security 认证信息
            String nonce = generateNonce();
            String created = generateTimestamp();
            String passwordDigest = generatePasswordDigest(nonce, created, password);
            
            // 打印认证调试信息
            log.info("[ONVIF] 认证信息: username={}, nonce={}, created={}, passwordDigest={}", 
                    username, nonce, created, passwordDigest);
            
            String soapRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" " +
                    "xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\">" +
                    "<s:Header>" +
                    "<Security s:mustUnderstand=\"1\" xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" " +
                    "xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">" +
                    "<UsernameToken wsu:Id=\"UsernameToken-1\">" +
                    "<Username>" + username + "</Username>" +
                    "<Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">" +
                    passwordDigest +
                    "</Password>" +
                    "<Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">" +
                    nonce +
                    "</Nonce>" +
                    "<wsu:Created>" + created + "</wsu:Created>" +
                    "</UsernameToken>" +
                    "</Security>" +
                    "</s:Header>" +
                    "<s:Body>" +
                    "<trt:GetProfiles/>" +
                    "</s:Body>" +
                    "</s:Envelope>";
            
            log.info("[ONVIF] SOAP 请求:\n{}", soapRequest);
            return soapRequest;
            
        } catch (Exception e) {
            throw new RuntimeException("构建 SOAP 请求失败", e);
        }
    }

    /**
     * 生成随机 Nonce（Base64 编码）
     */
    private String generateNonce() {
        byte[] nonceBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(nonceBytes);
        return Base64.getEncoder().encodeToString(nonceBytes);
    }

    /**
     * 生成 ISO 8601 时间戳（毫秒精度）
     * 注意：某些设备不支持纳秒级精度，必须截断为毫秒
     */
    private String generateTimestamp() {
        java.time.Instant now = java.time.Instant.now();
        // 截断为毫秒精度：2025-11-23T01:04:16.792Z
        return now.truncatedTo(java.time.temporal.ChronoUnit.MILLIS).toString();
    }

    /**
     * 生成 WS-Security PasswordDigest
     * PasswordDigest = Base64(SHA1(nonce + created + password))
     */
    private String generatePasswordDigest(String nonceBase64, String created, String password) throws Exception {
        byte[] nonceBytes = Base64.getDecoder().decode(nonceBase64);
        byte[] createdBytes = created.getBytes(StandardCharsets.UTF_8);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        
        // 拼接: nonce + created + password
        byte[] combined = new byte[nonceBytes.length + createdBytes.length + passwordBytes.length];
        System.arraycopy(nonceBytes, 0, combined, 0, nonceBytes.length);
        System.arraycopy(createdBytes, 0, combined, nonceBytes.length, createdBytes.length);
        System.arraycopy(passwordBytes, 0, combined, nonceBytes.length + createdBytes.length, passwordBytes.length);
        
        // SHA-1 哈希
        java.security.MessageDigest sha1 = java.security.MessageDigest.getInstance("SHA-1");
        byte[] digest = sha1.digest(combined);
        
        // Base64 编码
        return Base64.getEncoder().encodeToString(digest);
    }

    /**
     * 发送 SOAP 请求
     */
    private String sendSoapRequest(String serviceUrl, String soapRequest) throws Exception {
        URL url = new URL(serviceUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // 设置请求属性
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8; action=\"http://www.onvif.org/ver10/media/wsdl/GetProfiles\"");
            connection.setRequestProperty("Content-Length", String.valueOf(soapRequest.length()));
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoOutput(true);
            
            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                os.write(soapRequest.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            
            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                byte[] responseBytes = connection.getInputStream().readAllBytes();
                String response = new String(responseBytes, StandardCharsets.UTF_8);
                log.debug("[ONVIF] SOAP 响应:\n{}", response);
                return response;
            } else {
                // 读取错误响应
                String errorResponse = "";
                try {
                    if (connection.getErrorStream() != null) {
                        byte[] errorBytes = connection.getErrorStream().readAllBytes();
                        errorResponse = new String(errorBytes, StandardCharsets.UTF_8);
                        log.error("[ONVIF] HTTP {} 错误响应:\n{}", responseCode, errorResponse);
                    }
                } catch (Exception e) {
                    log.warn("[ONVIF] 无法读取错误响应", e);
                }
                throw new RuntimeException("HTTP 请求失败: " + responseCode + ", 响应: " + errorResponse);
            }
            
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 解析 GetProfiles 响应
     */
    private List<OnvifChannelInfo> parseGetProfilesResponse(String soapResponse) throws Exception {
        // 解析 XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(soapResponse.getBytes(StandardCharsets.UTF_8)));
        
        // 查找 Profiles 节点
        NodeList profileNodes = doc.getElementsByTagNameNS("*", "Profiles");
        log.info("[ONVIF] 找到 {} 个 Profile", profileNodes.getLength());
        
        // 按通道分组（使用 SourceToken 作为通道标识）
        java.util.Map<String, OnvifChannelInfo> channelMap = new java.util.LinkedHashMap<>();
        
        for (int i = 0; i < profileNodes.getLength(); i++) {
            Node profileNode = profileNodes.item(i);
            if (profileNode.getNodeType() == Node.ELEMENT_NODE) {
                Element profileElement = (Element) profileNode;
                
                // 解析 Profile Name
                String profileName = "";
                NodeList nameNodes = profileElement.getElementsByTagNameNS("*", "Name");
                if (nameNodes.getLength() > 0) {
                    profileName = nameNodes.item(0).getTextContent();
                }
                
                log.info("[ONVIF] 解析 Profile: name={}", profileName);
                
                // 过滤辅码流和第三码流（不区分大小写）
                String lowerProfileName = profileName.toLowerCase();
                if (lowerProfileName.contains("substream") || lowerProfileName.contains("sub_stream") ||
                    lowerProfileName.contains("thirdstream") || lowerProfileName.contains("third_stream") ||
                    lowerProfileName.contains("stream2") || lowerProfileName.contains("stream3")) {
                    log.debug("[ONVIF] 跳过辅码流 Profile: {}", profileName);
                    continue;
                }
                
                // 必须有 VideoEncoderConfiguration（跳过纯音频 Profile）
                NodeList videoEncoderNodes = profileElement.getElementsByTagNameNS("*", "VideoEncoderConfiguration");
                if (videoEncoderNodes.getLength() == 0) {
                    log.debug("[ONVIF] 跳过无视频编码的 Profile: {}", profileName);
                    continue;
                }
                
                // 解析 VideoSourceConfiguration
                NodeList videoSourceNodes = profileElement.getElementsByTagNameNS("*", "VideoSourceConfiguration");
                if (videoSourceNodes.getLength() == 0) {
                    log.debug("[ONVIF] 跳过无视频源的 Profile: {}", profileName);
                    continue;
                }
                
                Element videoSourceElement = (Element) videoSourceNodes.item(0);
                String sourceToken = "";
                NodeList sourceTokenNodes = videoSourceElement.getElementsByTagNameNS("*", "SourceToken");
                if (sourceTokenNodes.getLength() > 0) {
                    sourceToken = sourceTokenNodes.item(0).getTextContent();
                }
                
                // 如果该通道已存在，跳过
                if (channelMap.containsKey(sourceToken)) {
                    continue;
                }
                
                // 创建通道信息
                OnvifChannelInfo channel = new OnvifChannelInfo();
                channel.setChannelNo(channelMap.size() + 1);
                channel.setVideoSourceToken(sourceToken);
                
                // 解析 Profile Token
                String profileToken = profileElement.getAttribute("token");
                channel.setProfileToken(profileToken);
                
                // 解析通道名称
                channel.setChannelName(profileName);
                
                // 解析分辨率
                NodeList boundsNodes = videoSourceElement.getElementsByTagNameNS("*", "Bounds");
                if (boundsNodes.getLength() > 0) {
                    Element boundsElement = (Element) boundsNodes.item(0);
                    try {
                        int width = Integer.parseInt(boundsElement.getAttribute("width"));
                        int height = Integer.parseInt(boundsElement.getAttribute("height"));
                        channel.setResolution(width + "x" + height);
                    } catch (NumberFormatException e) {
                        log.warn("[ONVIF] 解析分辨率失败", e);
                    }
                }
                
                // 检查是否支持 PTZ
                NodeList ptzNodes = profileElement.getElementsByTagNameNS("*", "PTZConfiguration");
                boolean supportPtz = ptzNodes.getLength() > 0;
                channel.setSupportPtz(supportPtz);
                
                if (supportPtz) {
                    Element ptzElement = (Element) ptzNodes.item(0);
                    String ptzToken = ptzElement.getAttribute("token");
                    channel.setPtzToken(ptzToken);
                    
                    // 解析 PTZ NodeToken
                    NodeList nodeTokenNodes = ptzElement.getElementsByTagNameNS("*", "NodeToken");
                    if (nodeTokenNodes.getLength() > 0) {
                        channel.setPtzNodeToken(nodeTokenNodes.item(0).getTextContent());
                    }
                }
                
                channelMap.put(sourceToken, channel);
                
                log.info("[ONVIF] 解析通道: channelNo={}, name={}, sourceToken={}, profileToken={}, ptz={}", 
                        channel.getChannelNo(), channel.getChannelName(), 
                        channel.getVideoSourceToken(), channel.getProfileToken(), 
                        channel.getSupportPtz());
            }
        }
        
        List<OnvifChannelInfo> channels = new ArrayList<>(channelMap.values());
        log.info("[ONVIF] 共解析出 {} 个物理通道", channels.size());
        
        return channels;
    }


    /**
     * ONVIF 通道信息
     */
    @Data
    public static class OnvifChannelInfo {
        /** 通道编号 */
        private Integer channelNo;
        /** Profile Token（用于获取流地址） */
        private String profileToken;
        /** 通道名称 */
        private String channelName;
        /** 视频源 Token */
        private String videoSourceToken;
        /** 分辨率（如：2048x1536） */
        private String resolution;
        /** 是否支持云台控制 */
        private Boolean supportPtz;
        /** PTZ Token */
        private String ptzToken;
        /** PTZ Node Token */
        private String ptzNodeToken;
    }
}
