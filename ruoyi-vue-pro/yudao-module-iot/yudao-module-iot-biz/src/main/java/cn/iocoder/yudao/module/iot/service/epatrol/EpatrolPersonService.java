package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPersonPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPersonSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPersonDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 电子巡更 - 巡更人员 Service 接口
 *
 * @author 长辉信息
 */
public interface EpatrolPersonService {

    /**
     * 创建巡更人员
     */
    Long createPerson(@Valid EpatrolPersonSaveReqVO createReqVO);

    /**
     * 更新巡更人员
     */
    void updatePerson(@Valid EpatrolPersonSaveReqVO updateReqVO);

    /**
     * 删除巡更人员
     */
    void deletePerson(Long id);

    /**
     * 获得巡更人员
     */
    EpatrolPersonDO getPerson(Long id);

    /**
     * 获得巡更人员列表
     */
    List<EpatrolPersonDO> getPersonList(Collection<Long> ids);

    /**
     * 获得巡更人员分页
     */
    PageResult<EpatrolPersonDO> getPersonPage(EpatrolPersonPageReqVO pageReqVO);

    /**
     * 获得所有启用的巡更人员
     */
    List<EpatrolPersonDO> getEnabledPersonList();

    /**
     * 更新巡更人员状态
     */
    void updatePersonStatus(Long id, Integer status);

    /**
     * 根据人员卡编号获取人员
     */
    EpatrolPersonDO getPersonByCardNo(String personCardNo);

}
