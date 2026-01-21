package cn.iocoder.yudao.module.iot.service.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorApplyDO;

/**
 * 访客申请 Service 接口
 *
 * @author 芋道源码
 */
public interface IotVisitorApplyService {

    /**
     * 创建访客申请
     *
     * @param reqVO 创建信息
     * @return 申请ID
     */
    Long createApply(IotVisitorApplyCreateReqVO reqVO);

    /**
     * 更新访客申请
     *
     * @param id    申请ID
     * @param reqVO 更新信息
     */
    void updateApply(Long id, IotVisitorApplyCreateReqVO reqVO);

    /**
     * 删除访客申请
     *
     * @param id 申请ID
     */
    void deleteApply(Long id);

    /**
     * 获取访客申请
     *
     * @param id 申请ID
     * @return 访客申请
     */
    IotVisitorApplyDO getApply(Long id);

    /**
     * 获取访客申请详情（含授权设备）
     *
     * @param id 申请ID
     * @return 访客申请详情
     */
    IotVisitorApplyRespVO getApplyDetail(Long id);

    /**
     * 获取访客申请分页
     *
     * @param reqVO 分页查询条件
     * @return 访客申请分页
     */
    PageResult<IotVisitorApplyRespVO> getApplyPage(IotVisitorApplyPageReqVO reqVO);

    /**
     * 审批通过
     *
     * @param id     申请ID
     * @param remark 审批备注
     */
    void approve(Long id, String remark);

    /**
     * 审批拒绝
     *
     * @param id     申请ID
     * @param remark 拒绝原因
     */
    void reject(Long id, String remark);

    /**
     * 取消申请
     *
     * @param id     申请ID
     * @param reason 取消原因
     */
    void cancel(Long id, String reason);

    /**
     * 访客签到（到访）
     *
     * @param id 申请ID
     */
    void checkIn(Long id);

    /**
     * 访客签离
     *
     * @param id 申请ID
     */
    void checkOut(Long id);

    /**
     * 下发权限
     *
     * @param reqVO 下发信息
     * @return 下发任务ID
     */
    Long dispatchAuth(IotVisitorAuthDispatchReqVO reqVO);

    /**
     * 回收权限
     *
     * @param applyId 申请ID
     * @return 回收任务ID
     */
    Long revokeAuth(Long applyId);

    /**
     * 获取访客统计
     *
     * @return 统计数据
     */
    IotVisitorStatisticsRespVO getStatistics();

}
