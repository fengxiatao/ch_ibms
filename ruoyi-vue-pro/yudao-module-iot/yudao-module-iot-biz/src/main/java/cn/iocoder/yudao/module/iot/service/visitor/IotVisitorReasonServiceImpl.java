package cn.iocoder.yudao.module.iot.service.visitor;

import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorReasonDO;
import cn.iocoder.yudao.module.iot.dal.mysql.visitor.IotVisitorReasonMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.VISITOR_REASON_NOT_EXISTS;

/**
 * 访客来访事由 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotVisitorReasonServiceImpl implements IotVisitorReasonService {

    @Resource
    private IotVisitorReasonMapper visitorReasonMapper;

    @Override
    public List<IotVisitorReasonDO> getReasonList() {
        // 只返回启用状态的来访事由
        return visitorReasonMapper.selectListByStatus(0);
    }

    @Override
    public Long createReason(String reasonName, Integer sort, String remark) {
        IotVisitorReasonDO reason = IotVisitorReasonDO.builder()
                .reasonName(reasonName)
                .sort(sort != null ? sort : 0)
                .status(0)
                .remark(remark)
                .build();
        visitorReasonMapper.insert(reason);
        return reason.getId();
    }

    @Override
    public void updateReason(Long id, String reasonName, Integer sort, Integer status, String remark) {
        // 校验存在
        validateReasonExists(id);
        // 更新
        IotVisitorReasonDO updateObj = IotVisitorReasonDO.builder()
                .id(id)
                .reasonName(reasonName)
                .sort(sort)
                .status(status)
                .remark(remark)
                .build();
        visitorReasonMapper.updateById(updateObj);
    }

    @Override
    public void deleteReason(Long id) {
        // 校验存在
        validateReasonExists(id);
        // 删除
        visitorReasonMapper.deleteById(id);
    }

    @Override
    public IotVisitorReasonDO getReason(Long id) {
        return visitorReasonMapper.selectById(id);
    }

    private void validateReasonExists(Long id) {
        if (visitorReasonMapper.selectById(id) == null) {
            throw exception(VISITOR_REASON_NOT_EXISTS);
        }
    }

}
