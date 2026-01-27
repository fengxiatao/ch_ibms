package cn.iocoder.yudao.module.iot.controller.admin.videoview;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.videoview.vo.VideoViewGroupVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.videoview.VideoViewGroupDO;
import cn.iocoder.yudao.module.iot.dal.mysql.videoview.VideoViewGroupMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
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
public class VideoViewGroupController {

    @Resource
    private VideoViewGroupMapper videoViewGroupMapper;

    @GetMapping("/list")
    @Operation(summary = "获取分组列表")
    public CommonResult<List<VideoViewGroupVO>> getVideoViewGroupList() {
        List<VideoViewGroupDO> list = videoViewGroupMapper.selectList();
        return success(BeanUtils.toBean(list, VideoViewGroupVO.class));
    }

}
