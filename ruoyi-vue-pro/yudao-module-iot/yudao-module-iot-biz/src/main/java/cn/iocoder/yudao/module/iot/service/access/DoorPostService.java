package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorpost.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 门岗 Service 接口
 *
 * @author 智能化系统
 */
public interface DoorPostService {

    /**
     * 创建门岗
     *
     * @param createReqVO 创建信息
     * @return 门岗ID
     */
    Long createDoorPost(@Valid DoorPostCreateReqVO createReqVO);

    /**
     * 更新门岗
     *
     * @param updateReqVO 更新信息
     */
    void updateDoorPost(@Valid DoorPostUpdateReqVO updateReqVO);

    /**
     * 删除门岗
     *
     * @param id 门岗ID
     */
    void deleteDoorPost(Long id);

    /**
     * 获得门岗
     *
     * @param id 门岗ID
     * @return 门岗
     */
    DoorPostRespVO getDoorPost(Long id);

    /**
     * 获得门岗分页
     *
     * @param pageReqVO 分页查询
     * @return 门岗分页
     */
    PageResult<DoorPostRespVO> getDoorPostPage(DoorPostPageReqVO pageReqVO);

    /**
     * 获得门岗列表
     *
     * @return 门岗列表
     */
    List<DoorPostRespVO> getDoorPostList();

}


























