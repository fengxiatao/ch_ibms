package cn.iocoder.yudao.module.iot.service.video;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraRecordingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraRecordingRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraRecordingDO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 摄像头录像记录 Service接口
 *
 * @author 长辉信息
 */
public interface CameraRecordingService {

    /**
     * 分页查询录像记录
     *
     * @param pageReqVO 分页查询条件
     * @return 录像记录分页结果
     */
    PageResult<CameraRecordingRespVO> getRecordingPage(CameraRecordingPageReqVO pageReqVO);

    /**
     * 获取录像记录详情
     *
     * @param id 录像记录ID
     * @return 录像记录详情
     */
    IotCameraRecordingDO getRecording(Long id);

    /**
     * 删除录像记录
     *
     * @param id 录像记录ID
     */
    void deleteRecording(Long id);

    /**
     * 开始录像（手动录像）
     *
     * @param deviceId 设备ID
     * @param duration 录像时长（秒），0表示持续录像直到手动停止
     * @return 录像记录ID
     */
    Long startRecording(Long deviceId, Integer duration, String policy);

    /**
     * 停止录像
     *
     * @param recordingId 录像记录ID
     */
    void stopRecording(Long recordingId);

    /**
     * 创建录像记录（由ZLMediaKit回调触发）
     *
     * @param deviceId 设备ID
     * @param recordingType 录像类型（1=手动 2=定时 3=报警 4=移动侦测）
     * @param startTime 开始时间
     * @param filePath 文件路径
     * @return 录像记录ID
     */
    Long createRecording(Long deviceId, Integer recordingType, java.time.LocalDateTime startTime, String filePath);

    /**
     * 完成录像记录（由ZLMediaKit回调触发）
     *
     * @param recordingId 录像记录ID
     * @param endTime 结束时间
     * @param fileSize 文件大小（字节）
     * @param duration 录像时长（秒）
     */
    void completeRecording(Long recordingId, java.time.LocalDateTime endTime, Long fileSize, Integer duration);

    /**
     * 通过文件上传创建录像记录（复用基础设施文件存储）
     *
     * @param deviceId 设备ID
     * @param recordingType 录像类型（默认1=手动）
     * @param file 视频文件（建议 mp4）
     * @return 录像记录ID
     */
    Long uploadRecording(Long deviceId, Integer recordingType, MultipartFile file) throws Exception;

}


