package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dispatch.*;

/**
 * 门禁下发记录 Service 接口
 *
 * @author 智能化系统
 */
public interface AccessDispatchService {

    /**
     * 获得门禁下发记录分页
     *
     * @param pageReqVO 分页查询
     * @return 门禁下发记录分页
     */
    PageResult<AccessDispatchRespVO> getAccessDispatchPage(AccessDispatchPageReqVO pageReqVO);

    /**
     * 获得门禁下发记录
     *
     * @param id 记录ID
     * @return 门禁下发记录
     */
    AccessDispatchRespVO getAccessDispatch(Long id);

}


























