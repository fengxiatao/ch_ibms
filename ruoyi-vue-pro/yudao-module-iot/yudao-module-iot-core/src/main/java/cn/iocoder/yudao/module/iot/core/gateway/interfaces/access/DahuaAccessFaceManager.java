package cn.iocoder.yudao.module.iot.core.gateway.interfaces.access;

import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessFaceInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.BatchResult;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.FaceImageCheckResult;

import java.util.List;

/**
 * 人脸管理器 - 仅适用于二代设备
 * 使用 CLIENT_FaceInfoOpreate 接口
 * 
 * @author 长辉信息科技有限公司
 */
public interface DahuaAccessFaceManager {
    
    /**
     * 添加人脸
     * SDK: CLIENT_FaceInfoOpreate(EM_FACEINFO_OPREATE_ADD)
     * 注意: 人脸图片需要符合设备要求（分辨率、大小等）
     * 
     * @param loginHandle 登录句柄
     * @param faceInfo 人脸信息
     * @return 是否成功
     */
    boolean insertFace(long loginHandle, NetAccessFaceInfo faceInfo);
    
    /**
     * 批量添加人脸
     * 受 maxInsertRateFace 限制
     * 
     * @param loginHandle 登录句柄
     * @param faceInfoList 人脸信息列表
     * @return 批量操作结果
     */
    BatchResult insertFaces(long loginHandle, List<NetAccessFaceInfo> faceInfoList);
    
    /**
     * 获取人脸
     * SDK: CLIENT_FaceInfoOpreate(EM_FACEINFO_OPREATE_GET)
     * 
     * @param loginHandle 登录句柄
     * @param userId 用户ID
     * @return 人脸信息，不存在返回null
     */
    NetAccessFaceInfo getFace(long loginHandle, String userId);
    
    /**
     * 更新人脸
     * SDK: CLIENT_FaceInfoOpreate(EM_FACEINFO_OPREATE_UPDATE)
     * 
     * @param loginHandle 登录句柄
     * @param faceInfo 人脸信息
     * @return 是否成功
     */
    boolean updateFace(long loginHandle, NetAccessFaceInfo faceInfo);
    
    /**
     * 删除人脸
     * SDK: CLIENT_FaceInfoOpreate(EM_FACEINFO_OPREATE_REMOVE)
     * 
     * @param loginHandle 登录句柄
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean removeFace(long loginHandle, String userId);
    
    /**
     * 清空所有人脸
     * SDK: CLIENT_FaceInfoOpreate(EM_FACEINFO_OPREATE_CLEAR)
     * 
     * @param loginHandle 登录句柄
     * @return 是否成功
     */
    boolean clearAllFaces(long loginHandle);
    
    /**
     * 查询人脸数量
     * 
     * @param loginHandle 登录句柄
     * @return 人脸数量
     */
    int getFaceCount(long loginHandle);
    
    /**
     * 检查是否支持人脸功能
     * 
     * @param loginHandle 登录句柄
     * @return 是否支持
     */
    boolean isSupported(long loginHandle);
    
    /**
     * 检查人脸图片是否符合要求
     * 
     * @param imageData 图片数据
     * @param maxSizeKB 最大大小(KB)
     * @return 检查结果
     */
    FaceImageCheckResult checkFaceImage(byte[] imageData, int maxSizeKB);
    
    /**
     * 调整人脸图片大小
     * 
     * @param imageData 原始图片数据
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 调整后的图片数据
     */
    byte[] resizeFaceImage(byte[] imageData, int maxWidth, int maxHeight);
    
    /**
     * 压缩人脸图片到指定大小
     * 
     * @param imageData 原始图片数据
     * @param maxSizeKB 最大大小(KB)
     * @return 压缩后的图片数据
     */
    byte[] compressFaceImage(byte[] imageData, int maxSizeKB);
    
    /**
     * 获取最后一次操作的错误码
     * 
     * @return 错误码
     */
    int getLastErrorCode();
    
    /**
     * 获取最后一次操作的错误信息
     * 
     * @return 错误信息
     */
    String getLastErrorMessage();
}
