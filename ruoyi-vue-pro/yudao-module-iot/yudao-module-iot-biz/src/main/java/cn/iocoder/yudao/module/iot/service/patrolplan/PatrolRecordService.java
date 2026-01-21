package cn.iocoder.yudao.module.iot.service.patrolplan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.record.PatrolRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolRecordDO;

import jakarta.validation.Valid;

/**
 * 轮巡记录 Service 接口
 *
 * @author 长辉信息
 */
public interface PatrolRecordService {

    /**
     * 获得轮巡记录
     *
     * @param id 编号
     * @return 轮巡记录
     */
    IotVideoPatrolRecordDO getPatrolRecord(Long id);

    /**
     * 获得轮巡记录分页
     *
     * @param pageReqVO 分页查询
     * @return 轮巡记录分页
     */
    PageResult<IotVideoPatrolRecordDO> getPatrolRecordPage(@Valid PatrolRecordPageReqVO pageReqVO);

    /**
     * 标记为已处理
     *
     * @param id 编号
     * @param remark 处理备注
     */
    void markAsHandled(Long id, String remark);

}
