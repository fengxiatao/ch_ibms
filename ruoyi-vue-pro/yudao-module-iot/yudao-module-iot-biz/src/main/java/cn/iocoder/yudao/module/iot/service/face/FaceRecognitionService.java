package cn.iocoder.yudao.module.iot.service.face;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人脸识别服务集成
 * 
 * @author 长辉开发团队
 */
@Service
@Slf4j
public class FaceRecognitionService {

    @Value("${face.service.url:http://localhost:5000}")
    private String faceServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 检测人脸
     * 
     * @param imageData 图片数据
     * @return 检测结果
     */
    public FaceDetectionResult detectFaces(byte[] imageData) {
        try {
            // 构建 multipart 请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageData) {
                @Override
                public String getFilename() {
                    return "image.jpg";
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String apiUrl = faceServiceUrl + "/api/face/detect";
            
            log.info("[detectFaces][开始检测人脸] imageSize={}", imageData.length);
            
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.getBody());
                
                FaceDetectionResult result = new FaceDetectionResult();
                result.setSuccess(jsonResponse.getBool("success", false));
                result.setCount(jsonResponse.getInt("count", 0));
                result.setProcessTimeMs(jsonResponse.getDouble("process_time_ms", 0.0));
                
                // 解析人脸列表
                JSONArray facesArray = jsonResponse.getJSONArray("faces");
                List<FaceInfo> faces = new ArrayList<>();
                if (facesArray != null) {
                    for (int i = 0; i < facesArray.size(); i++) {
                        JSONObject faceObj = facesArray.getJSONObject(i);
                        FaceInfo face = new FaceInfo();
                        face.setBbox(faceObj.getJSONArray("bbox").toList(Double.class));
                        face.setConfidence(faceObj.getDouble("confidence", 0.0));
                        face.setAge(faceObj.getInt("age", null));
                        face.setGender(faceObj.getStr("gender", null));
                        
                        // 特征向量
                        JSONArray embeddingArray = faceObj.getJSONArray("embedding");
                        if (embeddingArray != null) {
                            face.setEmbedding(embeddingArray.toList(Double.class));
                        }
                        
                        faces.add(face);
                    }
                }
                result.setFaces(faces);
                
                log.info("[detectFaces][人脸检测成功] count={}, processTime={}ms", 
                        result.getCount(), result.getProcessTimeMs());
                
                return result;
            } else {
                log.error("[detectFaces][人脸检测失败] status={}", response.getStatusCode());
                throw new RuntimeException("人脸检测失败");
            }
            
        } catch (Exception e) {
            log.error("[detectFaces][人脸检测异常]", e);
            throw new RuntimeException("人脸检测异常: " + e.getMessage(), e);
        }
    }

    /**
     * 比对两张人脸
     * 
     * @param image1 第一张图片
     * @param image2 第二张图片
     * @return 比对结果
     */
    public FaceCompareResult compareFaces(byte[] image1, byte[] image2) {
        try {
            // 构建 multipart 请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file1", new ByteArrayResource(image1) {
                @Override
                public String getFilename() {
                    return "image1.jpg";
                }
            });
            body.add("file2", new ByteArrayResource(image2) {
                @Override
                public String getFilename() {
                    return "image2.jpg";
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String apiUrl = faceServiceUrl + "/api/face/compare";
            
            log.info("[compareFaces][开始比对人脸]");
            
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.getBody());
                
                FaceCompareResult result = new FaceCompareResult();
                result.setSuccess(jsonResponse.getBool("success", false));
                result.setSimilarity(jsonResponse.getDouble("similarity", 0.0));
                result.setSamePerson(jsonResponse.getBool("is_same_person", false));
                
                log.info("[compareFaces][人脸比对成功] similarity={}, isSamePerson={}", 
                        result.getSimilarity(), result.isSamePerson());
                
                return result;
            } else {
                log.error("[compareFaces][人脸比对失败] status={}", response.getStatusCode());
                throw new RuntimeException("人脸比对失败");
            }
            
        } catch (Exception e) {
            log.error("[compareFaces][人脸比对异常]", e);
            throw new RuntimeException("人脸比对异常: " + e.getMessage(), e);
        }
    }

    /**
     * 提取人脸特征
     * 
     * @param imageData 图片数据
     * @return 特征向量
     */
    public List<Double> extractFeatures(byte[] imageData) {
        try {
            // 构建 multipart 请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageData) {
                @Override
                public String getFilename() {
                    return "image.jpg";
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String apiUrl = faceServiceUrl + "/api/face/extract";
            
            log.info("[extractFeatures][开始提取人脸特征]");
            
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.getBody());
                
                if (jsonResponse.getBool("success", false)) {
                    JSONArray embeddingArray = jsonResponse.getJSONArray("embedding");
                    List<Double> embedding = embeddingArray.toList(Double.class);
                    
                    log.info("[extractFeatures][特征提取成功] dimension={}", embedding.size());
                    
                    return embedding;
                } else {
                    throw new RuntimeException("特征提取失败");
                }
            } else {
                log.error("[extractFeatures][特征提取失败] status={}", response.getStatusCode());
                throw new RuntimeException("特征提取失败");
            }
            
        } catch (Exception e) {
            log.error("[extractFeatures][特征提取异常]", e);
            throw new RuntimeException("特征提取异常: " + e.getMessage(), e);
        }
    }

    /**
     * 在图片上绘制人脸框
     * 
     * @param imageData 图片数据
     * @return Base64 编码的标注后图片
     */
    public String drawFaces(byte[] imageData) {
        try {
            // 构建 multipart 请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageData) {
                @Override
                public String getFilename() {
                    return "image.jpg";
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String apiUrl = faceServiceUrl + "/api/face/draw";
            
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.getBody());
                
                if (jsonResponse.getBool("success", false)) {
                    return jsonResponse.getStr("image");
                } else {
                    throw new RuntimeException("绘制人脸框失败");
                }
            } else {
                log.error("[drawFaces][绘制人脸框失败] status={}", response.getStatusCode());
                throw new RuntimeException("绘制人脸框失败");
            }
            
        } catch (Exception e) {
            log.error("[drawFaces][绘制人脸框异常]", e);
            throw new RuntimeException("绘制人脸框异常: " + e.getMessage(), e);
        }
    }

    // ==================== 数据模型 ====================

    /**
     * 人脸检测结果
     */
    @lombok.Data
    public static class FaceDetectionResult {
        private Boolean success;
        private Integer count;
        private List<FaceInfo> faces;
        private Double processTimeMs;
    }

    /**
     * 人脸信息
     */
    @lombok.Data
    public static class FaceInfo {
        private List<Double> bbox;  // [x1, y1, x2, y2]
        private Double confidence;
        private Integer age;
        private String gender;
        private List<Double> embedding;  // 512维特征向量
    }

    /**
     * 人脸比对结果
     */
    @lombok.Data
    public static class FaceCompareResult {
        private Boolean success;
        private Double similarity;
        private boolean samePerson;
    }
}


















