package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.authorization.*;

import jakarta.validation.Valid;

/**
 * 门禁授权 Service 接口
 *
 * @author 智能化系统
 */
public interface AccessAuthorizationService {

    /**
     * 创建门禁授权
     *
     * @param createReqVO 创建信息
     * @return 授权ID
     */
    Long createAccessAuthorization(@Valid AccessAuthorizationCreateReqVO createReqVO);

    /**
     * 更新门禁授权
     *
     * @param updateReqVO 更新信息
     */
    void updateAccessAuthorization(@Valid AccessAuthorizationUpdateReqVO updateReqVO);

    /**
     * 删除门禁授权
     *
     * @param id 授权ID
     */
    void deleteAccessAuthorization(Long id);

    /**
     * 获得门禁授权
     *
     * @param id 授权ID
     * @return 门禁授权
     */
    AccessAuthorizationRespVO getAccessAuthorization(Long id);

    /**
     * 获得门禁授权分页
     *
     * @param pageReqVO 分页查询
     * @return 门禁授权分页
     */
    PageResult<AccessAuthorizationRespVO> getAccessAuthorizationPage(AccessAuthorizationPageReqVO pageReqVO);

    /**
     * 更新授权状态
     *
     * @param id 授权ID
     * @param status 状态
     */
    void updateAccessAuthorizationStatus(Long id, Integer status);

}


























