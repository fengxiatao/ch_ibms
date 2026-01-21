package cn.iocoder.yudao.module.iot.service.subsystem;

import cn.iocoder.yudao.module.iot.controller.admin.subsystem.vo.SubsystemRespVO;

import java.util.List;

/**
 * IoT子系统 Service 接口
 *
 * @author IBMS Team
 */
public interface SubsystemService {

    /**
     * 获取所有子系统列表（扁平）
     *
     * @return 子系统列表
     */
    List<SubsystemRespVO> getSubsystemList();

    /**
     * 获取子系统树（两级）
     *
     * @return 子系统树
     */
    List<SubsystemRespVO> getSubsystemTree();

    /**
     * 根据代码获取子系统
     *
     * @param code 子系统代码
     * @return 子系统信息
     */
    SubsystemRespVO getSubsystemByCode(String code);

    /**
     * 获取指定父系统的子系统列表
     *
     * @param parentCode 父系统代码
     * @return 子系统列表
     */
    List<SubsystemRespVO> getSubsystemListByParent(String parentCode);

    /**
     * 获取子系统统计信息（包含产品数、设备数）
     *
     * @return 子系统统计列表
     */
    List<SubsystemRespVO> getSubsystemStatistics();
}


















































