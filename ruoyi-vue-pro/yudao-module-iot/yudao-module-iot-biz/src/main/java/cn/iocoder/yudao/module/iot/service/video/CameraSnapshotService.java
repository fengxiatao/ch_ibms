package cn.iocoder.yudao.module.iot.service.video;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraSnapshotPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraSnapshotRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraSnapshotDO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 摄像头抓图记录 Service接口
 *
 * @author 长辉信息
 */
public interface CameraSnapshotService {

    /**
     * 分页查询抓图记录
     *
     * @param pageReqVO 分页查询条件
     * @return 抓图记录分页结果
     */
    PageResult<CameraSnapshotRespVO> getSnapshotPage(CameraSnapshotPageReqVO pageReqVO);

    /**
     * 获取抓图记录详情
     *
     * @param id 抓图记录ID
     * @return 抓图记录详情
     */
    IotCameraSnapshotDO getSnapshot(Long id);

    /**
     * 删除抓图记录
     *
     * @param id 抓图记录ID
     */
    void deleteSnapshot(Long id);

    /**
     * 标记抓图记录为已处理
     *
     * @param id 抓图记录ID
     * @param processor 处理人
     * @param remark 处理备注
     */
    void markAsProcessed(Long id, String processor, String remark);

    /**
     * 手动抓拍（立即抓图）
     *
     * @param channelId 通道ID
     * @return 抓图记录ID
     */
    Long captureSnapshot(Long channelId);

    /**
     * 获取通道最近一条抓图记录
     *
     * @param channelId 通道ID
     * @return 抓图记录
     */
    CameraSnapshotRespVO getLatestSnapshot(Long channelId);

    /**
     * 创建抓图记录（由Gateway回调触发）
     *
     * @param channelId 通道ID
     * @param snapshotType 抓图类型（1=手动 2=定时 3=报警 4=移动侦测）
     * @param imageData Base64编码的图片数据
     * @return 抓图记录ID
     */
    Long createSnapshot(Long channelId, Integer snapshotType, String imageData);

    /**
     * 创建抓图记录（由Gateway回调触发，保存文件路径）
     *
     * @param channelId 通道ID
     * @param snapshotType 抓图类型
     * @param filePath 文件路径
     * @param fileSize 文件大小
     * @return 抓图记录ID
     */
    Long createSnapshotByFile(Long channelId, Integer snapshotType, String filePath, Long fileSize);

    /**
     * 通过文件上传创建抓图记录（复用基础设施文件存储）
     *
     * @param channelId 通道ID
     * @param snapshotType 抓图类型（默认1=手动）
     * @param file 图片文件
     * @return 抓图记录ID
     */
    Long uploadSnapshot(Long channelId, Integer snapshotType, MultipartFile file) throws Exception;

}


