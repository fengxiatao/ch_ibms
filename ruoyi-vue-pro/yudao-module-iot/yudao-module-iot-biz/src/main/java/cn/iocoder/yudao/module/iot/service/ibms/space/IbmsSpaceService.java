package cn.iocoder.yudao.module.iot.service.ibms.space;

import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsSpaceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsSpaceSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsSpaceTreeNodeRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsUnassignedItemRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsUnassignedPageReqVO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.util.List;

/**
 * IBMS 空间管理 Service 接口
 */
public interface IbmsSpaceService {

    Long createSpace(IbmsSpaceSaveReqVO reqVO);

    void updateSpace(IbmsSpaceSaveReqVO reqVO);

    void deleteSpace(Long id);

    IbmsSpaceRespVO getSpace(Long id);

    List<IbmsSpaceTreeNodeRespVO> getSpaceTree();

    PageResult<IbmsUnassignedItemRespVO> getUnassignedPage(IbmsUnassignedPageReqVO reqVO);
}

