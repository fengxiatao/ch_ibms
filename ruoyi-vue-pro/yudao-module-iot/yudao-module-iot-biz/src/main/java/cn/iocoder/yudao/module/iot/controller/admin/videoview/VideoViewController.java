package cn.iocoder.yudao.module.iot.controller.admin.videoview;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewTreeVO;
import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewVO;
import cn.iocoder.yudao.module.iot.service.videoview.VideoViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 视频监控 - 实时预览视图 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 视频监控视图")
@RestController
@RequestMapping("/iot/video-view")
@Validated
public class VideoViewController {

    @Resource
    private VideoViewService videoViewService;

    @PostMapping("/create")
    @Operation(summary = "创建视图")
    public CommonResult<Long> createVideoView(@Valid @RequestBody VideoViewSaveReqVO createReqVO) {
        return success(videoViewService.createVideoView(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新视图")
    public CommonResult<Boolean> updateVideoView(@Valid @RequestBody VideoViewSaveReqVO updateReqVO) {
        videoViewService.updateVideoView(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除视图")
    @Parameter(name = "id", description = "视图ID", required = true, example = "1")
    public CommonResult<Boolean> deleteVideoView(@RequestParam("id") Long id) {
        videoViewService.deleteVideoView(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取视图详情")
    @Parameter(name = "id", description = "视图ID", required = true, example = "1")
    public CommonResult<VideoViewVO> getVideoView(@RequestParam("id") Long id) {
        VideoViewVO view = videoViewService.getVideoView(id);
        return success(view);
    }

    @GetMapping("/tree")
    @Operation(summary = "获取视图树（分组+视图）")
    public CommonResult<List<VideoViewTreeVO>> getVideoViewTree() {
        List<VideoViewTreeVO> tree = videoViewService.getVideoViewTree();
        return success(tree);
    }

    @PutMapping("/set-default")
    @Operation(summary = "设置默认视图")
    @Parameter(name = "id", description = "视图ID", required = true, example = "1")
    public CommonResult<Boolean> setDefaultVideoView(@RequestParam("id") Long id) {
        videoViewService.setDefaultVideoView(id);
        return success(true);
    }

}
