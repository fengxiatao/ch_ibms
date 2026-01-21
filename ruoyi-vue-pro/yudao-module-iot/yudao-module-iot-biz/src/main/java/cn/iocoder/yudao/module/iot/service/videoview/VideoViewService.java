package cn.iocoder.yudao.module.iot.service.videoview;

import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewTreeVO;
import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewVO;

import java.util.List;

/**
 * 视频监控 - 实时预览视图 Service 接口
 *
 * @author 芋道源码
 */
public interface VideoViewService {

    /**
     * 创建视图
     *
     * @param createReqVO 创建信息
     * @return 视图ID
     */
    Long createVideoView(VideoViewSaveReqVO createReqVO);

    /**
     * 更新视图
     *
     * @param updateReqVO 更新信息
     */
    void updateVideoView(VideoViewSaveReqVO updateReqVO);

    /**
     * 删除视图
     *
     * @param id 视图ID
     */
    void deleteVideoView(Long id);

    /**
     * 获取视图详情
     *
     * @param id 视图ID
     * @return 视图详情
     */
    VideoViewVO getVideoView(Long id);

    /**
     * 获取视图树（分组+视图）
     *
     * @return 视图树
     */
    List<VideoViewTreeVO> getVideoViewTree();

    /**
     * 设置默认视图
     *
     * @param id 视图ID
     */
    void setDefaultVideoView(Long id);

}
