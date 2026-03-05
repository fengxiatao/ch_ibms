package cn.iocoder.yudao.module.iot.controller.admin.videoview;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewGroupVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.videoview.VideoViewGroupDO;
import cn.iocoder.yudao.module.iot.dal.mysql.videoview.VideoViewGroupMapper;
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
 * 视频监控 - 实时预览视图分组 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 视频监控视图分组")
@RestController
@RequestMapping("/iot/video-view-group")
@Validated
public class VideoViewGroupController {

    @Resource
    private VideoViewGroupMapper videoViewGroupMapper;

    @GetMapping("/list")
    @Operation(summary = "获取分组列表")
    public CommonResult<List<VideoViewGroupVO>> getVideoViewGroupList() {
        List<VideoViewGroupDO> list = videoViewGroupMapper.selectList();
        return success(BeanUtils.toBean(list, VideoViewGroupVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建视图分组")
    public CommonResult<Long> createVideoViewGroup(@Valid @RequestBody VideoViewGroupVO vo) {
        VideoViewGroupDO groupDO = BeanUtils.toBean(vo, VideoViewGroupDO.class);
        videoViewGroupMapper.insert(groupDO);
        return success(groupDO.getId());
    }

    @PutMapping("/update")
    @Operation(summary = "更新视图分组")
    public CommonResult<Boolean> updateVideoViewGroup(@Valid @RequestBody VideoViewGroupVO vo) {
        VideoViewGroupDO groupDO = BeanUtils.toBean(vo, VideoViewGroupDO.class);
        videoViewGroupMapper.updateById(groupDO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除视图分组")
    @Parameter(name = "id", description = "分组ID", required = true)
    public CommonResult<Boolean> deleteVideoViewGroup(@RequestParam("id") Long id) {
        videoViewGroupMapper.deleteById(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取视图分组详情")
    @Parameter(name = "id", description = "分组ID", required = true)
    public CommonResult<VideoViewGroupVO> getVideoViewGroup(@RequestParam("id") Long id) {
        VideoViewGroupDO groupDO = videoViewGroupMapper.selectById(id);
        return success(BeanUtils.toBean(groupDO, VideoViewGroupVO.class));
    }

}
