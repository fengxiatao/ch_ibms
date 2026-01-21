package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.campus.CampusPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.campus.CampusSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 园区 Service 接口
 *
 * @author IBMS Team
 */
public interface CampusService {

    /**
     * 创建园区
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCampus(@Valid CampusSaveReqVO createReqVO);

    /**
     * 更新园区
     *
     * @param updateReqVO 更新信息
     */
    void updateCampus(@Valid CampusSaveReqVO updateReqVO);

    /**
     * 删除园区
     *
     * @param id 编号
     */
    void deleteCampus(Long id);

    /**
     * 获得园区
     *
     * @param id 编号
     * @return 园区
     */
    CampusDO getCampus(Long id);

    /**
     * 获得园区分页
     *
     * @param pageReqVO 分页查询
     * @return 园区分页
     */
    PageResult<CampusDO> getCampusPage(CampusPageReqVO pageReqVO);

    /**
     * 获得园区列表
     *
     * @return 园区列表
     */
    List<CampusDO> getCampusList();

    /**
     * 获取所有配置了 jobConfig 的园区
     *
     * @return 园区列表（包含 id 和 jobConfig 字段）
     */
    List<CampusDO> getCampusWithJobConfig();

    /**
     * 更新园区的定时任务配置
     *
     * @param id 园区ID
     * @param jobConfig 任务配置JSON
     */
    void updateCampusJobConfig(Long id, String jobConfig);

}

