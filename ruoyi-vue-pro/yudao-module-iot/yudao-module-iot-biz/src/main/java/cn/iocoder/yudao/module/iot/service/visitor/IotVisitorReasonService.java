package cn.iocoder.yudao.module.iot.service.visitor;

import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorReasonDO;

import java.util.List;

/**
 * 访客来访事由 Service 接口
 *
 * @author 芋道源码
 */
public interface IotVisitorReasonService {

    /**
     * 获取启用的来访事由列表
     *
     * @return 来访事由列表
     */
    List<IotVisitorReasonDO> getReasonList();

    /**
     * 创建来访事由
     *
     * @param reasonName 事由名称
     * @param sort 排序
     * @param remark 备注
     * @return 主键ID
     */
    Long createReason(String reasonName, Integer sort, String remark);

    /**
     * 更新来访事由
     *
     * @param id 主键ID
     * @param reasonName 事由名称
     * @param sort 排序
     * @param status 状态
     * @param remark 备注
     */
    void updateReason(Long id, String reasonName, Integer sort, Integer status, String remark);

    /**
     * 删除来访事由
     *
     * @param id 主键ID
     */
    void deleteReason(Long id);

    /**
     * 获取来访事由详情
     *
     * @param id 主键ID
     * @return 来访事由
     */
    IotVisitorReasonDO getReason(Long id);

}
