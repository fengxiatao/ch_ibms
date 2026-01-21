package cn.iocoder.yudao.module.iot.service.parking;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.module.iot.controller.app.parking.vo.ParkingPayRespVO;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 停车场微信支付 Service 实现类
 * 
 * 参考充电桩项目的微信支付实现
 */
@Service
@Slf4j
public class ParkingWechatPayServiceImpl implements ParkingWechatPayService {

    /**
     * 小程序AppID
     */
    @Value("${wxpay.parking.appId:}")
    private String appId;

    /**
     * 商户号
     */
    @Value("${wxpay.parking.mchId:}")
    private String mchId;

    /**
     * API密钥
     */
    @Value("${wxpay.parking.apiKey:}")
    private String apiKey;

    /**
     * 小程序密钥
     */
    @Value("${wxpay.parking.secret:}")
    private String secret;

    /**
     * 支付回调地址
     */
    @Value("${wxpay.parking.notifyUrl:}")
    private String notifyUrl;

    /**
     * 退款回调地址
     */
    @Value("${wxpay.parking.refundNotifyUrl:}")
    private String refundNotifyUrl;

    /**
     * 证书路径
     */
    @Value("${wxpay.parking.certPath:}")
    private String certPath;

    /**
     * 微信获取session_key接口
     */
    private static final String WECHAT_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    /**
     * 微信统一下单接口
     */
    private static final String WECHAT_UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    /**
     * 微信退款接口
     */
    private static final String WECHAT_REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";

    /**
     * 微信退款查询接口
     */
    private static final String WECHAT_REFUND_QUERY_URL = "https://api.mch.weixin.qq.com/pay/refundquery";

    @Override
    public ParkingPayRespVO createUnifiedOrder(String code, String outTradeNo, String body,
                                               BigDecimal amount, String plateNumber) {
        // 1. 通过code获取openid
        String openid = getOpenidByCode(code);
        if (StrUtil.isBlank(openid)) {
            throw exception(WECHAT_AUTH_FAILED);
        }

        // 2. 构建统一下单参数
        SortedMap<String, Object> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", generateNonceStr());
        params.put("body", body);
        params.put("out_trade_no", outTradeNo);
        params.put("total_fee", amount.multiply(new BigDecimal(100)).intValue()); // 金额单位：分
        params.put("spbill_create_ip", getLocalIp());
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "JSAPI");
        params.put("openid", openid);

        // 3. 生成签名
        String sign = createSign(params);
        params.put("sign", sign);

        // 4. 发送统一下单请求
        String xmlRequest = mapToXml(params);
        log.info("[createUnifiedOrder] 统一下单请求：{}", xmlRequest);

        String xmlResponse = HttpUtil.post(WECHAT_UNIFIED_ORDER_URL, xmlRequest);
        log.info("[createUnifiedOrder] 统一下单响应：{}", xmlResponse);

        // 5. 解析响应
        Map<String, String> resultMap = parseNotifyData(xmlResponse);
        String returnCode = resultMap.get("return_code");
        String resultCode = resultMap.get("result_code");

