package cn.iocoder.yudao.module.iot.service.videoview;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewPaneVO;
import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewTreeVO;
import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.videoview.VideoViewDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.videoview.VideoViewGroupDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.videoview.VideoViewPaneDO;
import cn.iocoder.yudao.module.iot.dal.mysql.videoview.VideoViewGroupMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.videoview.VideoViewMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.videoview.VideoViewPaneMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 视频监控 - 实时预览视图 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class VideoViewServiceImpl implements VideoViewService {

    @Resource
    private VideoViewMapper videoViewMapper;

    @Resource
    private VideoViewPaneMapper videoViewPaneMapper;

    @Resource
    private VideoViewGroupMapper videoViewGroupMapper;

    /**
     * 将 List<Long> 转换为分号分隔的字符串
     * 例如: [1,2,3] -> "1;2;3"
     */
    private String joinGroupIds(List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return null;
        }
        return groupIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(";"));
    }

    /**
     * 将分号分隔的字符串转换为 List<Long>
     * 例如: "1;2;3" -> [1,2,3]
     */
    private List<Long> splitGroupIds(String groupIdsStr) {
        List<Long> groupIds = new ArrayList<>();
        if (groupIdsStr == null || groupIdsStr.trim().isEmpty()) {
            return groupIds;
        }
        
        try {
            for (String idStr : groupIdsStr.split(";")) {
                if (!idStr.trim().isEmpty()) {
                    groupIds.add(Long.parseLong(idStr.trim()));
                }
            }
        } catch (NumberFormatException e) {
            log.warn("[splitGroupIds] 无法解析 groupIds: {}", groupIdsStr);
        }
        return groupIds;
    }

    /**
     * 检查视图是否属于指定分组
     */
    private boolean viewBelongsToGroup(String groupIdsStr, Long targetGroupId) {
        if (groupIdsStr == null || groupIdsStr.trim().isEmpty()) {
            return false;
        }
        
        try {
            for (String idStr : groupIdsStr.split(";")) {
                if (!idStr.trim().isEmpty()) {
                    Long groupId = Long.parseLong(idStr.trim());
                    if (groupId.equals(targetGroupId)) {
                        return true;
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.warn("[viewBelongsToGroup] 无法解析 groupIds: {}", groupIdsStr);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVideoView(VideoViewSaveReqVO createReqVO) {
        // 1. 创建视图主记录
        VideoViewDO view = BeanUtils.toBean(createReqVO, VideoViewDO.class);
        view.setGroupIds(joinGroupIds(createReqVO.getGroupIds()));
        videoViewMapper.insert(view);

        // 2. 批量创建窗格配置
        if (CollUtil.isNotEmpty(createReqVO.getPanes())) {
            List<VideoViewPaneDO> panes = createReqVO.getPanes().stream()
                    .map(pane -> {
                        VideoViewPaneDO paneDO = BeanUtils.toBean(pane, VideoViewPaneDO.class);
                        paneDO.setViewId(view.getId());
                        return paneDO;
                    })
                    .collect(Collectors.toList());

            panes.forEach(videoViewPaneMapper::insert);
        }

        log.info("[createVideoView][创建视图成功，视图ID：{}]", view.getId());
        return view.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVideoView(VideoViewSaveReqVO updateReqVO) {
        // 1. 更新视图主记录
        VideoViewDO view = BeanUtils.toBean(updateReqVO, VideoViewDO.class);
        view.setGroupIds(joinGroupIds(updateReqVO.getGroupIds()));
        videoViewMapper.updateById(view);

        // 2. 删除旧的窗格配置
        videoViewPaneMapper.deleteByViewId(updateReqVO.getId());

        // 3. 创建新的窗格配置
        if (CollUtil.isNotEmpty(updateReqVO.getPanes())) {
            List<VideoViewPaneDO> panes = updateReqVO.getPanes().stream()
                    .map(pane -> {
                        VideoViewPaneDO paneDO = BeanUtils.toBean(pane, VideoViewPaneDO.class);
                        paneDO.setViewId(updateReqVO.getId());
                        return paneDO;
                    })
                    .collect(Collectors.toList());

            panes.forEach(videoViewPaneMapper::insert);
        }

        log.info("[updateVideoView][更新视图成功，视图ID：{}]", updateReqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVideoView(Long id) {
        // 1. 删除视图主记录
        videoViewMapper.deleteById(id);

        // 2. 删除窗格配置
        videoViewPaneMapper.deleteByViewId(id);

        log.info("[deleteVideoView][删除视图成功，视图ID：{}]", id);
    }

    @Override
    public VideoViewVO getVideoView(Long id) {
        // 1. 查询视图主记录
        VideoViewDO view = videoViewMapper.selectById(id);
        if (view == null) {
            return null;
        }

        // 2. 转换为 VO
        VideoViewVO viewVO = BeanUtils.toBean(view, VideoViewVO.class);
        viewVO.setGroupIds(splitGroupIds(view.getGroupIds()));

        // 3. 查询窗格配置
        List<VideoViewPaneDO> panes = videoViewPaneMapper.selectListByViewId(id);
        List<VideoViewPaneVO> paneVOs = BeanUtils.toBean(panes, VideoViewPaneVO.class);
        viewVO.setPanes(paneVOs);

        return viewVO;
    }

    @Override
    public List<VideoViewTreeVO> getVideoViewTree() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String userIdStr = userId != null ? String.valueOf(userId) : "";

        // 1. 查询所有分组
        List<VideoViewGroupDO> groups = videoViewGroupMapper.selectList();

        // 2. 查询用户的所有视图
        List<VideoViewDO> views = videoViewMapper.selectListByCreator(userIdStr);

        // 3. 组装树形结构
        return groups.stream().map(group -> {
            VideoViewTreeVO treeNode = new VideoViewTreeVO();
            treeNode.setId(group.getId());
            treeNode.setName(group.getName());
            treeNode.setIcon(group.getIcon());
            treeNode.setType("group");

            // 过滤该分组下的视图（检查 groupIds 是否包含当前分组ID）
            List<VideoViewTreeVO> children = views.stream()
                    .filter(view -> viewBelongsToGroup(view.getGroupIds(), group.getId()))
                    .map(view -> {
                        VideoViewTreeVO child = new VideoViewTreeVO();
                        child.setId(view.getId());
                        child.setName(view.getName());
                        child.setIcon("ep:video-camera");
                        child.setType("view");
                        child.setGridLayout(view.getGridLayout());

                        // 统计窗格数量
                        List<VideoViewPaneDO> panes = videoViewPaneMapper.selectListByViewId(view.getId());
                        child.setPaneCount(panes.size());

                        return child;
                    })
                    .collect(Collectors.toList());

            treeNode.setChildren(children);
            return treeNode;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultVideoView(Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String userIdStr = userId != null ? String.valueOf(userId) : "";

        // 1. 取消当前用户的所有默认视图
        List<VideoViewDO> views = videoViewMapper.selectListByCreator(userIdStr);
        views.forEach(view -> {
            if (Boolean.TRUE.equals(view.getIsDefault())) {
                view.setIsDefault(false);
                videoViewMapper.updateById(view);
            }
        });

        // 2. 设置新的默认视图
        VideoViewDO view = videoViewMapper.selectById(id);
        if (view != null) {
            view.setIsDefault(true);
            videoViewMapper.updateById(view);
        }

        log.info("[setDefaultVideoView][设置默认视图成功，视图ID：{}]", id);
    }

}
