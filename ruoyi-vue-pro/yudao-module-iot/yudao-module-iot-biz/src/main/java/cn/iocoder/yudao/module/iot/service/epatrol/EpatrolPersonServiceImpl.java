package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPersonPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPersonSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPersonDO;
import cn.iocoder.yudao.module.iot.dal.mysql.epatrol.EpatrolPersonMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 电子巡更 - 巡更人员 Service 实现类
 *
 * @author 长辉信息
 */
@Service
@Validated
public class EpatrolPersonServiceImpl implements EpatrolPersonService {

    @Resource
    private EpatrolPersonMapper personMapper;

    @Override
    public Long createPerson(EpatrolPersonSaveReqVO createReqVO) {
        EpatrolPersonDO person = BeanUtils.toBean(createReqVO, EpatrolPersonDO.class);
        person.setStatus(1); // 默认启用
        personMapper.insert(person);
        return person.getId();
    }

    @Override
    public void updatePerson(EpatrolPersonSaveReqVO updateReqVO) {
        // 校验存在
        validatePersonExists(updateReqVO.getId());
        // 更新
        EpatrolPersonDO updateObj = BeanUtils.toBean(updateReqVO, EpatrolPersonDO.class);
        personMapper.updateById(updateObj);
    }

    @Override
    public void deletePerson(Long id) {
        // 校验存在
        validatePersonExists(id);
        // 删除
        personMapper.deleteById(id);
    }

    private void validatePersonExists(Long id) {
        if (personMapper.selectById(id) == null) {
            throw exception(EPATROL_PERSON_NOT_EXISTS);
        }
    }

    @Override
    public EpatrolPersonDO getPerson(Long id) {
        return personMapper.selectById(id);
    }

    @Override
    public List<EpatrolPersonDO> getPersonList(Collection<Long> ids) {
        return personMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<EpatrolPersonDO> getPersonPage(EpatrolPersonPageReqVO pageReqVO) {
        return personMapper.selectPage(pageReqVO);
    }

    @Override
    public List<EpatrolPersonDO> getEnabledPersonList() {
        return personMapper.selectListByStatus(1);
    }

    @Override
    public void updatePersonStatus(Long id, Integer status) {
        // 校验存在
        validatePersonExists(id);
        // 更新状态
        EpatrolPersonDO updateObj = new EpatrolPersonDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        personMapper.updateById(updateObj);
    }

    @Override
    public EpatrolPersonDO getPersonByCardNo(String personCardNo) {
        return personMapper.selectByPersonCardNo(personCardNo);
    }

}