        if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
            String errMsg = resultMap.get("return_msg");
            if (StrUtil.isBlank(errMsg)) {
                errMsg = resultMap.get("err_code_des");
            }
            log.error("[createUnifiedOrder] 统一下单失败：{}", errMsg);
            throw exception(WECHAT_PAY_FAILED);
        }

        String prepayId = resultMap.get("prepay_id");

        // 6. 构建返回给小程序的支付参数
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = generateNonceStr();

        SortedMap<String, Object> payParams = new TreeMap<>();
        payParams.put("appId", appId);
        payParams.put("timeStamp", timeStamp);
        payParams.put("nonceStr", nonceStr);
        payParams.put("package", "prepay_id=" + prepayId);
        payParams.put("signType", "MD5");

        String paySign = createSign(payParams);

        ParkingPayRespVO respVO = new ParkingPayRespVO();
        respVO.setTimeStamp(timeStamp);
        respVO.setNonceStr(nonceStr);
        respVO.setPackageValue("prepay_id=" + prepayId);
        respVO.setSignType("MD5");
        respVO.setPaySign(paySign);

        return respVO;
    }

    @Override
    public Map<String, String> parseNotifyData(String xmlData) {
        try {
            Map<String, String> data = new HashMap<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8)));
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }
            return data;
        } catch (Exception e) {
            log.error("[parseNotifyData] 解析XML失败：", e);
            return new HashMap<>();
        }
    }

    @Override
    public boolean verifySign(Map<String, String> data) {
        String sign = data.get("sign");
        if (StrUtil.isBlank(sign)) {
            return false;
        }

        // 构建签名参数
        SortedMap<String, Object> sortedData = new TreeMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!"sign".equals(entry.getKey())) {
                sortedData.put(entry.getKey(), entry.getValue());
            }
        }

        String calculatedSign = createSign(sortedData);
        return sign.equals(calculatedSign);
    }

    @Override
    public Map<String, String> refund(String outTradeNo, String outRefundNo,
                                      BigDecimal totalFee, BigDecimal refundFee, String refundReason) {
        log.info("[refund] 申请退款，原订单号：{}，退款单号：{}，订单金额：{}，退款金额：{}", 
                outTradeNo, outRefundNo, totalFee, refundFee);

        // 1. 构建退款参数
        SortedMap<String, Object> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", generateNonceStr());
        params.put("out_trade_no", outTradeNo);
        params.put("out_refund_no", outRefundNo);
        params.put("total_fee", totalFee.multiply(new BigDecimal(100)).intValue()); // 金额单位：分
        params.put("refund_fee", refundFee.multiply(new BigDecimal(100)).intValue());
        if (StrUtil.isNotBlank(refundReason)) {
            params.put("refund_desc", refundReason);
        }
        if (StrUtil.isNotBlank(refundNotifyUrl)) {
            params.put("notify_url", refundNotifyUrl);
        }

        // 2. 生成签名
        String sign = createSign(params);
        params.put("sign", sign);

        // 3. 发送退款请求（需要证书）
        String xmlRequest = mapToXml(params);
        log.info("[refund] 退款请求：{}", xmlRequest);

        String xmlResponse;
        try {
            xmlResponse = postWithCert(WECHAT_REFUND_URL, xmlRequest);
        } catch (Exception e) {
            log.error("[refund] 退款请求异常：", e);
            throw exception(WECHAT_REFUND_FAILED);
        }
        log.info("[refund] 退款响应：{}", xmlResponse);

        // 4. 解析响应
        Map<String, String> resultMap = parseNotifyData(xmlResponse);
        String returnCode = resultMap.get("return_code");
        String resultCode = resultMap.get("result_code");

        if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
            String errMsg = resultMap.get("return_msg");
            if (StrUtil.isBlank(errMsg)) {
                errMsg = resultMap.get("err_code_des");
            }
            log.error("[refund] 退款失败：{}", errMsg);
            throw exception(WECHAT_REFUND_FAILED);
        }

        log.info("[refund] 退款成功，微信退款单号：{}", resultMap.get("refund_id"));
        return resultMap;
    }

    @Override
    public Map<String, String> queryRefund(String outRefundNo) {
        log.info("[queryRefund] 查询退款状态，退款单号：{}", outRefundNo);

        // 1. 构建查询参数
        SortedMap<String, Object> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", generateNonceStr());
        params.put("out_refund_no", outRefundNo);

        // 2. 生成签名
        String sign = createSign(params);
        params.put("sign", sign);

        // 3. 发送查询请求
        String xmlRequest = mapToXml(params);
        String xmlResponse = HttpUtil.post(WECHAT_REFUND_QUERY_URL, xmlRequest);
        log.info("[queryRefund] 查询响应：{}", xmlResponse);

        // 4. 解析响应
        return parseNotifyData(xmlResponse);
    }

    /**
     * 带证书的HTTPS请求（退款必须）
     */
    private String postWithCert(String url, String xmlData) throws Exception {
        // 如果未配置证书路径，使用模拟退款（开发测试用）
        if (StrUtil.isBlank(certPath)) {
            log.warn("[postWithCert] 未配置微信支付证书，使用模拟退款模式");
            return buildMockRefundResponse(xmlData);
        }

        // 加载证书
        java.io.FileInputStream fis = new java.io.FileInputStream(certPath);
        java.security.KeyStore keyStore = java.security.KeyStore.getInstance("PKCS12");
        keyStore.load(fis, mchId.toCharArray());
        fis.close();

        // 创建SSL上下文
        javax.net.ssl.KeyManagerFactory kmf = javax.net.ssl.KeyManagerFactory.getInstance(
                javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, mchId.toCharArray());

        javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new java.security.SecureRandom());

        // 创建HTTPS连接
        java.net.URL urlObj = new java.net.URL(url);
        javax.net.ssl.HttpsURLConnection conn = (javax.net.ssl.HttpsURLConnection) urlObj.openConnection();
        conn.setSSLSocketFactory(sslContext.getSocketFactory());
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");

        // 发送请求
        java.io.OutputStream os = conn.getOutputStream();
        os.write(xmlData.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        // 读取响应
        java.io.InputStream is = conn.getInputStream();
        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        return response.toString();
    }

    /**
     * 构建模拟退款响应（开发测试用）
     */
    private String buildMockRefundResponse(String xmlData) {
        Map<String, String> requestData = parseNotifyData(xmlData);
        String outRefundNo = requestData.get("out_refund_no");
        String refundId = "MOCK_" + System.currentTimeMillis();
        
        return "<xml>" +
                "<return_code><![CDATA[SUCCESS]]></return_code>" +
                "<return_msg><![CDATA[OK]]></return_msg>" +
                "<appid><![CDATA[" + appId + "]]></appid>" +
                "<mch_id><![CDATA[" + mchId + "]]></mch_id>" +
                "<nonce_str><![CDATA[" + generateNonceStr() + "]]></nonce_str>" +
                "<sign><![CDATA[MOCK_SIGN]]></sign>" +
                "<result_code><![CDATA[SUCCESS]]></result_code>" +
                "<out_refund_no><![CDATA[" + outRefundNo + "]]></out_refund_no>" +
                "<refund_id><![CDATA[" + refundId + "]]></refund_id>" +
                "</xml>";
    }

    /**
     * 通过微信授权码获取openid
     */
    private String getOpenidByCode(String code) {
        String url = WECHAT_SESSION_URL + "?appid=" + appId + "&secret=" + secret 
                + "&js_code=" + code + "&grant_type=authorization_code";

        log.info("[getOpenidByCode] 获取openid请求：{}", url);
        String response = HttpUtil.get(url);
        log.info("[getOpenidByCode] 获取openid响应：{}", response);

        JSONObject jsonObject = JSON.parseObject(response);
        if (jsonObject == null) {
            log.error("[getOpenidByCode] 微信接口返回为空");
            return null;
        }

        Integer errcode = jsonObject.getInteger("errcode");
        if (errcode != null && errcode != 0) {
            log.error("[getOpenidByCode] 获取openid失败，errcode：{}，errmsg：{}", 
                    errcode, jsonObject.getString("errmsg"));
            return null;
        }

        return jsonObject.getString("openid");
    }

    /**
     * 生成签名
     */
    private String createSign(SortedMap<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && !"".equals(value) && !"sign".equals(key) && !"key".equals(key)) {
                sb.append(key).append("=").append(value).append("&");
            }
        }
        sb.append("key=").append(apiKey);
        return md5(sb.toString()).toUpperCase();
    }

    /**
     * MD5加密
     */
    private String md5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * Map转XML
     */
    private String mapToXml(SortedMap<String, Object> map) {
        StringBuilder sb = new StringBuilder("<xml>");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append("<").append(entry.getKey()).append(">")
              .append(entry.getValue())
              .append("</").append(entry.getKey()).append(">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 获取本机IP
     */
    private String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
