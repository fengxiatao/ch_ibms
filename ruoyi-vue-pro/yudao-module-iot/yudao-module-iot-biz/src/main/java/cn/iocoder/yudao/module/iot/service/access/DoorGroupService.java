package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorgroup.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 门组 Service 接口
 *
 * @author 智能化系统
 */
public interface DoorGroupService {

    /**
     * 创建门组
     *
     * @param createReqVO 创建信息
     * @return 门组ID
     */
    Long createDoorGroup(@Valid DoorGroupCreateReqVO createReqVO);

    /**
     * 更新门组
     *
     * @param updateReqVO 更新信息
     */
    void updateDoorGroup(@Valid DoorGroupUpdateReqVO updateReqVO);

    /**
     * 删除门组
     *
     * @param id 门组ID
     */
    void deleteDoorGroup(Long id);

    /**
     * 获得门组
     *
     * @param id 门组ID
     * @return 门组
     */
    DoorGroupRespVO getDoorGroup(Long id);

    /**
     * 获得门组分页
     *
     * @param pageReqVO 分页查询
     * @return 门组分页
     */
    PageResult<DoorGroupRespVO> getDoorGroupPage(DoorGroupPageReqVO pageReqVO);

    /**
     * 获得门组列表
     *
     * @return 门组列表
     */
    List<DoorGroupRespVO> getDoorGroupList();

}


























